package com.example.chatapp.model.response

import com.google.gson.annotations.SerializedName

/**
 * Generic API response class that can hold different types of data
 * @param T data type specific to the response
 */
data class ApiResponse<T>(
    val status: Int,
    val success: Boolean,
    val message: String,
    @SerializedName("data") val data: T?
)
