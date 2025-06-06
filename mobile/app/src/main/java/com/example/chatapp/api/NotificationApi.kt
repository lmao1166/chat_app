package com.example.chatapp.api

import com.example.chatapp.model.request.NotificationRequest
import com.example.chatapp.model.response.ApiResponse
import com.example.chatapp.model.response.NotificationResponse
import retrofit2.Response
import retrofit2.http.*

interface NotificationApi {
      @GET("api/v1/notifications")
    suspend fun getUserNotifications(
        @Query("page") page: Int = 1,
        @Query("limit") limit: Int = 20,
        @Query("unreadOnly") unreadOnly: Boolean = false
    ): Response<ApiResponse<List<NotificationResponse>>>
    
    @GET("api/v1/notifications/unread-count")
    suspend fun getUnreadCount(): Response<ApiResponse<Map<String, Int>>>
    
    @PUT("api/v1/notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") notificationId: String
    ): Response<ApiResponse<NotificationResponse>>
    
    @PUT("api/v1/notifications/mark-all-read")
    suspend fun markAllAsRead(): Response<ApiResponse<Boolean>>
    
    @DELETE("api/v1/notifications/{id}")
    suspend fun deleteNotification(
        @Path("id") notificationId: String
    ): Response<ApiResponse<Boolean>>
}
