package com.example.splitwiseclone.ui_viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.rest_api.RequestSplitDto
import com.example.splitwiseclone.rest_api.RestApiService
import com.example.splitwiseclone.rest_api.SplitDto
import com.example.splitwiseclone.rest_api.UpdateExpenseRequest
import com.example.splitwiseclone.roomdb.entities.Expense
import com.example.splitwiseclone.roomdb.expense.ExpenseRepository
import com.example.splitwiseclone.roomdb.entities.Splits
import com.example.splitwiseclone.roomdb.user.CurrentUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject

@HiltViewModel
class ExpenseEditViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val apiService: RestApiService,
    private val currentUserRepository: CurrentUserRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val expenseId: String? = savedStateHandle.get<String>("expenseId")

    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _totalAmount = MutableStateFlow("")
    val totalAmount = _totalAmount.asStateFlow()

    private var originalTotalAmount: BigDecimal = BigDecimal.ZERO

    private val _splits = MutableStateFlow<List<SplitDto>>(emptyList())
    val splits = _splits.asStateFlow()

    private val _date = MutableStateFlow("")
    val date = _date.asStateFlow()

    private val _participants = MutableStateFlow<List<String>>(emptyList())
    val participants = _participants.asStateFlow()

    private val _paidByUserIds = MutableStateFlow<List<String>>(emptyList())
    val paidByUserIds = _paidByUserIds.asStateFlow()

    private val _paidByText = MutableStateFlow("Select Payer")
    val paidByText = _paidByText.asStateFlow()

    private val _splitText = MutableStateFlow("Select Split")
    val splitText = _splitText.asStateFlow()

    private val _currencyCode = MutableStateFlow("INR")
    private val _splitType = MutableStateFlow("EQUAL")
    private val _groupId = MutableStateFlow<String?>(null)

    init {
        expenseId?.let { loadExpenseForEditing(it) }
    }

    private fun loadExpenseForEditing(id: String) {
        viewModelScope.launch {
            val expense = expenseRepository.getExpenseById(id).first()
            expense?.let {
                _description.value = it.description ?: ""
                _totalAmount.value = it.totalExpense.toString()
                originalTotalAmount = it.totalExpense.toBigDecimal()
                _date.value = it.expenseDate
                _participants.value = it.participants
                _paidByUserIds.value = it.paidByUserIds
                _groupId.value = it.groupId
                _splitType.value = it.splitType
                _currencyCode.value = it.currencyCode
                _splits.value = it.splits.map { roomSplit ->
                    SplitDto(
                        owedByUserId = roomSplit.owedByUserId,
                        owedAmount = roomSplit.owedAmount,
                        owedToUserId = roomSplit.owedToUserId
                    )
                }
            }
        }
    }

    fun onDescriptionChange(newDescription: String) {
        _description.value = newDescription
    }

    fun onAmountChange(newAmountString: String) {
        _totalAmount.value = newAmountString
        val newTotal = newAmountString.toBigDecimalOrNull() ?: BigDecimal.ZERO

        if (originalTotalAmount != BigDecimal.ZERO && newTotal > BigDecimal.ZERO) {
            val ratio = newTotal.divide(originalTotalAmount, 10, RoundingMode.HALF_UP)

            val newSplits = _splits.value.map { oldSplit ->
                val newSplitAmount = oldSplit.owedAmount.toBigDecimal().multiply(ratio).toDouble()
                oldSplit.copy(owedAmount = newSplitAmount)
            }
            _splits.value = newSplits
        }
    }

    fun commitPayerSelection(payers: List<String>, text: String) {
        _paidByUserIds.value = payers
        _paidByText.value = text
        recalculateSplitsEqually()
    }

    private fun recalculateSplitsEqually() {
        val total = _totalAmount.value.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val currentParticipants = _participants.value
        val currentPayers = _paidByUserIds.value

        if (total == BigDecimal.ZERO || currentParticipants.isEmpty() || currentPayers.isEmpty()) {
            _splits.value = emptyList()
            return
        }

        val primaryPayer = currentPayers.first()
        val share = total.divide(BigDecimal(currentParticipants.size), 2, RoundingMode.HALF_UP)

        val newSplits = currentParticipants
            .filter { it !in currentPayers }
            .map { participantId ->
                SplitDto(
                    owedByUserId = participantId,
                    owedAmount = share.toDouble(),
                    owedToUserId = primaryPayer
                )
            }

        _splits.value = newSplits
        _splitType.value = "EQUAL"
        _splitText.value = "Split Equally"
    }

    fun commitSplitSelection(newSplits: List<SplitDto>, text: String) {
        _splits.value = newSplits
        _splitText.value = text
        _splitType.value = if (text == "Split Equally") "EQUAL" else "CUSTOM"
    }

    fun recalculateForTwoPeople(splitType: String) {
        viewModelScope.launch {
            val total = _totalAmount.value.toBigDecimalOrNull() ?: return@launch
            val currentParticipants = _participants.value
            val currentUser = currentUserRepository.currentUser.first() ?: return@launch

            if (currentParticipants.size != 2) return@launch

            val friendId = currentParticipants.find { it != currentUser.currentUserId } ?: return@launch

            val newSplits: List<SplitDto>
            val newPayers: List<String>

            when (splitType) {
                "1" -> {
                    newPayers = listOf(currentUser.currentUserId)
                    newSplits = listOf(
                        SplitDto(
                            owedByUserId = friendId,
                            owedAmount = total.divide(BigDecimal(2), 2, RoundingMode.HALF_UP).toDouble(),
                            owedToUserId = currentUser.currentUserId
                        )
                    )
                }
                "2" -> {
                    newPayers = listOf(currentUser.currentUserId)
                    newSplits = listOf(
                        SplitDto(
                            owedByUserId = friendId,
                            owedAmount = total.toDouble(),
                            owedToUserId = currentUser.currentUserId
                        )
                    )
                }
                "3" -> {
                    newPayers = listOf(friendId)
                    newSplits = listOf(
                        SplitDto(
                            owedByUserId = currentUser.currentUserId,
                            owedAmount = total.divide(BigDecimal(2), 2, RoundingMode.HALF_UP).toDouble(),
                            owedToUserId = friendId
                        )
                    )
                }
                "4" -> {
                    newPayers = listOf(friendId)
                    newSplits = listOf(
                        SplitDto(
                            owedByUserId = currentUser.currentUserId,
                            owedAmount = total.toDouble(),
                            owedToUserId = friendId
                        )
                    )
                }
                else -> {
                    newPayers = _paidByUserIds.value
                    newSplits = _splits.value
                }
            }
            _paidByUserIds.value = newPayers
            _splits.value = newSplits
        }
    }

    fun saveChanges(onSuccess: () -> Unit) {
        expenseId ?: return

        viewModelScope.launch {
            try {
                val updateRequest = UpdateExpenseRequest(
                    id = expenseId,
                    description = _description.value,
                    totalExpense = _totalAmount.value.toBigDecimalOrNull() ?: BigDecimal.ZERO,
                    splitType = _splitType.value,
                    splits = _splits.value.map { RequestSplitDto(it.owedByUserId, it.owedAmount, it.owedToUserId) },
                    currencyCode = _currencyCode.value,
                    paidByUserIds = _paidByUserIds.value,
                    participants = _participants.value,
                    groupId = _groupId.value
                )

                val updatedExpenseFromApi = apiService.updateExpense(updateRequest)

                val expenseToSaveInDb = Expense(
                    id = updatedExpenseFromApi.id,
                    groupId = updatedExpenseFromApi.groupId,
                    createdById = updatedExpenseFromApi.createdByUserId,
                    totalExpense = updatedExpenseFromApi.totalExpense,
                    description = updatedExpenseFromApi.description,
                    splitType = updatedExpenseFromApi.splitType,
                    splits = updatedExpenseFromApi.splits.map { splitDto ->
                        Splits(
                            id = splitDto.id,
                            owedByUserId = splitDto.owedByUserId,
                            owedAmount = splitDto.owedAmount,
                            owedToUserId = splitDto.owedToUserId
                        )
                    },
                    isDeleted = updatedExpenseFromApi.deleted,
                    currencyCode = updatedExpenseFromApi.currencyCode,
                    paidByUserIds = updatedExpenseFromApi.paidByUserIds,
                    participants = updatedExpenseFromApi.participants,
                    expenseDate = updatedExpenseFromApi.expenseDate
                )

                expenseRepository.insertExpense(expenseToSaveInDb)
                onSuccess()

            } catch (e: Exception) {
                Log.e("ExpenseEditViewModel", "Error saving changes", e)
            }
        }
    }
}