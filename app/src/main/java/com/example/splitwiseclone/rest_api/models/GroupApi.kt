package com.example.splitwiseclone.rest_api.models

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
