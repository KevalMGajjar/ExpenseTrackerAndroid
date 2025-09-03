package com.example.splitwiseclone.rest_api.models

import com.example.splitwiseclone.roomdb.groups.Member

data class GroupApi(
    var groupName: String,
    var profilePicture: String,
    val groupCreatedByUserId: String,
    var isArchived: Boolean,
    val groupType: String
)

data class Member(
    val userId: String?,
    val role: String
)
