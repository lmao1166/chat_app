package com.example.chatapp.utils

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import com.example.chatapp.MainActivity
import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor xử lý các lỗi xác thực và tự động chuyển hướng người dùng về trang đăng nhập
 * khi token hết hạn (401 Unauthorized)
 */
class AuthInterceptor(private val context: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val response = chain.proceed(request)

        // Kiểm tra nếu response code là 401 (Unauthorized)
        if (response.code == 401) {
            // Xử lý token hết hạn trên main thread
            Handler(Looper.getMainLooper()).post {
                handleUnauthorized()
            }
        }

        return response
    }

    private fun handleUnauthorized() {
        // Hiển thị thông báo cho người dùng
        Toast.makeText(
            context,
            "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại",
            Toast.LENGTH_LONG
        ).show()

        // Xóa token
        TokenManager.getInstance(context).clearTokens()

        // Khởi động lại MainActivity với cờ xóa activity stack và chuyển về trang đăng nhập
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("navigate_to", "login")
        }
        context.startActivity(intent)
    }
}
