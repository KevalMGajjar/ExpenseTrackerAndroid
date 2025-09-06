package com.example.splitwiseclone.ui_viewmodels

import com.example.splitwiseclone.roomdb.entities.Expense
import javax.inject.Inject
import javax.inject.Singleton

// A data class to hold the results of our calculation
data class BalanceResult(
    val totalBalance: Double, // The current user's total balance
    val balancePerUser: Map<String, Double> // Each other user's balance relative to the current user
)

@Singleton
class BalanceCalculator @Inject constructor() {

    /**
     * Calculates all balances from a list of expenses relative to a specific user.
     * This is the single source of truth for all balance calculations.
     */
    fun calculateBalances(expenses: List<Expense>, currentUserId: String): BalanceResult {
        // Step 1: Calculate the net balance of every single person involved in these expenses.
        // A positive balance means they are owed money overall.
        // A negative balance means they owe money overall.
        val netBalances = mutableMapOf<String, Double>()
        expenses.forEach { expense ->
            expense.splits.forEach { split ->
                // The person who is owed gets a positive credit.
                netBalances[split.owedToUserId] = (netBalances[split.owedToUserId] ?: 0.0) + split.owedAmount
                // The person who owes gets a negative debit.
                netBalances[split.owedByUserId] = (netBalances[split.owedByUserId] ?: 0.0) - split.owedAmount
            }
        }

        // Step 2: The current user's total balance is their net balance from the map.
        val currentUserTotalBalance = netBalances[currentUserId] ?: 0.0

        // Step 3: FIX - Calculate each individual's balance *relative to the current user*.
        // The previous logic was flawed. This new logic correctly calculates the
        // one-on-one balance by looking at every transaction that occurred
        // between the current user and each other person.
        val balanceWithEachUser = mutableMapOf<String, Double>()
        expenses.forEach { expense ->
            expense.splits.forEach { split ->
                // Case 1: Another user owes the current user in this split.
                if (split.owedToUserId == currentUserId) {
                    val otherPersonId = split.owedByUserId
                    balanceWithEachUser[otherPersonId] = (balanceWithEachUser[otherPersonId] ?: 0.0) + split.owedAmount
                }
                // Case 2: The current user owes another user in this split.
                else if (split.owedByUserId == currentUserId) {
                    val otherPersonId = split.owedToUserId
                    balanceWithEachUser[otherPersonId] = (balanceWithEachUser[otherPersonId] ?: 0.0) - split.owedAmount
                }
                // Important: Splits that do not involve the current user (e.g., between two other friends in a group)
                // are correctly ignored here, as they do not affect the direct balance.
            }
        }

        return BalanceResult(
            totalBalance = currentUserTotalBalance,
            balancePerUser = balanceWithEachUser
        )
    }
}