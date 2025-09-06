package com.example.splitwiseclone.roomdb.expense

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.roomdb.entities.Expense
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ExpenseRoomViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository

): ViewModel() {

    private var _relatedExpenses = MutableStateFlow<List<Expense>>(emptyList())
    var relatedExpenses: StateFlow<List<Expense>> = _relatedExpenses

    val allExpenses: StateFlow<List<Expense>> = expenseRepository.getAllExpenses()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun insertExpense(expense: Expense, onSuccess: () -> Unit) {
        viewModelScope.launch {
            expenseRepository.insertExpense(expense)
            onSuccess()
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.deleteExpense(expense)
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            expenseRepository.updateExpense(expense)
        }
    }

    fun getAllRelatedExpenses(id: String, friendId: String) {

        viewModelScope.launch {
            expenseRepository.getAllRelatedExpenses(id, friendId).collect { expenses ->
                _relatedExpenses.value = expenses
            }
        }
    }

}