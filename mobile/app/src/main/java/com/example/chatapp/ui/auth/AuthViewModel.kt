package com.example.chatapp.ui.auth

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.chatapp.utils.TokenManager

class AuthViewModel(application: Application) : AndroidViewModel(application) {
    
    private val tokenManager = TokenManager.getInstance(application)
    
    private val _isLoggedIn = MutableLiveData<Boolean>()
    val isLoggedIn: LiveData<Boolean> = _isLoggedIn
    
    init {
        checkLoginStatus()
    }
    
    fun checkLoginStatus() {
        _isLoggedIn.value = tokenManager.isLoggedIn()
    }
    
    fun logout() {
        tokenManager.clearTokens()
        _isLoggedIn.value = false
    }
    
    fun onLoginSuccess() {
        _isLoggedIn.value = true
    }
}
