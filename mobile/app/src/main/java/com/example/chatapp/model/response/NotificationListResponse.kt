package com.example.chatapp.model.response

/**
 * Response wrapper for notification list with pagination
 */
data class NotificationListResponse(
    val notifications: List<NotificationResponse>,
    val pagination: PaginationResponse?
)

data class PaginationResponse(
    val currentPage: Int,
    val totalPages: Int,
    val totalCount: Int,
    val hasNextPage: Boolean,
    val hasPrevPage: Boolean
)
