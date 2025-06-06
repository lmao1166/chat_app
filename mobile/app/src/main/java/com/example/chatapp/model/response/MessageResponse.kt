package com.example.chatapp.model.response

import com.google.gson.annotations.SerializedName

data class MessageResponse(
    val id: Int? = null,
    val content: String,
    val timestamp: String,
    @SerializedName("attachment_url")
    val attachmentUrl: String?,
    val sender: MessageSender,
    @SerializedName("message_type")
    val messageType: String? = "TEXT", // TEXT, IMAGE, FILE, etc.
    @SerializedName("delivery_status")
    val deliveryStatus: String? = "SENT", // SENT, DELIVERED, READ
    @SerializedName("is_deleted")
    val isDeleted: Boolean? = false
)

data class MessageSender(
    val id: Int,
    val username: String,
    val email: String,
    val profilePicUrl: String?
)