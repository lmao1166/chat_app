package com.example.chatapp.model.response

import com.google.gson.annotations.SerializedName

data class ConversationResponse(
    val id: Int,
    val type: String,
    val name: String?,
    val thumbnail: String?,
    @SerializedName("last_message_at")
    val lastMessageAt: String?,
    val createdAt: String,
    val updatedAt: String,
    val members: List<ConversationMember>,
    val lastMessage: LastMessage?
)

data class ConversationMember(
    val id: Int,
    val username: String,
    val email: String,
    val profilePicUrl: String?,
    @SerializedName("ConversationMember")
    val conversationMember: ConversationMemberDetails
)

data class ConversationMemberDetails(
    @SerializedName("joined_at")
    val joinedAt: String,
    @SerializedName("left_at")
    val leftAt: String?
)

data class LastMessage(
    val id: Int,
    val content: String,
    val timestamp: String,
    @SerializedName("message_type")
    val messageType: String,
    @SerializedName("attachment_url")
    val attachmentUrl: String?,
    val sender: MessageSender
)

data class MessageSender(
    val id: Int,
    val username: String,
    @SerializedName("profilePicUrl")
    val profilePicUrl: String?
)