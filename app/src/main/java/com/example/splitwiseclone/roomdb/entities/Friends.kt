package com.example.splitwiseclone.roomdb.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Friends")
data class Friend(
    @PrimaryKey val id: String,
    val username: String?,
    val email: String,
    val phoneNumber: String?,
    val profilePic: String,
    val balanceWithUser: Double = 0.0,
    val currentUserId: String,
    val friendId: String
)
