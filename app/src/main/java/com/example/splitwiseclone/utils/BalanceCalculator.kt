package com.example.splitwiseclone.ui_viewmodels

import com.example.splitwiseclone.roomdb.entities.Expense
import javax.inject.Inject
import javax.inject.Singleton

data class BalanceResult(
    val totalBalance: Double,
    val balancePerUser: Map<String, Double>
)

@Singleton
class BalanceCalculator @Inject constructor() {

    fun calculateBalances(expenses: List<Expense>, currentUserId: String): BalanceResult {
        val netBalances = mutableMapOf<String, Double>()
        expenses.forEach { expense ->
            expense.splits.forEach { split ->
                netBalances[split.owedToUserId] = (netBalances[split.owedToUserId] ?: 0.0) + split.owedAmount
                netBalances[split.owedByUserId] = (netBalances[split.owedByUserId] ?: 0.0) - split.owedAmount
            }
        }

        val currentUserTotalBalance = netBalances[currentUserId] ?: 0.0
        val balanceWithEachUser = mutableMapOf<String, Double>()
        expenses.forEach { expense ->
            expense.splits.forEach { split ->
                if (split.owedToUserId == currentUserId) {
                    val otherPersonId = split.owedByUserId
                    balanceWithEachUser[otherPersonId] = (balanceWithEachUser[otherPersonId] ?: 0.0) + split.owedAmount
                }
                else if (split.owedByUserId == currentUserId) {
                    val otherPersonId = split.owedToUserId
                    balanceWithEachUser[otherPersonId] = (balanceWithEachUser[otherPersonId] ?: 0.0) - split.owedAmount
                }
            }
        }

        return BalanceResult(
            totalBalance = currentUserTotalBalance,
            balancePerUser = balanceWithEachUser
        )
    }
}