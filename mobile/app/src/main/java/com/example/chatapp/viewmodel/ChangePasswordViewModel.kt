package com.example.chatapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.chatapp.model.request.ChangePasswordRequest
import com.example.chatapp.repository.UserRepository
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ChangePasswordViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ChangePasswordUiState())
    val uiState: StateFlow<ChangePasswordUiState> = _uiState.asStateFlow()

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            try {
                // Tạo đối tượng ChangePasswordRequest để gọi API
                val request = ChangePasswordRequest(currentPassword, newPassword, confirmPassword)
                val response = userRepository.changePassword(request)

                if (response.isSuccessful) {
                    // Success case
                    val responseBody = response.body()
                    val successMessage = responseBody?.message ?: "Đổi mật khẩu thành công"
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        isSuccess = true,
                        successMessage = successMessage,
                        error = null
                    )
                } else {
                    // Error case - try to parse error response
                    var errorMessage = "Đã xảy ra lỗi"

                    try {
                        val errorBody = response.errorBody()?.string()
                        if (!errorBody.isNullOrEmpty()) {
                            // Try to parse JSON error response
                            val gson = Gson()
                            val jsonObject = gson.fromJson(errorBody, JsonObject::class.java)
                            errorMessage = jsonObject.get("message")?.asString ?: getDefaultErrorMessage(response.code())
                        } else {
                            errorMessage = getDefaultErrorMessage(response.code())
                        }
                    } catch (parseException: Exception) {
                        errorMessage = getDefaultErrorMessage(response.code())
                    }

                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = errorMessage
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Đã xảy ra lỗi không xác định"
                )
            }
        }
    }

    // Overloaded method để nhận ChangePasswordRequest object
    fun changePassword(request: ChangePasswordRequest) {
        changePassword(request.getPassword(), request.getNewPassword(), request.getConfirmPassword())
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    fun clearSuccess() {
        _uiState.value = _uiState.value.copy(successMessage = null)
    }

    fun resetState() {
        _uiState.value = ChangePasswordUiState()
    }

    // Helper method to get default error message based on status code
    private fun getDefaultErrorMessage(statusCode: Int): String {
        return when (statusCode) {
            400 -> "Dữ liệu không hợp lệ"
            401 -> "Không có quyền truy cập"
            404 -> "Người dùng không tồn tại"
            500 -> "Lỗi server"
            else -> "Đã xảy ra lỗi ($statusCode)"
        }
    }
}

data class ChangePasswordUiState(
    val isLoading: Boolean = false,
    val isSuccess: Boolean = false,
    val successMessage: String? = null,
    val error: String? = null
)

class ChangePasswordViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ChangePasswordViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ChangePasswordViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
