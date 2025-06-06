package com.example.chatapp.model.request

data class NotificationRequest(
    val type: String,
    val content: String,
    val relatedEntityId: Int? = null
)
