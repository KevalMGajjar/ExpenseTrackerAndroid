package com.example.splitwiseclone.ui_viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.roomdb.entities.Expense
import com.example.splitwiseclone.roomdb.expense.ExpenseRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed interface ActivityItem {
    val timestamp: Long
    val date: Date
}

data class ExpenseActivity(val expense: Expense) : ActivityItem {
    override val date: Date = try {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(expense.expenseDate) ?: Date(0)
    } catch (e: Exception) {
        Date(0)
    }
    override val timestamp: Long = date.time
}

@HiltViewModel
class ActivityViewModel @Inject constructor(
    expenseRepository: ExpenseRepository
) : ViewModel() {

    val activities: StateFlow<List<ActivityItem>> = expenseRepository.getAllExpenses()
        .map { expenseList ->
            expenseList
                .map { expense -> ExpenseActivity(expense) }
                .sortedByDescending { it.timestamp }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList<ActivityItem>()
        )
}

