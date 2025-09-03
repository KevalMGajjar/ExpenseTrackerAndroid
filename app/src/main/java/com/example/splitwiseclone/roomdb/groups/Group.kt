package com.example.splitwiseclone.roomdb.groups

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Groups")
data class Group(
    @PrimaryKey val id: String,
    var groupName: String,
    var profilePicture: String,
    val groupCreatedByUserId: String,
    var isArchived: Boolean,
    var members: List<Member>?,
    val groupType: String
)

data class Member(
    val userId: String?,
    val role: String,
    val username: String,
    val email: String,
    val profilePicture: String
)
