package com.example.splitwiseclone.roomdb.expense

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class ExpenseRepository @Inject constructor(
    private val dao: ExpenseDao
){

    suspend fun insertExpense(expense: Expense) {
        dao.insertExpense(expense)
    }

    suspend fun deleteExpense(expense: Expense) {
        dao.deleteExpense(expense)
    }

    suspend fun updateExpense(expense: Expense) {
        dao.updateExpense(expense)
    }

    fun getAllExpenses(): Flow<List<Expense>> {
        return dao.getAllExpenses()
    }

    suspend fun deleteAllExpenses() {
        dao.deleteAllExpenses()
    }

    fun getAllRelatedExpenses(id: String, friendId: String): Flow<List<Expense>> {
        return dao.getAllRelatedExpenses(id, friendId)
    }

    fun getExpenseById(id: String): Flow<Expense> {
        return dao.getExpenseById(id)
    }

}