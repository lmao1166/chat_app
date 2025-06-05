package com.example.chatapp.ui.home.chat

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.example.chatapp.model.response.ConversationResponse
import com.example.chatapp.model.response.MessageResponse
import com.example.chatapp.model.response.MessageSender
import com.example.chatapp.repository.ConversationRepository
import com.example.chatapp.repository.MessageRepository
import com.example.chatapp.utils.SocketManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * ViewModel for handling chat functionalities including conversations and messages
 */
// Thay đổi từ ViewModel sang AndroidViewModel để có thể truy cập context
class ChatViewModel(application: Application) : AndroidViewModel(application) {
    private val conversationRepository = ConversationRepository()
    private val messageRepository = MessageRepository()
    private val socketManager = SocketManager.getInstance()
    @SuppressLint("StaticFieldLeak")
    private val context: Context = application.applicationContext

    // Thông tin người dùng hiện tại
    private var currentUserId: String = ""
    private var currentUserName: String = ""
    private var currentUserProfilePic: String = ""

    // LiveData for conversations
    private val _conversations = MutableLiveData<List<ConversationResponse>>()
    val conversations: LiveData<List<ConversationResponse>> = _conversations

    // LiveData for messages
    private val _messages = MutableLiveData<List<MessageResponse>>()
    val messages: LiveData<List<MessageResponse>> = _messages

    // LiveData for the current conversation
    private val _currentConversation = MutableLiveData<ConversationResponse?>()
    val currentConversation: LiveData<ConversationResponse?> = _currentConversation

    // Loading and error states
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // Real-time features
    private val _typingUsers = MutableLiveData<List<String>>()
    val typingUsers: LiveData<List<String>> = _typingUsers

    private val _onlineUsers = MutableLiveData<List<String>>()
    val onlineUsers: LiveData<List<String>> = _onlineUsers

    private var currentConversationId: String? = null

    init {
        // Initialize Socket.IO connection
        socketManager.connect(context) // Truyền context vào phương thức connect
        setupSocketListeners()
    }

    private fun setupSocketListeners() {
        // Listen for new messages
        socketManager.addMessageListener { messageData ->
            viewModelScope.launch {
                try {
                    // Convert JSON to MessageResponse and add to current messages
                    val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
                    
                    // Create MessageResponse from socket data
                    val newMessage = MessageResponse(
                        content = messageData.optString("content", ""),
                        timestamp = messageData.optString("createdAt", getCurrentTimestamp()),
                        attachmentUrl = messageData.optString("attachmentUrl", null.toString()),
                        sender = MessageSender(
                            id = messageData.optJSONObject("sender")?.optInt("id") ?: 0,
                            username = messageData.optJSONObject("sender")?.optString("username") ?: "",
                            email = messageData.optJSONObject("sender")?.optString("email") ?: "",
                            profilePicUrl = messageData.optJSONObject("sender")?.optString("profilePicUrl")
                        )
                    )
                    
                    currentMessages.add(newMessage)
                    _messages.postValue(currentMessages)
                    
                    Log.d("ChatViewModel", "Received new message via Socket.IO: ${newMessage.content}")
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error processing new message from socket", e)
                }
            }
        }

        // Listen for typing indicators
        socketManager.onTyping { typingData ->
            viewModelScope.launch {
                try {
                    val username = typingData.optString("username", "")
                    val isTyping = typingData.optBoolean("isTyping", false)

                    val currentTyping = _typingUsers.value?.toMutableList() ?: mutableListOf()

                    if (isTyping && !currentTyping.contains(username)) {
                        currentTyping.add(username)
                    } else if (!isTyping) {
                        currentTyping.remove(username)
                    }

                    _typingUsers.postValue(currentTyping)
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error processing typing indicator", e)
                }
            }
        }

        // Listen for user status updates
        socketManager.onUserStatus { statusData ->
            viewModelScope.launch {
                try {
                    val username = statusData.optString("username", "")
                    val isOnline = statusData.optBoolean("isOnline", false)

                    val currentOnline = _onlineUsers.value?.toMutableList() ?: mutableListOf()

                    if (isOnline && !currentOnline.contains(username)) {
                        currentOnline.add(username)
                    } else if (!isOnline) {
                        currentOnline.remove(username)
                    }

                    _onlineUsers.postValue(currentOnline)
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error processing user status update", e)
                }
            }
        }
    }

    // Helper function to get current timestamp
    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        return sdf.format(Date())
    }

    /**
     * Loads all conversations for the current user
     */
    fun loadConversations() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = ""

            try {
                val response = conversationRepository.getConversations()

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        _conversations.value = apiResponse.data ?: emptyList()
                    } else {
                        _error.value = apiResponse?.message ?: "Unknown error"
                    }
                } else {
                    _error.value = "Error ${response.code()}: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error loading conversations", e)
                _error.value = e.localizedMessage ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Loads or creates a conversation with another user
     * @param userId ID of the user to start conversation with
     */
    fun loadOrCreateConversation(userId: Int) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = ""

            try {
                val response = conversationRepository.getOrCreateConversation(userId)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        _currentConversation.value = apiResponse.data

                        // Load messages for this conversation
                        loadMessages(apiResponse.data.id)
                    } else {
                        _error.value = apiResponse?.message ?: "Unknown error"
                        _isLoading.value = false
                    }
                } else {
                    _error.value = "Error ${response.code()}: ${response.message()}"
                    _isLoading.value = false
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error creating conversation", e)
                _error.value = e.localizedMessage ?: "Unknown error"
                _isLoading.value = false
            }
        }
    }

    /**
     * Loads messages for a specific conversation
     * @param conversationId ID of the conversation
     */
    fun loadMessages(conversationId: Comparable<*>?) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = ""

            try {
                val response = messageRepository.getMessagesByConversationId(conversationId.toString())

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        _messages.value = apiResponse.data ?: emptyList()
                    } else {
                        _error.value = apiResponse?.message ?: "Unknown error"
                    }
                } else {
                    _error.value = "Error ${response.code()}: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error loading messages", e)
                _error.value = e.localizedMessage ?: "Unknown error"
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Cập nhật thông tin người dùng hiện tại (thường gọi sau khi đăng nhập)
     */
    fun updateCurrentUserInfo(userId: String, username: String, profilePicUrl: String) {
        currentUserId = userId
        currentUserName = username
        currentUserProfilePic = profilePicUrl

        // Cập nhật thông tin người dùng vào SocketManager
        socketManager.updateCurrentUserData(userId, username, profilePicUrl)
        Log.d("ChatViewModel", "Updated current user info: $username, avatar: $profilePicUrl")
    }

    /**
     * Sends a message in the current conversation using Socket.IO for real-time delivery
     * @param conversationId ID of the conversation
     * @param content Message content
     */
    fun sendMessage(conversationId: Comparable<*>?, content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            try {
                // First send via Socket.IO for real-time delivery with user info
                if (currentUserId.isNotEmpty() && currentUserName.isNotEmpty()) {
                    // Gửi tin nhắn với thông tin đầy đủ của người dùng bao gồm ảnh đại diện
                    socketManager.sendMessage(
                        conversationId.toString(),
                        content,
                        currentUserId,
                        currentUserName,
                        currentUserProfilePic
                    )
                    Log.d("ChatViewModel", "Sent message via socket with profile pic: $currentUserProfilePic")
                } else {
                    // Fallback nếu chưa có thông tin người dùng
                    socketManager.sendMessage(conversationId.toString(), content)
                    Log.d("ChatViewModel", "Sent message via socket without user info")
                }

                // Also send via REST API as backup and for persistence
                val response = messageRepository.sendMessage(conversationId.toString(), content)

                if (!response.isSuccessful) {
                    // If REST API fails, show error but message might still be delivered via Socket.IO
                    Log.w("ChatViewModel", "REST API send failed but Socket.IO might have succeeded: ${response.code()}")
                    _error.value = "Message sent but may not be persisted properly"
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
                _error.value = e.localizedMessage ?: "Unknown error"
            }
        }
    }

    /**
     * Disconnect from Socket.IO when ViewModel is cleared
     */
    override fun onCleared() {
        super.onCleared()
        leaveConversation()
        socketManager.disconnect()
    }

    /**
     * Join a conversation room for real-time messaging
     */
    fun joinConversation(conversationId: String) {
        currentConversationId = conversationId
        socketManager.joinConversation(conversationId)
        Log.d("ChatViewModel", "Joined conversation: $conversationId")
    }

    /**
     * Leave current conversation room
     */
    fun leaveConversation() {
        currentConversationId?.let { conversationId ->
            socketManager.leaveConversation(conversationId)
            Log.d("ChatViewModel", "Left conversation: $conversationId")
        }
        currentConversationId = null
    }

    /**
     * Send typing indicator
     */
    fun sendTyping(isTyping: Boolean) {
        currentConversationId?.let { conversationId ->
            socketManager.sendTyping(conversationId, isTyping)
        }
    }
}


