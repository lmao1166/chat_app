package com.example.chatapp.repository

import com.example.chatapp.RetrofitInstance
import com.example.chatapp.model.request.ChangePasswordRequest
import com.example.chatapp.model.request.UserRequest
import com.example.chatapp.model.response.ApiResponse
import com.example.chatapp.model.response.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response

class UserRepository {
    suspend fun register(userRequest: UserRequest): Response<ApiResponse<UserResponse>> {
        return RetrofitInstance.userApi.register(userRequest)
    }

    suspend fun updateProfile(
        username: RequestBody? = null,
        profilePicture: MultipartBody.Part? = null
    ): Response<ApiResponse<UserResponse>> {
        return RetrofitInstance.userApi.updateProfile(username, profilePicture)
    }

    suspend fun changePassword(changePasswordRequest: ChangePasswordRequest): Response<ApiResponse<Any>> {
        return RetrofitInstance.userApi.changePassword(changePasswordRequest)
    }

    suspend fun getAllUsers(): Response<ApiResponse<List<UserResponse>>> {
        return RetrofitInstance.userApi.getAllUsers()
    }

    suspend fun findUserById(userId: Int): Response<ApiResponse<UserResponse>> {
        return RetrofitInstance.userApi.findUserById(userId)
    }
}