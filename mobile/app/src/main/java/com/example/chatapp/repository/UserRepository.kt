package com.example.chatapp.repository

import com.example.chatapp.RetrofitInstance
import com.example.chatapp.model.request.ChangePasswordRequest
import com.example.chatapp.model.request.UserRequest
import com.example.chatapp.model.response.ApiResponse
import com.example.chatapp.model.response.UserResponse
import retrofit2.Response

class UserRepository {
    suspend fun register(userRequest: UserRequest): Response<ApiResponse<UserResponse>> {
        return RetrofitInstance.userApi.register(userRequest)
    }

    suspend fun changePassword(changePasswordRequest: ChangePasswordRequest): Response<ApiResponse<UserResponse>> {
        return RetrofitInstance.userApi.changePassword(changePasswordRequest)
    }
}