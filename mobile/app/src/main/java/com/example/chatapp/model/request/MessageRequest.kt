package com.example.chatapp.model.request

import com.google.gson.annotations.SerializedName

data class MessageRequest(
    val content: String,
    @SerializedName("conversation_id")
    val conversationId: Int
)
