package com.example.chatapp.api

import com.example.chatapp.model.request.MessageRequest
import com.example.chatapp.model.response.ApiResponse
import com.example.chatapp.model.response.MessageResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface MessageApi {
    @GET("api/v1/messages/conversation/{conversationId}")
    suspend fun getMessagesByConversationId(@Path("conversationId") conversationId: String): Response<ApiResponse<List<MessageResponse>>>

    @POST("api/v1/messages")
    suspend fun sendMessage(@Body messageRequest: MessageRequest): Response<ApiResponse<MessageResponse>>

    @DELETE("api/v1/messages/{messageId}")
    suspend fun deleteMessage(@Path("messageId") messageId: String): Response<ApiResponse<Boolean>>
}