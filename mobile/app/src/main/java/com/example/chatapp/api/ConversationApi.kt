package com.example.chatapp.api

import com.example.chatapp.model.request.ConversationRequest
import com.example.chatapp.model.response.ApiResponse
import com.example.chatapp.model.response.ConversationResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ConversationApi {
    @GET("api/v1/conversations/user")
    suspend fun getConversations(): Response<ApiResponse<List<ConversationResponse>>>

    @POST("api/v1/conversations")
    suspend fun getOrCreateConversation(@Body request: ConversationRequest): Response<ApiResponse<ConversationResponse>>
}

