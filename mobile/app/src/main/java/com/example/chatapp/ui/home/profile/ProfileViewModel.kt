package com.example.chatapp.ui.home.profile

import android.annotation.SuppressLint
import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chatapp.RetrofitInstance
import com.example.chatapp.model.response.ApiResponse
import com.example.chatapp.model.response.UserResponse
import com.example.chatapp.repository.AuthRepository
import com.example.chatapp.utils.TokenManager
import kotlinx.coroutines.launch
import retrofit2.Response
import java.io.IOException

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AuthRepository()
    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    private val _userProfile = MutableLiveData<UserResponse?>()
    val userProfile: MutableLiveData<UserResponse?> = _userProfile

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _logoutStatus = MutableLiveData<Boolean>()
    val logoutStatus: LiveData<Boolean> = _logoutStatus

    init {
        fetchUserProfile()
    }

    fun fetchUserProfile() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Đảm bảo token được cập nhật trước khi gọi API
                RetrofitInstance.updateWithToken(context)
                val response = repository.getCurrentUser()
                handleUserProfileResponse(response)
            } catch (e: Exception) {
                _error.value = e.message ?: "Không thể tải thông tin người dùng"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun logout() {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                // Đảm bảo cập nhật token mới nhất vào OkHttp client trước khi gọi API logout
                RetrofitInstance.updateWithToken(context)

                // Kiểm tra token trước khi gọi API
                val tokenManager = TokenManager.getInstance(context)
                val accessToken = tokenManager.getAccessToken()

                if (accessToken.isNullOrEmpty()) {
                    // Không có token, xử lý như đã đăng xuất
                    _error.value = "Không tìm thấy token đăng nhập"
                    _logoutStatus.value = true  // Vẫn xem như đăng xuất thành công
                    return@launch
                }

                // Log token để debug (không nên để trong production)
                Log.d("ProfileViewModel", "Using token: ${accessToken.take(10)}...")

                val response = repository.logout()
                handleLogoutResponse(response)
            } catch (e: Exception) {
                // Log chi tiết lỗi để debug
                Log.e("ProfileViewModel", "Logout error", e)
                _logoutStatus.value = false
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun handleUserProfileResponse(response: Response<ApiResponse<UserResponse>>) {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.success) {
                _userProfile.value = body.data
            } else {
                _error.value = body?.message ?: "Không thể tải thông tin người dùng"
            }
        } else {
            _error.value = "Lỗi: ${response.code()} - ${response.message()}"
        }
    }

    private fun handleLogoutResponse(response: Response<ApiResponse<Any?>>) {
        if (response.isSuccessful) {
            val body = response.body()
            if (body != null && body.success) {
                // Không cần quan tâm đến body.data vì có thể là null
                _logoutStatus.value = true
            } else {
                _error.value = body?.message ?: "Đăng xuất thất bại"
                _logoutStatus.value = false
            }
        } else {
            _error.value = "Lỗi: ${response.code()} - ${response.message()}"
            _logoutStatus.value = false
        }
    }
}
