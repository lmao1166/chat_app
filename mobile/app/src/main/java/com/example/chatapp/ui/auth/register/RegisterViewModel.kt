package com.example.chatapp.ui.auth.register

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.model.request.UserRequest
import com.example.chatapp.model.response.ApiResponse
import com.example.chatapp.model.response.UserResponse
import com.example.chatapp.repository.UserRepository
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.launch
import retrofit2.Response

class RegisterViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _registrationStatus = MutableLiveData<RegistrationStatus>()
    val registrationStatus: LiveData<RegistrationStatus> = _registrationStatus

    // Hàm đăng ký người dùng
    fun register(username: String, email: String, password: String, confirmPassword: String) {
        // Đặt trạng thái là đang tải
        _registrationStatus.value = RegistrationStatus.Loading

        // Tạo request object
        val userRequest = UserRequest(username, email, password, confirmPassword)

        // Gọi API đăng ký trong coroutine scope
        viewModelScope.launch {
            try {
                val response = userRepository.register(userRequest)
                handleRegisterResponse(response)
            } catch (e: Exception) {
                _registrationStatus.value = RegistrationStatus.Error(e.message ?: "Đã xảy ra lỗi")
            }
        }
    }

    // Hàm xử lý response từ API
    private fun handleRegisterResponse(response: Response<ApiResponse<UserResponse>>) {
        if (response.isSuccessful) {
            val apiResponse = response.body()

            if (apiResponse != null && apiResponse.success) {
                // Đăng ký thành công
                _registrationStatus.value = RegistrationStatus.Success(apiResponse.message ?: "Đăng ký thành công")
            } else {
                // API trả về lỗi
                _registrationStatus.value = RegistrationStatus.Error(apiResponse?.message ?: "Đăng ký thất bại")
            }
        } else {
            // Lỗi HTTP
            var errorMessage = "Lỗi: ${response.code()}"

            // Thử parse error body để lấy thông tin chi tiết
            try {
                response.errorBody()?.string()?.let { errorBodyString ->
                    val gson = Gson()
                    val errorJson = gson.fromJson(errorBodyString, JsonObject::class.java)
                    errorMessage = errorJson.get("message")?.asString ?: errorMessage
                }
            } catch (e: Exception) {
                // Không parse được error body, giữ nguyên message lỗi
            }

            _registrationStatus.value = RegistrationStatus.Error(errorMessage)
        }
    }

    // Reset trạng thái đăng ký
    fun resetStatus() {
        _registrationStatus.value = RegistrationStatus.Initial
    }
}

// Sealed class đại diện cho các trạng thái đăng ký
sealed class RegistrationStatus {
    object Initial : RegistrationStatus()
    object Loading : RegistrationStatus()
    data class Success(val message: String) : RegistrationStatus()
    data class Error(val message: String) : RegistrationStatus()
}
