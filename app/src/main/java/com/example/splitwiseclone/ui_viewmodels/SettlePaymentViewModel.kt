package com.example.splitwiseclone.ui_viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.rest_api.RequestSplitDto
import com.example.splitwiseclone.rest_api.RestApiService
import com.example.splitwiseclone.rest_api.UpdateExpenseRequest
import com.example.splitwiseclone.roomdb.expense.Expense
import com.example.splitwiseclone.roomdb.expense.ExpenseRepository
import com.example.splitwiseclone.roomdb.expense.Splits
import com.example.splitwiseclone.roomdb.friends.Friend
import com.example.splitwiseclone.roomdb.friends.FriendRepository
import com.example.splitwiseclone.roomdb.user.CurrentUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class SettleUpViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val expenseRepository: ExpenseRepository,
    private val apiService: RestApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val friendId: String? = savedStateHandle.get<String>("friendId")

    private val _friend = MutableStateFlow<Friend?>(null)
    val friend = _friend.asStateFlow()

    private val _amount = MutableStateFlow("0.00")
    val amount = _amount.asStateFlow()

    init {
        if (friendId != null) {
            viewModelScope.launch {
                _friend.value = friendRepository.findFriendById(friendId).firstOrNull()
                val balance = _friend.value?.balanceWithUser ?: 0.0
                if (balance < 0) {
                    _amount.value = String.format(Locale.US, "%.2f", -balance)
                }
            }
        }
    }

    fun onKeypadPress(key: String) {
        val currentAmount = _amount.value.replace(",", "")
        val newAmountStr = when (key) {
            "x" -> if (currentAmount.length > 1) currentAmount.dropLast(1) else "0"
            "." -> if (!currentAmount.contains(".")) "$currentAmount." else currentAmount
            else -> if (currentAmount == "0" || currentAmount == "0.00") key else currentAmount + key
        }

        if (newAmountStr.count { it == '.' } > 1) return
        if (newAmountStr.contains('.') && newAmountStr.substringAfter('.').length > 2) return

        _amount.value = newAmountStr
    }

    fun settlePayment(currentUser: CurrentUser, onSuccess: () -> Unit) {
        val friendValue = _friend.value ?: return
        val amountToSettle = _amount.value.toBigDecimalOrNull() ?: return

        if (amountToSettle <= BigDecimal.ZERO) return

        viewModelScope.launch {
            try {
                // 1. Create the request object for the API
                val createRequest = UpdateExpenseRequest(
                    description = "Payment to ${friendValue.username}",
                    totalExpense = amountToSettle,
                    splitType = "EXACT",
                    splits = listOf(
                        RequestSplitDto(
                            owedByUserId = friendValue.friendId,
                            owedToUserId = currentUser.currentUserId,
                            owedAmount = amountToSettle.toDouble()
                        )
                    ),
                    currencyCode = "USD", // Or get from settings
                    paidByUserIds = listOf(currentUser.currentUserId),
                    participants = listOf(currentUser.currentUserId, friendValue.friendId),
                    groupId = null,
                    id = friendValue.id
                )

                // 2. Call the API to save the expense on the server
                val expenseFromApi = apiService.updateExpense(createRequest)

                // 3. Map the API response to a local Room entity
                val expenseToSaveInDb = Expense(
                    id = expenseFromApi.id,
                    groupId = expenseFromApi.groupId,
                    createdById = expenseFromApi.createdByUserId,
                    totalExpense = expenseFromApi.totalExpense,
                    description = expenseFromApi.description,
                    splitType = expenseFromApi.splitType,
                    splits = expenseFromApi.splits.map { splitDto ->
                        Splits(
                            id = splitDto.id,
                            owedByUserId = splitDto.owedByUserId,
                            owedAmount = splitDto.owedAmount,
                            owedToUserId = splitDto.owedToUserId
                        )
                    },
                    isDeleted = expenseFromApi.deleted,
                    currencyCode = expenseFromApi.currencyCode,
                    paidByUserIds = expenseFromApi.paidByUserIds,
                    participants = expenseFromApi.participants,
                    expenseDate = expenseFromApi.expenseDate
                )

                // 4. Save the synced expense to the local database
                expenseRepository.insertExpense(expenseToSaveInDb)

                // 5. Update the friend's balance locally
                val updatedFriend = friendValue.copy(
                    balanceWithUser = friendValue.balanceWithUser + amountToSettle.toDouble()
                )
                friendRepository.updateFriend(updatedFriend)

                // 6. Signal success to the UI
                onSuccess()

            } catch (e: Exception) {
                Log.e("SettleUpViewModel", "Error settling payment via API", e)
                // Optionally, expose an error state to the UI here
            }
        }
    }
}

