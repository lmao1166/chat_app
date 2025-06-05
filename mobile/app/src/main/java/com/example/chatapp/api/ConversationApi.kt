package com.example.chatapp.api

import com.example.chatapp.model.response.ApiResponse
import com.example.chatapp.model.response.ConversationResponse
import retrofit2.Response
import retrofit2.http.GET

interface ConversationApi {
    @GET("api/v1/conversations/user")
    suspend fun getConversations(): Response<ApiResponse<List<ConversationResponse>>>
}