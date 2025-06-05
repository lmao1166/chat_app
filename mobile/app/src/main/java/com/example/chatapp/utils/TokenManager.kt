package com.example.chatapp.utils

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONObject
import java.util.*

/**
 * Lớp này quản lý việc lưu trữ và truy xuất token xác thực.
 */
class TokenManager(context: Context) {

    companion object {
        private const val PREF_NAME = "AuthPrefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"

        @Volatile
        private var instance: TokenManager? = null

        fun getInstance(context: Context): TokenManager {
            return instance ?: synchronized(this) {
                instance ?: TokenManager(context.applicationContext).also { instance = it }
            }
        }
    }

    private val prefs: SharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)

    /**
     * Lưu access token và refresh token vào SharedPreferences.
     */
    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            apply()
        }
    }

    /**
     * Lấy access token đã lưu.
     * @return Access token hoặc null nếu chưa lưu.
     */
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * Lấy refresh token đã lưu.
     * @return Refresh token hoặc null nếu chưa lưu.
     */
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }    /**
     * Kiểm tra xem người dùng đã đăng nhập hay chưa và token còn hạn.
     * @return true nếu access token tồn tại và còn hạn, false nếu không.
     */
    fun isLoggedIn(): Boolean {
        val accessToken = getAccessToken()
        return accessToken != null && isTokenValid(accessToken)
    }

    /**
     * Kiểm tra token có còn hạn hay không bằng cách decode JWT
     * @param token JWT token cần kiểm tra
     * @return true nếu token còn hạn, false nếu hết hạn
     */
    private fun isTokenValid(token: String): Boolean {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.w("TokenManager", "Invalid JWT token format")
                return false
            }

            val payload = parts[1]
            val decodedBytes = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)
            val decodedString = String(decodedBytes)
            val jsonObject = JSONObject(decodedString)
            
            val exp = jsonObject.optLong("exp", 0)
            val currentTime = System.currentTimeMillis() / 1000
            
            Log.d("TokenManager", "Token exp: $exp, Current time: $currentTime")
            exp > currentTime
        } catch (e: Exception) {
            Log.e("TokenManager", "Error checking token validity", e)
            false
        }
    }

    /**
     * Xóa tất cả token khi đăng xuất.
     */
    fun clearTokens() {
        prefs.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            apply()
        }
    }

    /**
     * Lấy user ID từ access token đã lưu.
     * @return User ID hoặc null nếu không thể lấy được.
     */
    fun getUserId(): String? {
        val accessToken = getAccessToken()
        return if (accessToken != null) {
            extractUserIdFromToken(accessToken)
        } else {
            null
        }
    }

    /**
     * Trích xuất user ID từ JWT token
     * @param token JWT token
     * @return User ID hoặc null nếu không thể trích xuất được
     */
    private fun extractUserIdFromToken(token: String): String? {
        return try {
            val parts = token.split(".")
            if (parts.size != 3) {
                Log.w("TokenManager", "Invalid JWT token format")
                return null
            }

            val payload = parts[1]
            val decodedBytes = android.util.Base64.decode(payload, android.util.Base64.URL_SAFE)
            val decodedString = String(decodedBytes)
            val jsonObject = JSONObject(decodedString)
            
            val userId = jsonObject.optString("userId", null.toString())
            Log.d("TokenManager", "Extracted user ID: $userId")
            userId
        } catch (e: Exception) {
            Log.e("TokenManager", "Error extracting user ID from token", e)
            null
        }
    }
}
