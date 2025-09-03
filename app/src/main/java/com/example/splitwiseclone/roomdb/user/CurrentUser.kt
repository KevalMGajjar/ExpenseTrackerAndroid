package com.example.splitwiseclone.roomdb.user

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "CurrentUser")
data class CurrentUser(
    @PrimaryKey val currentUserId: String,
    val username: String,
    val email: String,
    val hashedPassword: String,
    val currencyCode: String,
    val profileUrl: String? = null,
    var phoneNumber: String?
)