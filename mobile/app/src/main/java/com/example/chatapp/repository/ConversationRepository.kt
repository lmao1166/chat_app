package com.example.chatapp.repository

import com.example.chatapp.RetrofitInstance
import com.example.chatapp.model.request.ConversationRequest
import com.example.chatapp.model.response.ApiResponse
import com.example.chatapp.model.response.ConversationResponse
import retrofit2.Response

/**
 * Repository class that handles all conversation-related operations
 * Acts as a single source of truth for conversation data
 */
class ConversationRepository {
    private val conversationApi = RetrofitInstance.conversationApi

    /**
     * Gets all conversations for the current user
     * @return API response with user conversations
     */
    suspend fun getConversations(): Response<ApiResponse<List<ConversationResponse>>> {
        return conversationApi.getConversations()
    }

    /**
     * Gets or creates a conversation with another user
     * @param userId ID of the user to create conversation with
     * @return API response with the conversation
     */
    suspend fun getOrCreateConversation(userId: Int): Response<ApiResponse<ConversationResponse>> {
        val request = ConversationRequest(userId)
        return conversationApi.getOrCreateConversation(request)
    }
}