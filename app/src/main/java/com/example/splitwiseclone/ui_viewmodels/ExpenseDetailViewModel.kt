package com.example.splitwiseclone.ui_viewmodels

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.central.SyncRepository
import com.example.splitwiseclone.rest_api.RestApiService
import com.example.splitwiseclone.roomdb.expense.Expense
import com.example.splitwiseclone.roomdb.expense.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseDetailViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val syncRepository: SyncRepository,
    private val apiService: RestApiService,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // 1. Get a FLOW of the expenseId from navigation. This is reactive.
    private val expenseIdFlow: StateFlow<String> = savedStateHandle.getStateFlow("expenseId", "")

    // 2. Use `flatMapLatest` to create a new flow that automatically updates
    //    whenever the expenseIdFlow changes.
    val expense: StateFlow<Expense?> = expenseIdFlow.flatMapLatest { id ->
        if (id.isEmpty()) {
            flowOf(null) // If no ID, emit null
        } else {
            // This will now correctly re-run the query when the ID is available.
            expenseRepository.getExpenseById(id)
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    fun deleteExpense(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val expenseToDelete = expense.value
            val currentExpenseId = expenseIdFlow.value // Use the value from the flow

            if (expenseToDelete != null && currentExpenseId.isNotEmpty()) {
                try {
                    apiService.deleteExpense(currentExpenseId)
                    expenseRepository.deleteExpense(expenseToDelete)
                    syncRepository.syncAllData()
                    onSuccess()
                } catch (e: Exception) {
                    Log.e("ExpenseDetailViewModel", "Error deleting expense", e)
                }
            } else {
                Log.e("ExpenseDetailViewModel", "Attempted to delete with null expense or empty ID.")
            }
        }
    }
}