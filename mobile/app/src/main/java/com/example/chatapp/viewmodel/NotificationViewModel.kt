package com.example.chatapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.model.response.NotificationResponse
import com.example.chatapp.repository.NotificationRepository
import kotlinx.coroutines.launch

class NotificationViewModel : ViewModel() {
    private val notificationRepository = NotificationRepository()

    private val _notifications = MutableLiveData<List<NotificationResponse>>()
    val notifications: LiveData<List<NotificationResponse>> = _notifications

    private val _unreadCount = MutableLiveData<Int>()
    val unreadCount: LiveData<Int> = _unreadCount

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    /**
     * Load user notifications
     */
    fun loadNotifications(page: Int = 1, unreadOnly: Boolean = false) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = ""

                val response = notificationRepository.getUserNotifications(page, 20, unreadOnly)
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            _notifications.value = apiResponse.data ?: emptyList()
                        } else {
                            _error.value = apiResponse.message ?: "Không thể tải thông báo"
                        }
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

    /**
     * Load unread notifications count
     */
    fun loadUnreadCount() {
        viewModelScope.launch {
            try {
                val response = notificationRepository.getUnreadCount()
                if (response.isSuccessful) {
                    response.body()?.let { apiResponse ->
                        if (apiResponse.success) {
                            val count = apiResponse.data?.get("count") ?: 0
                            _unreadCount.value = count
                        }
                    }
                }
            } catch (e: Exception) {
                // Silent fail for unread count
            }
        }
    }

    /**
     * Mark notification as read
     */
    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            try {
                val response = notificationRepository.markAsRead(notificationId)
                if (response.isSuccessful) {
                    // Update local list
                    val currentList = _notifications.value?.toMutableList() ?: mutableListOf()
                    val index = currentList.indexOfFirst { it.notification_id.toString() == notificationId }
                    if (index != -1) {
                        currentList[index] = currentList[index].copy(is_read = true)
                        _notifications.value = currentList
                    }
                    // Update unread count
                    loadUnreadCount()
                }
            } catch (e: Exception) {
                _error.value = "Không thể đánh dấu đã đọc: ${e.message}"
            }
        }
    }

    /**
     * Mark all notifications as read
     */
    fun markAllAsRead() {
        viewModelScope.launch {
            try {
                val response = notificationRepository.markAllAsRead()
                if (response.isSuccessful) {
                    // Update local list
                    val currentList = _notifications.value?.map { it.copy(is_read = true) } ?: emptyList()
                    _notifications.value = currentList
                    _unreadCount.value = 0
                }
            } catch (e: Exception) {
                _error.value = "Không thể đánh dấu tất cả đã đọc: ${e.message}"
            }
        }
    }

    /**
     * Delete notification
     */
    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            try {
                val response = notificationRepository.deleteNotification(notificationId)
                if (response.isSuccessful) {
                    // Remove from local list
                    val currentList = _notifications.value?.toMutableList() ?: mutableListOf()
                    currentList.removeAll { it.notification_id.toString() == notificationId }
                    _notifications.value = currentList
                    // Update unread count
                    loadUnreadCount()
                }
            } catch (e: Exception) {
                _error.value = "Không thể xóa thông báo: ${e.message}"
            }
        }
    }
}
