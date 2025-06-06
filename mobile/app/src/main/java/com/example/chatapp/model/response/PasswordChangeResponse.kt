package com.example.chatapp.model.response

/**
 * Specific response class for password change operation
 * This can handle both object and primitive value responses
 */
data class PasswordChangeResponse(
    val status: Int,
    val success: Boolean,
    val message: String,
    val data: Any? = null
)
