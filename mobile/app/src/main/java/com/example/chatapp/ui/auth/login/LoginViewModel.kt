package com.example.chatapp.ui.auth.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.model.response.ApiResponse
import com.example.chatapp.model.response.AuthResponse
import com.example.chatapp.repository.AuthRepository
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.json.JSONObject

class LoginViewModel : ViewModel() {

    private val repository = AuthRepository()
    private val tag = "LoginViewModel"

    private val _loginResult = MutableLiveData<LoginResult>()
    val loginResult: LiveData<LoginResult> = _loginResult

    fun login(email: String, password: String) {
        viewModelScope.launch {
            try {
                val response = repository.login(email, password)
                Log.d(tag, "Login API response: ${response.body()}")
                if (response.isSuccessful) {
                    // Log the raw response to check what's coming from API
                    val rawJson = response.body().toString()
                    Log.d(tag, "Raw API response: $rawJson")

                    response.body()?.let {
                        if (it.success && it.data != null) {
                            // Log specific token values
                            Log.d(tag, "Access Token: ${it.data.accessToken}")
                            Log.d(tag, "Refresh Token: ${it.data.refreshToken}")

                            _loginResult.value = LoginResult.Success(it)
                        } else {
                            _loginResult.value = LoginResult.Error(it.message)
                        }
                    } ?: run {
                        Log.e(tag, "Empty response body despite successful API call")
                        _loginResult.value = LoginResult.Error("Empty response body")
                    }
                } else {
                    // Xử lý lỗi một cách cụ thể
                    val errorBody = response.errorBody()?.string()
                    Log.e(tag, "Login failed: ${response.code()}, Error: $errorBody")

                    // Phân tích nội dung lỗi để hiển thị thông báo phù hợp
                    val errorMessage = getErrorMessage(response.code(), errorBody)
                    _loginResult.value = LoginResult.Error(errorMessage)
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception during login", e)
                _loginResult.value = LoginResult.Error("Lỗi kết nối: ${e.message}")
            }
        }
    }

    /**
     * Phân tích nội dung lỗi từ API để trả về thông báo phù hợp
     */
    private fun getErrorMessage(statusCode: Int, errorBody: String?): String {
        // Cố gắng phân tích thông báo lỗi từ API nếu có
        if (!errorBody.isNullOrEmpty()) {
            try {
                val jsonObject = JSONObject(errorBody)
                if (jsonObject.has("message")) {
                    return jsonObject.getString("message")
                }
            } catch (e: Exception) {
                Log.e(tag, "Error parsing error body", e)
            }
        }

        // Nếu không parse được hoặc không có message, dùng mã lỗi để xác định thông báo
        return when (statusCode) {
            400 -> "Thông tin đăng nhập không hợp lệ"
            401 -> "Tên đăng nhập hoặc mật khẩu không chính xác"
            403 -> "Tài khoản không có quyền truy cập"
            404 -> "Tài khoản không tồn tại"
            500 -> "Lỗi máy chủ, vui lòng thử lại sau"
            else -> "Đăng nhập thất bại: mã lỗi $statusCode"
        }
    }

    sealed class LoginResult {
        data class Success(val data: ApiResponse<AuthResponse>) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }
}
