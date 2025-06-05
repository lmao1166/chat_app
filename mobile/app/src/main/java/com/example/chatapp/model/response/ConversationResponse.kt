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
    val members: List<ConversationMember>
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