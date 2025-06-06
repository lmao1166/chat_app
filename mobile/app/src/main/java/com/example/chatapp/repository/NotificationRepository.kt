package com.example.chatapp.repository

import com.example.chatapp.RetrofitInstance
import com.example.chatapp.model.response.ApiResponse
import com.example.chatapp.model.response.NotificationResponse
import retrofit2.Response

class NotificationRepository {
    
    /**
     * Get user notifications with pagination
     */
    suspend fun getUserNotifications(
        page: Int = 1,
        limit: Int = 20,
        unreadOnly: Boolean = false
    ): Response<ApiResponse<List<NotificationResponse>>> {
        return RetrofitInstance.notificationApi.getUserNotifications(page, limit, unreadOnly)
    }

    /**
     * Get unread notifications count
     */
    suspend fun getUnreadCount(): Response<ApiResponse<Map<String, Int>>> {
        return RetrofitInstance.notificationApi.getUnreadCount()
    }

    /**
     * Mark a notification as read
     */
    suspend fun markAsRead(notificationId: String): Response<ApiResponse<NotificationResponse>> {
        return RetrofitInstance.notificationApi.markAsRead(notificationId)
    }

    /**
     * Mark all notifications as read
     */
    suspend fun markAllAsRead(): Response<ApiResponse<Boolean>> {
        return RetrofitInstance.notificationApi.markAllAsRead()
    }

    /**
     * Delete a notification
     */
    suspend fun deleteNotification(notificationId: String): Response<ApiResponse<Boolean>> {
        return RetrofitInstance.notificationApi.deleteNotification(notificationId)
    }
}
