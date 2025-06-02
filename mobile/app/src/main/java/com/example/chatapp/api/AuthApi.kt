package com.example.chatapp.api

import com.example.chatapp.model.request.AuthRequest
import com.example.chatapp.model.response.ApiResponse
import com.example.chatapp.model.response.AuthResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body authRequest: AuthRequest): Response<ApiResponse<AuthResponse>>
}
