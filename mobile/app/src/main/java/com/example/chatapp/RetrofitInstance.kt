package com.example.chatapp

import com.example.chatapp.api.AuthApi
import okhttp3.OkHttpClient
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

    private val client = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .retryOnConnectionFailure(true)
        .build()

    private val retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    
    val authApi: AuthApi by lazy {
        retrofit.create(AuthApi::class.java)
    }
}
