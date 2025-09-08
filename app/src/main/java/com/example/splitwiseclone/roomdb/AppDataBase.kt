package com.example.splitwiseclone.roomdb

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.splitwiseclone.roomdb.converters.ExpenseDbConvertor
import com.example.splitwiseclone.roomdb.converters.GroupDbConvertor
import com.example.splitwiseclone.roomdb.dao.*
import com.example.splitwiseclone.roomdb.entities.*

@Database(
    entities = [CurrentUser::class, Friend::class, Group::class, Expense::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(ExpenseDbConvertor::class, GroupDbConvertor::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun currentUserDao(): CurrentUserDao
    abstract fun friendDao(): FriendDao
    abstract fun groupDao(): GroupDao
    abstract fun expenseDao(): ExpenseDao

}