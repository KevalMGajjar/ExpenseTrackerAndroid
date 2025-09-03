package com.example.splitwiseclone.roomdb.friends

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [Friend::class], version = 4, exportSchema = false)
abstract class FriendsDataBase: RoomDatabase() {

    abstract fun getDao(): FriendDao
}