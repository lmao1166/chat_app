package com.example.chatapp.ui.auth.login

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.model.response.ApiResponse
import com.example.chatapp.model.response.AuthResponse
import com.example.chatapp.repository.AuthRepository
import kotlinx.coroutines.launch

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
                    // Try to get error message from response
                    val errorBody = response.errorBody()?.string()
                    Log.e(tag, "Login failed: ${response.code()}, Error: $errorBody")
                    _loginResult.value = LoginResult.Error("Login failed: ${response.code()}")
                }
            } catch (e: Exception) {
                Log.e(tag, "Exception during login", e)
                _loginResult.value = LoginResult.Error("Network error: ${e.message}")
            }
        }
    }

    sealed class LoginResult {
        data class Success(val data: ApiResponse<AuthResponse>) : LoginResult()
        data class Error(val message: String) : LoginResult()
    }
}
