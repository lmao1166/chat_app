package com.example.chatapp.api

import com.example.chatapp.model.request.AuthRequest
import com.example.chatapp.model.request.ChangePasswordRequest
import com.example.chatapp.model.request.ProfileRequest
import com.example.chatapp.model.request.UserRequest
import com.example.chatapp.model.response.ApiResponse
import com.example.chatapp.model.response.UserResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Part
import retrofit2.http.Path

interface UserApi {
    @POST("api/v1/users")
    suspend fun register(@Body userRequest: UserRequest): Response<ApiResponse<UserResponse>>

    @Multipart
    @PUT("api/v1/users")
    suspend fun updateProfile(
        @Part("username") username: RequestBody? = null,
        @Part profilePicture: MultipartBody.Part? = null
    ): Response<ApiResponse<UserResponse>>

    @PUT("api/v1/users/password")
    suspend fun changePassword(@Body changePasswordRequest: ChangePasswordRequest): Response<ApiResponse<Any>>

    @GET("api/v1/users/{id}")
    suspend fun findUserById(@Path("id") id: Int): Response<ApiResponse<UserResponse>>

    @GET("api/v1/users")
    suspend fun getAllUsers(): Response<ApiResponse<List<UserResponse>>>

}