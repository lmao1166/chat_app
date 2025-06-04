package com.example.chatapp

import android.content.Context
import com.example.chatapp.api.AuthApi
import com.example.chatapp.api.UserApi
import com.example.chatapp.utils.TokenManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitInstance {
    // Cấu hình cho các môi trường khác nhau
    private const val EMULATOR_URL = "http://10.0.2.2:3000/"      // Cho Android Emulator
    private const val REAL_DEVICE_URL = "http://192.168.1.10:3000/" // Cho thiết bị thật
    private const val LOCALHOST_URL = "http://localhost:3000/"      // Cho test local
    
    // Chọn URL phù hợp - thay đổi theo môi trường bạn đang dùng
    private const val BASE_URL = REAL_DEVICE_URL  // <-- Đang dùng cho Emulator
    
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // Khởi tạo client ban đầu với logging interceptor
    private var client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    // Khởi tạo retrofit với client ban đầu (không có token)
    private var retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // Cập nhật client và retrofit để sử dụng token
    fun updateWithToken(context: Context) {
        val tokenManager = TokenManager.getInstance(context)
        val authInterceptor = Interceptor { chain ->
            val originalRequest = chain.request()

            // Lấy access token từ TokenManager
            val accessToken = tokenManager.getAccessToken()

            // Nếu có token, thêm vào header
            val newRequest = if (!accessToken.isNullOrEmpty()) {
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $accessToken")
                    .build()
            } else {
                originalRequest
            }

            chain.proceed(newRequest)
        }

        // Tạo client mới với auth interceptor
        client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()

        // Cập nhật lại Retrofit với client mới
        retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }

    val userApi: UserApi by lazy {
        retrofit.create(UserApi::class.java)
    }
}
