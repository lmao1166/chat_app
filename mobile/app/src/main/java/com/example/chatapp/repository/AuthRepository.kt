package com.example.chatapp.repository

import com.example.chatapp.RetrofitInstance
import com.example.chatapp.model.request.AuthRequest
import com.example.chatapp.model.response.ApiResponse
import com.example.chatapp.model.response.AuthResponse
import com.example.chatapp.model.response.UserResponse
import retrofit2.Response

class AuthRepository {

    suspend fun login(email: String, password: String): Response<ApiResponse<AuthResponse>> {
        val authRequest = AuthRequest(email, password)
        return RetrofitInstance.authApi.login(authRequest)
    }

    suspend fun getCurrentUser() : Response<ApiResponse<UserResponse>> {
        return RetrofitInstance.authApi.getCurrentUser()
    }

    suspend fun logout() : Response<ApiResponse<Any?>> {
        return RetrofitInstance.authApi.logout()
    }
}
