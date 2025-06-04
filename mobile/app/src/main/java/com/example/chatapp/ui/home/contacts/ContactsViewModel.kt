package com.example.chatapp.ui.home.contacts

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chatapp.model.response.UserResponse
import com.example.chatapp.repository.UserRepository
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

class ContactsViewModel(application: Application) : AndroidViewModel(application) {
    private val userRepository = UserRepository()
    
    private val _contacts = MutableLiveData<List<UserResponse>>()
    val contacts: LiveData<List<UserResponse>> = _contacts
    
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading
    
    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    
    private val _filteredContacts = MutableLiveData<List<UserResponse>>()
    val filteredContacts: LiveData<List<UserResponse>> = _filteredContacts
    
    private var originalContacts: List<UserResponse> = emptyList()
    
    fun loadContacts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = "" // Clear previous errors
            try {
                Log.d("ContactsViewModel", "Starting to load contacts...")
                val response = userRepository.getAllUsers()
                Log.d("ContactsViewModel", "Response received: isSuccessful=${response.isSuccessful}, code=${response.code()}")
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    Log.d("ContactsViewModel", "API Response: $apiResponse")

                    if (apiResponse != null) {
                        val users = apiResponse.data ?: emptyList()
                        Log.d("ContactsViewModel", "Users received: ${users.size}")
                        users.forEachIndexed { index, user ->
                            Log.d("ContactsViewModel", "User $index - ID: ${user.id}, Username: ${user.username}, Email: ${user.email}")
                        }

                        originalContacts = users
                        _contacts.value = users
                        _filteredContacts.value = users
                        _error.value = "" // Use empty string instead of null
                        Log.d("ContactsViewModel", "Successfully loaded ${users.size} contacts")
                    } else {
                        _error.value = "Phản hồi từ server không hợp lệ"
                        Log.e("ContactsViewModel", "Response body is null")
                    }
                } else {
                    val errorMessage = when (response.code()) {
                        401 -> "Phiên đăng nhập đã hết hạn"
                        403 -> "Không có quyền truy cập"
                        404 -> "Không tìm thấy dữ liệu"
                        500 -> "Lỗi server"
                        else -> "Lỗi tải danh bạ: ${response.message()}"
                    }
                    _error.value = errorMessage
                    Log.e("ContactsViewModel", "Error loading contacts: ${response.code()} - ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("ContactsViewModel", "Exception during loadContacts", e)
                _error.value = "Lỗi không mong muốn: ${e.message}"
            } finally {
                _isLoading.value = false
                Log.d("ContactsViewModel", "Load contacts completed")
            }
        }
    }
    
    fun searchContacts(query: String) {
        val filtered = if (query.isEmpty()) {
            originalContacts
        } else {
            originalContacts.filter { user ->
                user.username?.contains(query, ignoreCase = true) == true ||
                user.email?.contains(query, ignoreCase = true) == true
            }
        }
        _filteredContacts.value = filtered
    }
    
    fun refreshContacts() {
        loadContacts()
    }
}
