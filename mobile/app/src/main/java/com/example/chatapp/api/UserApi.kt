package com.example.chatapp.api

import com.example.chatapp.model.request.AuthRequest
import com.example.chatapp.model.request.UserRequest
import com.example.chatapp.model.response.ApiResponse
import com.example.chatapp.model.response.UserResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT

interface UserApi {
    @POST("api/v1/user")
    suspend fun register(@Body userRequest: UserRequest): Response<ApiResponse<UserResponse>>

    @PUT("api/v1/user")
    suspend fun changeUserProfile(@Body authRequest: AuthRequest): Response<ApiResponse<UserResponse>>

    @PUT("api/v1/user/password")
    suspend fun changePassword(@Body authRequest: AuthRequest): Response<ApiResponse<UserResponse>>

    @GET("api/v1/user/{id}")
    suspend fun findUserById(@Body id: String): Response<ApiResponse<UserResponse>>

}