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
import com.example.chatapp.model.response.LastMessage
import com.example.chatapp.repository.ConversationRepository
import com.example.chatapp.repository.MessageRepository
import com.example.chatapp.utils.SocketManager
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

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

    // Track processed message IDs to prevent duplicates
    private val processedMessageIds = mutableSetOf<String>()

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
        // Listen for connection changes
        socketManager.addConnectionListener { isConnected ->
            viewModelScope.launch {
                if (isConnected) {
                    Log.d("ChatViewModel", "Socket reconnected")
                    // Rejoin current conversation if exists
                    currentConversationId?.let { conversationId ->
                        Log.d("ChatViewModel", "Rejoining conversation after reconnect: $conversationId")
                        socketManager.joinConversation(conversationId)

                        // Sync messages để không bị miss message trong thời gian disconnect
                        syncMessagesWhenReconnected(conversationId)
                    }
                } else {
                    Log.d("ChatViewModel", "Socket disconnected")
                }
            }
        }        // Listen for new messages từ người khác (không phải tin nhắn của mình)
        socketManager.addMessageListener { messageData ->
            viewModelScope.launch {
                try {
                    val messageConversationId = messageData.optString("conversationId", "")
                    val senderId = messageData.optJSONObject("sender")?.optInt("id", -1) ?: -1
                    val senderIdString = senderId.toString()

                    // Lấy message ID từ server nếu có
                    val messageId = messageData.optString("id", "")
                    val messageContent = messageData.optString("content", "")
                    val messageTimestamp = messageData.optString("createdAt", getCurrentTimestamp())

                    // Tạo unique key để track duplicate
                    val messageKey = if (messageId.isNotEmpty()) {
                        "id_$messageId"
                    } else {
                        "${senderId}_${messageContent}_${parseTimestamp(messageTimestamp)}"
                    }

                    // Skip if we've already processed this message
                    if (processedMessageIds.contains(messageKey)) {
                        Log.d("ChatViewModel", "Skipping already processed message: $messageKey")
                        return@launch
                    }

                    // Add to processed IDs
                    processedMessageIds.add(messageKey)
                    // Prevent the set from growing too large
                    if (processedMessageIds.size > 200) {
                        // Remove oldest 50 entries
                        val toRemove = processedMessageIds.take(50)
                        processedMessageIds.removeAll(toRemove.toSet())
                    }

                    // Chỉ thêm tin nhắn nếu không phải từ current user
                    if (senderIdString != currentUserId) {
                        val newMessage = MessageResponse(
                            id = if (messageId.isNotEmpty()) messageId.toIntOrNull() else null,
                            content = messageContent,
                            timestamp = messageTimestamp,
                            attachmentUrl = messageData.optString("attachment_url", null),
                            messageType = messageData.optString("message_type", "TEXT"),                            sender = MessageSender(
                                id = senderId,
                                username = messageData.optJSONObject("sender")?.optString("username") ?: "Unknown",
                                profilePicUrl = messageData.optJSONObject("sender")?.optString("profilePicUrl")
                            )
                        )

                        // Always update conversation list for any new message
                        updateConversationWithNewMessage(messageConversationId, newMessage)

                        // Only add to current chat if it's the active conversation
                        if (messageConversationId == currentConversationId) {
                            val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()

                            // Double check - tìm tin nhắn duplicate trong danh sách hiện tại
                            val existingMessage = currentMessages.find { message ->
                                // Check by ID first if available
                                if (messageId.isNotEmpty() && message.id?.toString() == messageId) {
                                    true
                                } else {
                                    // Fallback to content + sender + timestamp check
                                    message.content == messageContent &&
                                            message.sender.id == senderId &&
                                            abs(parseTimestamp(message.timestamp) - parseTimestamp(messageTimestamp)) < 3000 // 3 giây tolerance
                                }
                            }

                            if (existingMessage == null) {
                                currentMessages.add(newMessage)
                                // Sort messages by timestamp to maintain order
                                currentMessages.sortBy { parseTimestamp(it.timestamp) }
                                _messages.postValue(currentMessages)

                                Log.d("ChatViewModel", "Added new message from Socket.IO to current chat: ${newMessage.content} (ID: $messageId)")
                            } else {
                                Log.d("ChatViewModel", "Duplicate message detected and skipped: $messageContent")
                            }
                        } else {
                            Log.d("ChatViewModel", "Updated conversation list for message from different conversation: $messageConversationId")
                        }
                    } else {
                        Log.d("ChatViewModel", "Skipping own message from Socket.IO: $messageContent")
                    }
                } catch (e: Exception) {
                    Log.e("ChatViewModel", "Error processing new message from socket", e)
                }
            }
        }// Listen for typing indicators
        socketManager.onTyping { typingData ->
            viewModelScope.launch {
                try {
                    // KIỂM TRA CONVERSATION CONTEXT cho typing events
                    val typingConversationId = typingData.optString("conversationId", "")
                    if (typingConversationId.isNotEmpty() && typingConversationId != currentConversationId) {
                        Log.d("ChatViewModel", "Typing indicator from different conversation, ignoring")
                        return@launch
                    }

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
                    Log.e("ChatViewModel", "Error processing user status update", e)                }
            }
        }
    }    // Helper function to get current timestamp
    private fun getCurrentTimestamp(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdf.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return sdf.format(Date())
    }// Utility method to parse timestamps
    private fun parseTimestamp(timestamp: String): Long {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            dateFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
            dateFormat.parse(timestamp)?.time ?: 0L
        } catch (e: Exception) {
            Log.w("ChatViewModel", "Failed to parse timestamp: $timestamp", e)
            0L
        }
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
                        val conversations = apiResponse.data ?: emptyList()

                        // Sort conversations by most recent message timestamp (newest first)
                        val sortedConversations = conversations.sortedByDescending { conversation ->
                            conversation.lastMessage?.timestamp?.let { parseTimestamp(it) } ?: 0L
                        }

                        _conversations.value = sortedConversations
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
    }    /**
     * Cập nhật thông tin người dùng hiện tại (thường gọi sau khi đăng nhập)
     */
    fun updateCurrentUserInfo(userId: String, username: String, profilePicUrl: String) {
        currentUserId = userId
        currentUserName = username
        currentUserProfilePic = profilePicUrl

        // Cập nhật thông tin người dùng vào SocketManager
        socketManager.updateCurrentUserData(userId, username, profilePicUrl)
        Log.d("ChatViewModel", "Updated current user info: $username, avatar: $profilePicUrl")
    }    /**
     * Sends a message in the current conversation - REST API first, Socket.IO second
     * @param conversationId ID of the conversation
     * @param content Message content
     */
    fun sendMessage(conversationId: Comparable<*>?, content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            try {
                // 1. GỬI QUA REST API TRƯỚC (Primary method for persistence)
                val response = messageRepository.sendMessage(conversationId.toString(), content)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        val newMessage = apiResponse.data

                        // Add message ID to processed set to prevent duplicate from Socket.IO
                        newMessage.id?.let { messageId ->
                            processedMessageIds.add("id_$messageId")
                        }                        // Cập nhật UI ngay lập tức với tin nhắn từ server
                        val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
                        currentMessages.add(newMessage)
                        // Sort to maintain chronological order
                        currentMessages.sortBy { parseTimestamp(it.timestamp) }
                        _messages.value = currentMessages

                        // Cập nhật conversation list với tin nhắn mới đã gửi
                        updateConversationWithNewMessage(conversationId.toString(), newMessage)

                        Log.d("ChatViewModel", "Message sent via REST API: ${newMessage.content} (ID: ${newMessage.id})")

                        // 2. GỬI QUA SOCKET.IO (Secondary, for real-time to other users)
                        // Note: Don't emit Socket.IO for own messages to prevent duplicate
                        try {
                            if (currentUserId.isNotEmpty() && currentUserName.isNotEmpty()) {
                                // Include message ID in Socket.IO data
                                socketManager.sendMessage(
                                    conversationId.toString(),
                                    content,
                                    currentUserId,
                                    currentUserName,
                                    currentUserProfilePic,
                                    newMessage.id?.toString() // Pass message ID
                                )
                                Log.d("ChatViewModel", "Message also sent via Socket.IO for real-time")
                            }
                        } catch (socketError: Exception) {
                            Log.w("ChatViewModel", "Socket send failed, but REST API succeeded", socketError)
                            // Không cần xử lý lỗi socket vì REST API đã thành công
                        }

                    } else {
                        _error.value = apiResponse?.message ?: "Không thể gửi tin nhắn"
                    }
                } else {
                    _error.value = "Lỗi gửi tin nhắn: ${response.code()}"
                }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message via REST API", e)
                _error.value = "Không thể gửi tin nhắn: ${e.localizedMessage}"
            }
        }
    }

    /**
     * Sends a message with image attachment in the current conversation
     * @param conversationId ID of the conversation
     * @param content Message content (can be empty for image-only messages)
     * @param imagePart MultipartBody.Part containing the image
     */
    fun sendMessageWithImage(conversationId: Comparable<*>?, content: String, imagePart: okhttp3.MultipartBody.Part) {
        viewModelScope.launch {
            try {
                // Send message with image via REST API
                val response = messageRepository.sendMessageWithImage(conversationId.toString(), content, imagePart)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        val newMessage = apiResponse.data

                        // Add message ID to processed set to prevent duplicate from Socket.IO
                        newMessage.id?.let { messageId ->
                            processedMessageIds.add("id_$messageId")
                        }                        // Update UI immediately with message from server
                        val currentMessages = _messages.value?.toMutableList() ?: mutableListOf()
                        currentMessages.add(newMessage)
                        // Sort to maintain chronological order
                        currentMessages.sortBy { parseTimestamp(it.timestamp) }
                        _messages.value = currentMessages

                        // Cập nhật conversation list với tin nhắn có hình ảnh mới đã gửi
                        updateConversationWithNewMessage(conversationId.toString(), newMessage)

                        Log.d("ChatViewModel", "Message with image sent via REST API: ${newMessage.content} (ID: ${newMessage.id})")

                        // Note: Socket.IO typically doesn't handle file uploads, so we rely on REST API for image messages
                        // Other users will get the message through periodic refresh or WebSocket notifications

                    } else {
                        _error.value = apiResponse?.message ?: "Không thể gửi tin nhắn với hình ảnh"
                    }
                } else {
                    _error.value = "Lỗi gửi tin nhắn với hình ảnh: ${response.code()}"
                }

            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message with image via REST API", e)
                _error.value = "Không thể gửi tin nhắn với hình ảnh: ${e.localizedMessage}"
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
    }    /**
     * Join a conversation room for real-time messaging
     */
    fun joinConversation(conversationId: String) {
        Log.d("ChatViewModel", "Attempting to join conversation: $conversationId")

        // Leave current conversation first if exists
        leaveConversation()

        // Clear processed message IDs để tránh miss message mới
        processedMessageIds.clear()

        // Set new conversation ID BEFORE joining
        currentConversationId = conversationId

        // Join new conversation room
        socketManager.joinConversation(conversationId)

        Log.d("ChatViewModel", "Successfully joined conversation: $conversationId")
    }

    /**
     * Leave current conversation room
     */
    fun leaveConversation() {
        currentConversationId?.let { conversationId ->
            socketManager.leaveConversation(conversationId)
            Log.d("ChatViewModel", "Left conversation: $conversationId")
        }

        // Reset current conversation ID
        currentConversationId = null

        // Clear typing users when leaving conversation
        _typingUsers.postValue(emptyList())
    }

    /**
     * Send typing indicator
     */
    fun sendTyping(isTyping: Boolean) {
        currentConversationId?.let { conversationId ->
            socketManager.sendTyping(conversationId, isTyping)
        }
    }

    /**
     * Sync messages when reconnected to avoid missing messages
     */
    private fun syncMessagesWhenReconnected(conversationId: String) {
        viewModelScope.launch {
            try {
                // Reload messages from server to get any missed messages
                val response = messageRepository.getMessagesByConversationId(conversationId)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        val serverMessages = apiResponse.data ?: emptyList()
                        val currentMessages = _messages.value ?: emptyList()

                        // Find new messages that we don't have locally
                        val newMessages = serverMessages.filter { serverMessage ->
                            currentMessages.none { localMessage ->
                                localMessage.content == serverMessage.content &&
                                        localMessage.sender.id == serverMessage.sender.id &&
                                        abs(parseTimestamp(localMessage.timestamp) - parseTimestamp(serverMessage.timestamp)) < 5000
                            }
                        }

                        if (newMessages.isNotEmpty()) {
                            val updatedMessages = (currentMessages + newMessages).sortedBy { parseTimestamp(it.timestamp) }
                            _messages.value = updatedMessages
                            Log.d("ChatViewModel", "Synced ${newMessages.size} new messages")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error syncing messages", e)
            }
        }
    }

    /**
     * Debug method to check current connection and conversation state
     */
    fun debugConnectionState() {
        Log.d("ChatViewModel", "=== DEBUG CONNECTION STATE ===")
        Log.d("ChatViewModel", "Current conversation ID: $currentConversationId")
        Log.d("ChatViewModel", "Socket connected: ${socketManager.isConnected()}")
        Log.d("ChatViewModel", "Processed messages count: ${processedMessageIds.size}")
        Log.d("ChatViewModel", "Current messages count: ${_messages.value?.size ?: 0}")
        Log.d("ChatViewModel", "===========================")
    }

    /**
     * Force rejoin current conversation (for debugging)
     */
    fun forceRejoinConversation() {
        currentConversationId?.let { conversationId ->
            Log.d("ChatViewModel", "Force rejoining conversation: $conversationId")
            socketManager.leaveConversation(conversationId)
            socketManager.joinConversation(conversationId)
        }
    }

    /**
     * Updates the conversation list with a new message, making it the most recent
     * @param conversationId ID of the conversation
     * @param newMessage The new message to update with
     */
    private fun updateConversationWithNewMessage(conversationId: String, newMessage: MessageResponse) {
        val currentConversations = _conversations.value?.toMutableList() ?: return

        // Find the conversation to update
        val conversationIndex = currentConversations.indexOfFirst {
            it.id.toString() == conversationId
        }

        if (conversationIndex != -1) {
            val conversation = currentConversations[conversationIndex]            // Create updated conversation with new last message
            val updatedConversation = conversation.copy(
                lastMessage = LastMessage(
                    id = newMessage.id ?: 0,
                    content = newMessage.content,
                    timestamp = newMessage.timestamp,
                    messageType = newMessage.messageType ?: "TEXT",
                    attachmentUrl = newMessage.attachmentUrl,
                    sender = newMessage.sender
                )
            )

            // Replace the conversation at the same index
            currentConversations[conversationIndex] = updatedConversation

            // Sort conversations by most recent message timestamp (newest first)
            currentConversations.sortByDescending { conversation ->
                conversation.lastMessage?.timestamp?.let { parseTimestamp(it) } ?: 0L
            }

            // Update the conversations LiveData
            _conversations.postValue(currentConversations)

            Log.d("ChatViewModel", "Updated conversation $conversationId with new message: ${newMessage.content}")
        } else {
            Log.w("ChatViewModel", "Conversation $conversationId not found in current conversation list")
        }
    }

    /**
     * Refreshes the conversation list to get latest messages from server
     * This helps catch any messages we might have missed from other conversations
     */
    fun refreshConversationList() {
        viewModelScope.launch {
            try {
                val response = conversationRepository.getConversations()

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        val newConversations = apiResponse.data ?: emptyList()

                        // Sort by most recent message timestamp
                        val sortedConversations = newConversations.sortedByDescending { conversation ->
                            conversation.lastMessage?.timestamp?.let { parseTimestamp(it) } ?: 0L
                        }

                        _conversations.value = sortedConversations
                        Log.d("ChatViewModel", "Refreshed conversation list with ${sortedConversations.size} conversations")
                    }
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error refreshing conversation list", e)
            }
        }
    }

    /**
     * Called when user switches to a different conversation
     * Updates the conversation list and handles real-time setup
     */
    fun switchToConversation(conversationId: String) {
        // Join the new conversation for real-time updates
        joinConversation(conversationId)

        // Refresh conversation list to get latest state
        refreshConversationList()

        Log.d("ChatViewModel", "Switched to conversation: $conversationId")
    }
}

