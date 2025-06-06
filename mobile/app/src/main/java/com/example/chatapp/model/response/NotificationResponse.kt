package com.example.chatapp.model.response

data class NotificationResponse(
    val notification_id: Int,
    val user_id: Int,
    val type: String,
    val content: String,
    val is_read: Boolean,
    val related_entity_id: Int?,
    val created_at: String,
    val updated_at: String
)
