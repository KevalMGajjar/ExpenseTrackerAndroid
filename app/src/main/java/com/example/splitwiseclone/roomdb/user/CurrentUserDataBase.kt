package com.example.splitwiseclone.roomdb.user

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [CurrentUser::class], version = 2, exportSchema = false)
abstract class CurrentUserDataBase: RoomDatabase() {

    abstract fun getDao(): CurrentUserDao
}