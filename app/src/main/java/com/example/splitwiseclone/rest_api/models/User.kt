package com.example.splitwiseclone.rest_api.models

import com.squareup.moshi.JsonClass


@JsonClass(generateAdapter = true)

data class User(
    val username: String,
    val email: String,
    val password: String,
    val currencyCode: String,
    val profileUrl: String? = null,
    val phoneNumber: String?
)