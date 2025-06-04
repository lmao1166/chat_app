package com.example.chatapp.ui.home.profile

import android.annotation.SuppressLint
import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chatapp.model.response.UserResponse
import com.example.chatapp.repository.UserRepository
import com.example.chatapp.utils.FileUtils
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody

class EditProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = UserRepository()
    @SuppressLint("StaticFieldLeak")
    private val context = application.applicationContext

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val _updateSuccess = MutableLiveData<Boolean>()
    val updateSuccess: LiveData<Boolean> = _updateSuccess

    private val _updatedUser = MutableLiveData<UserResponse>()
    val updatedUser: LiveData<UserResponse> = _updatedUser
    @SuppressLint("NullSafeMutableLiveData")
    fun updateProfile(username: String, imageUri: Uri? = null) {
        viewModelScope.launch {
            _isLoading.value = true
            // Don't set error to null here, only set when there's actually an error

            try {
                // Prepare username
                val usernameBody = username.toRequestBody("text/plain".toMediaTypeOrNull())

                // Prepare image if provided
                var imagePart: MultipartBody.Part? = null
                if (imageUri != null) {
                    val imageFile = FileUtils.getFileFromUri(context, imageUri)
                    if (imageFile != null && imageFile.exists()) {
                        val requestFile = imageFile.asRequestBody("image/*".toMediaTypeOrNull())
                        imagePart = MultipartBody.Part.createFormData(
                            "profilePicture", 
                            imageFile.name, 
                            requestFile
                        )
                    }
                }

                // Call API (chỉ gửi username và ảnh, không gửi email)
                val response = repository.updateProfile(usernameBody, imagePart)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        _updatedUser.value = apiResponse.data!!
                        _updateSuccess.value = true
                    } else {
                        _error.value = apiResponse?.message ?: "Cập nhật thất bại"
                    }
                } else {
                    _error.value = "Lỗi kết nối: ${response.code()}"
                }

            } catch (e: Exception) {
                _error.value = "Lỗi: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}
