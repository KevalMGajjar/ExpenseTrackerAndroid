package com.example.splitwiseclone.roomdb.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.splitwiseclone.roomdb.entities.Expense
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExpense(expense: Expense)

    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Update
    suspend fun updateExpense(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY id ASC")
    fun getAllExpenses(): Flow<List<Expense>>

    @Query("DELETE FROM expenses")
    suspend fun deleteAllExpenses()

    @Query("SELECT * FROM expenses WHERE participants LIKE '%' || :id || '%' AND participants LIKE '%' || :friendId || '%'")
    fun getAllRelatedExpenses(id: String, friendId: String): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    fun getExpenseById(id: String): Flow<Expense>

    @Query("SELECT * FROM expenses WHERE groupId = :groupId")
    fun getExpensesByGroupId(groupId: String): Flow<List<Expense>>

}