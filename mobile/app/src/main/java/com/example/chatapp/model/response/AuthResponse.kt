package com.example.chatapp.model.response

/**
 * Data class for authentication tokens
 */
data class AuthResponse(
    val accessToken: String,
    val refreshToken: String
)
