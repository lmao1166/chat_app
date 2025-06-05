package com.example.chatapp.repository

import com.example.chatapp.RetrofitInstance
import com.example.chatapp.model.request.ConversationRequest
import com.example.chatapp.model.response.ApiResponse
import com.example.chatapp.model.response.ConversationResponse
import retrofit2.Response

class ConversationRepository {
    suspend fun getOrCreateConversation(userId2: Int): Response<ApiResponse<ConversationResponse>> {
        return RetrofitInstance.conversationApi.getOrCreateConversation(ConversationRequest(userId2))
    }

    suspend fun getConversations(): Response<ApiResponse<List<ConversationResponse>>> {
        return RetrofitInstance.conversationApi.getConversations()
    }
}
