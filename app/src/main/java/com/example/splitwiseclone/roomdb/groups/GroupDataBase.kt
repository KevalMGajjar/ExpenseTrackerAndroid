package com.example.splitwiseclone.roomdb.groups

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(entities = [Group::class], version = 3, exportSchema = false)
@TypeConverters(GroupDbConvertor::class)
abstract class GroupDataBase : RoomDatabase(){

    abstract fun getDao(): GroupDao
}