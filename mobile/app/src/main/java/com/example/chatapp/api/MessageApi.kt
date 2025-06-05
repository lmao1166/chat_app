package com.example.chatapp.api

import com.example.chatapp.model.response.ApiResponse
import com.example.chatapp.model.response.MessageResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface MessageApi {
    @GET("api/v1/messages/conversation/{conversationId}")
    suspend fun getMessagesByConversationId(@Path("conversationId") conversationId: String): Response<ApiResponse<List<MessageResponse>>>
}