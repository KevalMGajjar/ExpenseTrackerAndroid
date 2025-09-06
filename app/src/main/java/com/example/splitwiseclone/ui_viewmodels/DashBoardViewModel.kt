package com.example.splitwiseclone.ui_viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.splitwiseclone.roomdb.entities.Expense
import com.example.splitwiseclone.roomdb.expense.ExpenseRepository
import com.example.splitwiseclone.roomdb.friends.FriendRepository
import com.example.splitwiseclone.roomdb.entities.Group
import com.example.splitwiseclone.roomdb.groups.GroupRepository
import com.example.splitwiseclone.roomdb.entities.CurrentUser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject
import kotlin.collections.map

// --- Data classes to hold the processed data for the UI ---

data class MonthlyExpenseSummary(
    val month: String,
    val year: Int,
    val totalSpending: Double,
    val balance: Double
)

data class GroupBalanceSummary(
    val group: Group,
    val userBalanceInGroup: Double
)

// FIX: This new state holds all three required balance types.
data class DashboardBalances(
    val totalBalance: Double = 0.0,
    val totalYouOwe: Double = 0.0,
    val totalYouAreOwed: Double = 0.0
)

data class HomeUiState(
    val balances: DashboardBalances = DashboardBalances(),
    val monthlySummaries: List<MonthlyExpenseSummary> = emptyList(),
    val groupSummaries: List<GroupBalanceSummary> = emptyList(),
    val isLoading: Boolean = true
)

@HiltViewModel
class DashBoardViewModel @Inject constructor(
    private val expenseRepository: ExpenseRepository,
    private val groupRepository: GroupRepository,
    private val friendRepository: FriendRepository, // Added to get friend data
    private val balanceCalculator: BalanceCalculator
) : ViewModel() {

    fun getUiState(currentUser: CurrentUser): StateFlow<HomeUiState> {
        val expensesFlow = expenseRepository.getAllExpenses()
        val groupsFlow = groupRepository.allGroups
        val friendsFlow = friendRepository.allFriends

        return combine(expensesFlow, groupsFlow, friendsFlow) { expenses, groups, friends ->

            // FIX: Use the BalanceCalculator to get the TRUE total balance across ALL expenses.
            val overallBalanceResult = balanceCalculator.calculateBalances(expenses, currentUser.currentUserId)

            var totalYouOwe = 0.0
            var totalYouAreOwed = 0.0

            // Iterate through the per-user balances to calculate totals.
            overallBalanceResult.balancePerUser.values.forEach { balance ->
                if (balance < 0) {
                    totalYouOwe += balance
                } else {
                    totalYouAreOwed += balance
                }
            }

            val dashboardBalances = DashboardBalances(
                totalBalance = overallBalanceResult.totalBalance,
                totalYouOwe = totalYouOwe,
                totalYouAreOwed = totalYouAreOwed
            )

            val monthlySummaries = processExpensesByMonth(expenses, currentUser.currentUserId)
            val groupSummaries = processGroupBalances(groups, expenses, currentUser.currentUserId)

            HomeUiState(
                balances = dashboardBalances,
                monthlySummaries = monthlySummaries,
                groupSummaries = groupSummaries,
                isLoading = false
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = HomeUiState()
        )
    }

    private fun processExpensesByMonth(expenses: List<Expense>, currentUserId: String): List<MonthlyExpenseSummary> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        return expenses
            .groupBy { expense ->
                try {
                    calendar.time = dateFormat.parse(expense.expenseDate) ?: return@groupBy "Unknown"
                    "${calendar.get(Calendar.YEAR)}-${String.format("%02d", calendar.get(Calendar.MONTH))}"
                } catch (e: Exception) { "Unknown" }
            }
            .mapNotNull { (yearMonth, monthExpenses) ->
                if (yearMonth == "Unknown") return@mapNotNull null

                calendar.time = SimpleDateFormat("yyyy-MM", Locale.getDefault()).parse(yearMonth.substring(0, 7))!!
                val monthName = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT, Locale.getDefault())!!
                val year = calendar.get(Calendar.YEAR)

                val totalSpending = monthExpenses.sumOf { it.totalExpense }
                val userBalance = monthExpenses.sumOf { calculateUserBalanceForExpense(it, currentUserId) }

                MonthlyExpenseSummary(month = monthName, year = year, totalSpending = totalSpending, balance = userBalance)
            }
            .sortedBy { it.year * 100 + getMonthNumber(it.month) }
    }

    private fun processGroupBalances(groups: List<Group>, allExpenses: List<Expense>, currentUserId: String): List<GroupBalanceSummary> {
        val expensesByGroupId = allExpenses.groupBy { it.groupId }
        return groups.map { group ->
            val groupExpenses = expensesByGroupId[group.id] ?: emptyList()
            val userBalance = groupExpenses.sumOf { calculateUserBalanceForExpense(it, currentUserId) }
            GroupBalanceSummary(group = group, userBalanceInGroup = userBalance)
        }
    }

    private fun calculateUserBalanceForExpense(expense: Expense, currentUserId: String): Double {
        var balance = 0.0
        balance += expense.splits.filter { it.owedToUserId == currentUserId }.sumOf { it.owedAmount }
        balance -= expense.splits.filter { it.owedByUserId == currentUserId }.sumOf { it.owedAmount }
        return balance
    }

    private fun getMonthNumber(month: String): Int {
        return when (month.lowercase()) {
            "jan" -> 1; "feb" -> 2; "mar" -> 3; "apr" -> 4; "may" -> 5; "jun" -> 6;
            "jul" -> 7; "aug" -> 8; "sep" -> 9; "oct" -> 10; "nov" -> 11; "dec" -> 12
            else -> 0
        }
    }
}