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
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.math.BigDecimal
import javax.inject.Inject

@HiltViewModel
class ExpenseEditViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val apiService: RestApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // ✅ FIX 1: Make expenseId nullable and remove '!!' to prevent crashes.
    private val expenseId: String? = savedStateHandle.get<String>("expenseId")

    // --- State for all editable fields ---
    // ✅ BEST PRACTICE: Expose read-only StateFlows to the UI.
    private val _description = MutableStateFlow("")
    val description = _description.asStateFlow()

    private val _totalAmount = MutableStateFlow("")
    val totalAmount = _totalAmount.asStateFlow()

    private val _date = MutableStateFlow("")
    val date = _date.asStateFlow()

    private val _participants = MutableStateFlow<List<String>>(emptyList())
    val participants = _participants.asStateFlow()

    private val _splits = MutableStateFlow<List<SplitDto>>(emptyList())
    val splits = _splits.asStateFlow()

    private val _paidByUserIds = MutableStateFlow<List<String>>(emptyList())
    val paidByUserIds = _paidByUserIds.asStateFlow()

    private val _paidByText = MutableStateFlow("Select Payer")
    val paidByText = _paidByText.asStateFlow()

    private val _splitText = MutableStateFlow("Select Split")
    val splitText = _splitText.asStateFlow()

    // Helper states
    private val _currencyCode = MutableStateFlow("INR")
    private val _splitType = MutableStateFlow("EQUAL")
    private val _groupId = MutableStateFlow<String?>(null)

    init {
        // ✅ FIX 2: Only attempt to load the expense if the ID is not null.
        if (expenseId != null) {
            loadExpenseForEditing(expenseId)
        } else {
            // This case can happen if navigation goes wrong.
            // The ViewModel will now handle it safely instead of crashing.
            Log.e("ExpenseEditViewModel", "expenseId is null, cannot load expense for editing.")
        }
    }

    private fun loadExpenseForEditing(id: String) {
        viewModelScope.launch {
            val expense = expenseRepository.getExpenseById(id).first()
            expense?.let {
                _description.value = it.description ?: ""
                _totalAmount.value = it.totalExpense.toString()
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

    // --- Functions for the UI to call ---
    fun onDescriptionChange(newDescription: String) {
        _description.value = newDescription
    }

    fun onAmountChange(newAmount: String) {
        _totalAmount.value = newAmount
    }

    fun onDateSelected(newDate: String) {
        _date.value = newDate
    }

    fun toggleParticipant(friendId: String) {
        val currentParticipants = _participants.value.toMutableList()
        if (currentParticipants.contains(friendId)) {
            if (currentParticipants.size > 1) currentParticipants.remove(friendId)
        } else {
            currentParticipants.add(friendId)
        }
        _participants.value = currentParticipants
    }

    fun commitPayerSelection(payers: List<String>, text: String) {
        _paidByUserIds.value = payers
        _paidByText.value = text
    }

    fun commitSplitSelection(newSplits: List<SplitDto>, text: String) {
        _splits.value = newSplits
        _splitText.value = text
    }

    fun saveChanges(onSuccess: () -> Unit) {
        // ✅ FIX 3: Ensure you don't try to save if the ID was missing from the start.
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