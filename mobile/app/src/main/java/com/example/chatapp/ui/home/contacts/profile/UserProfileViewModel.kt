package com.example.chatapp.ui.home.contacts.profile

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.model.response.UserResponse
import com.example.chatapp.repository.UserRepository
import kotlinx.coroutines.launch

class UserProfileViewModel : ViewModel() {

    private val userRepository = UserRepository()
    private val tag = "UserProfileViewModel"

    private val _userData = MutableLiveData<UserResponse>()
    val userData: LiveData<UserResponse> = _userData

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun loadUserProfile(userId: String) {
        _isLoading.value = true
        _error.value = ""

        viewModelScope.launch {
            try {
                Log.d(tag, "Loading user profile for ID: $userId")
                val response = userRepository.findUserById(userId)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse != null && apiResponse.success) {
                        apiResponse.data?.let { user ->
                            Log.d(tag, "User data loaded: ${user.username}")
                            _userData.value = user
                        } ?: run {
                            _error.value = "Không tìm thấy thông tin người dùng"
                            Log.e(tag, "User data is null even though response was successful")
                        }
                    } else {
                        _error.value = apiResponse?.message ?: "Lỗi không xác định"
                        Log.e(tag, "API error: ${apiResponse?.message}")
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Phiên đăng nhập đã hết hạn"
                        403 -> "Không có quyền truy cập"
                        404 -> "Không tìm thấy người dùng"
                        else -> "Lỗi: ${response.code()}"
                    }
                    _error.value = errorMessage
                    Log.e(tag, "HTTP error: ${response.code()}")
                }
            } catch (e: Exception) {
                _error.value = "Lỗi kết nối: ${e.message}"
                Log.e(tag, "Exception during profile load", e)
            } finally {
                _isLoading.value = false
            }
        }
    }
}
