package com.example.chatapp.repository

import com.example.chatapp.RetrofitInstance
import com.example.chatapp.model.request.MessageRequest
import com.example.chatapp.model.response.ApiResponse
import com.example.chatapp.model.response.MessageResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import retrofit2.Response

/**
 * Repository class that handles all message-related operations
 * Acts as a single source of truth for message data
 */
class MessageRepository {
    private val messageApi = RetrofitInstance.messageApi

    /**
     * Gets all messages for a conversation
     * @param conversationId ID of the conversation
     * @return API response with messages
     */
    suspend fun getMessagesByConversationId(conversationId: String): Response<ApiResponse<List<MessageResponse>>> {
        return messageApi.getMessagesByConversationId(conversationId)
    }

    /**
     * Sends a new message in a conversation
     * @param conversationId ID of the conversation
     * @param content Content of the message
     * @return API response with the created message
     */
    suspend fun sendMessage(conversationId: String, content: String): Response<ApiResponse<MessageResponse>> {
        val messageRequest = MessageRequest(
            content = content,
            conversationId = conversationId.toInt()
        )
        return messageApi.sendMessage(messageRequest)
    }

    /**
     * Deletes a message
     * @param messageId ID of the message to delete
     * @return API response indicating success/failure
     */
    suspend fun deleteMessage(messageId: String): Response<ApiResponse<Boolean>> {
        return messageApi.deleteMessage(messageId)
    }

    /**
     * Sends a new message with image attachment in a conversation
     * @param conversationId ID of the conversation
     * @param content Content of the message (can be empty for image-only messages)
     * @param imagePart MultipartBody.Part containing the image file
     * @return API response with the created message
     */
    suspend fun sendMessageWithImage(
        conversationId: String,
        content: String,
        imagePart: MultipartBody.Part
    ): Response<ApiResponse<MessageResponse>> {
        val conversationIdPart = conversationId.toRequestBody("text/plain".toMediaTypeOrNull())
        val contentPart = content.toRequestBody("text/plain".toMediaTypeOrNull())

        return messageApi.sendMessageWithImage(conversationIdPart, contentPart, imagePart)
    }
}