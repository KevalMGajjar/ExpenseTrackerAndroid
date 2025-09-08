package com.example.splitwiseclone.ui_viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.rest_api.RestApiService
import com.example.splitwiseclone.rest_api.SettleUpRequest
import com.example.splitwiseclone.roomdb.entities.Friend
import com.example.splitwiseclone.roomdb.friends.FriendRepository
import com.example.splitwiseclone.roomdb.entities.CurrentUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject
import kotlin.math.abs

enum class PaymentDirection {
    USER_PAYS_FRIEND,
    FRIEND_PAYS_USER
}

@HiltViewModel
class SettleUpViewModel @Inject constructor(
    private val friendRepository: FriendRepository,
    private val apiService: RestApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val friendId: String? = savedStateHandle.get<String>("friendId")
    private val _friend = MutableStateFlow<Friend?>(null)
    val friend = _friend.asStateFlow()
    private val _amount = MutableStateFlow("0.00")
    val amount = _amount.asStateFlow()

    private val _paymentDirection = MutableStateFlow<PaymentDirection?>(null)
    val paymentDirection = _paymentDirection.asStateFlow()

    private var isAmountEditedByUser = false

    init {
        if (friendId != null) {
            viewModelScope.launch {
                _friend.value = friendRepository.findFriendById(friendId).firstOrNull()
                val balance = _friend.value?.balanceWithUser ?: 0.0
                _amount.value = String.format(Locale.US, "%.2f", abs(balance))

                if (balance < 0) {
                    _paymentDirection.value = PaymentDirection.USER_PAYS_FRIEND
                } else if (balance > 0) {
                    _paymentDirection.value = PaymentDirection.FRIEND_PAYS_USER
                }
            }
        }
    }

    fun setPaymentDirection(direction: PaymentDirection) {
        _paymentDirection.value = direction
    }

    fun onKeypadPress(key: String) {
        var currentAmount = if (isAmountEditedByUser) _amount.value else ""
        isAmountEditedByUser = true

        val newAmountStr = when (key) {
            "x" -> if (currentAmount.isNotEmpty()) currentAmount.dropLast(1) else "0.00"
            "." -> if (!currentAmount.contains(".")) "$currentAmount." else currentAmount
            else -> if (currentAmount == "0.00") key else currentAmount + key
        }

        val balance = abs(_friend.value?.balanceWithUser ?: 0.0)
        val newAmount = newAmountStr.toDoubleOrNull() ?: 0.0

        _amount.value = if (newAmount > balance) {
            String.format(Locale.US, "%.2f", balance)
        } else {
            newAmountStr.ifEmpty { "0.00" }
        }
    }

    fun settlePayment(currentUser: CurrentUser, onSuccess: () -> Unit) {
        val friendValue = _friend.value ?: return
        val amountToSettle = _amount.value.toDoubleOrNull() ?: 0.0
        val direction = _paymentDirection.value ?: return

        if (amountToSettle <= 0.0) return

        viewModelScope.launch {
            try {
                val (payerId, receiverId) = when (direction) {
                    PaymentDirection.USER_PAYS_FRIEND -> currentUser.currentUserId to friendValue.friendId
                    PaymentDirection.FRIEND_PAYS_USER -> friendValue.friendId to currentUser.currentUserId
                }

                val request = SettleUpRequest(
                    payerId = payerId,
                    receiverId = receiverId,
                    amount = amountToSettle
                )
                val response = apiService.settleUp(request)

                if (response.isSuccessful) {
                    val balanceChange = if (direction == PaymentDirection.USER_PAYS_FRIEND) amountToSettle else -amountToSettle
                    val updatedFriend = friendValue.copy(
                        balanceWithUser = friendValue.balanceWithUser + balanceChange
                    )
                    friendRepository.updateFriend(updatedFriend)
                    onSuccess()
                } else {
                    Log.e("SettleUpViewModel", "API error during settlement: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e("SettleUpViewModel", "Error settling payment via API", e)
            }
        }
    }
}