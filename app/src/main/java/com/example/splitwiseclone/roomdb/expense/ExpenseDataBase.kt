package com.example.splitwiseclone.roomdb.expense

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Expense::class], version = 2, exportSchema = true)
@TypeConverters(ExpenseDbConvertor::class)
abstract class ExpenseDataBase: RoomDatabase() {

    abstract fun getDao(): ExpenseDao
}