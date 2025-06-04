package com.example.chatapp.model.request

import kotlinx.serialization.Serializable

@Serializable
data class ProfileRequest(
    val username: String,
    val email: String? = null
)