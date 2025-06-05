package com.example.chatapp.model.response

import com.google.gson.annotations.SerializedName

data class MessageResponse(
    val content: String,
    val timestamp: String,
    @SerializedName("attachment_url")
    val attachmentUrl: String?,
    val sender: MessageSender
)

data class MessageSender(
    val id: Int,
    val username: String,
    val email: String,
    val profilePicUrl: String?
)