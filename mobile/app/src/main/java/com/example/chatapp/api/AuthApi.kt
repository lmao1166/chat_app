package com.example.chatapp.api

import com.example.chatapp.model.request.AuthRequest
import com.example.chatapp.model.response.ApiResponse
import com.example.chatapp.model.response.AuthResponse
import com.example.chatapp.model.response.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface AuthApi {
    @POST("api/v1/auth/login")
    suspend fun login(@Body authRequest: AuthRequest): Response<ApiResponse<AuthResponse>>

    @GET("api/v1/auth")
    suspend fun getCurrentUser(): Response<ApiResponse<UserResponse>>

    @POST("api/v1/auth/logout")
    suspend fun logout(): Response<ApiResponse<Any?>>
}
