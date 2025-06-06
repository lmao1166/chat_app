package com.example.chatapp.utils

import android.content.Context
import android.util.Log
import io.socket.client.IO
import io.socket.client.Socket
import io.socket.emitter.Emitter
import org.json.JSONObject
import java.net.URISyntaxException

/**
 * Singleton class to manage Socket.IO connection for real-time messaging
 */
class SocketManager private constructor() {
    companion object {
        private const val TAG = "SocketManager"
        private const val SERVER_URL = "http://192.168.1.10:3000"
        
        @Volatile
        private var INSTANCE: SocketManager? = null
        
        fun getInstance(): SocketManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SocketManager().also { INSTANCE = it }
            }
        }
    }
    
    private var socket: Socket? = null
    private var isConnected = false
    private var currentConversationId: Int? = null

    // Thêm biến để lưu thông tin người dùng hiện tại bao gồm ảnh đại diện
    private var currentUserData: JSONObject? = null

    // Listeners for real-time events
    private val messageListeners = mutableListOf<(message: JSONObject) -> Unit>()
    private val userStatusListeners = mutableListOf<(userId: String, isOnline: Boolean) -> Unit>()
    private val typingListeners = mutableListOf<(userId: String, isTyping: Boolean) -> Unit>()
    private val connectionListeners = mutableListOf<(isConnected: Boolean) -> Unit>()

    /**
     * Initialize socket connection with authentication token
     */
    fun connect(context: Context) {
        if (isConnected && socket != null) {
            Log.d(TAG, "Socket already connected")
            return
        }
        
        val tokenManager = TokenManager.getInstance(context)
        val token = tokenManager.getAccessToken()

        if (token.isNullOrEmpty()) {
            Log.e(TAG, "No token available for socket connection")
            return
        }
        
        try {
            val options = IO.Options()
            options.auth = mapOf("token" to token)
            options.reconnection = true
            options.reconnectionAttempts = 5
            options.reconnectionDelay = 1000
            options.timeout = 10000
            
            socket = IO.socket(SERVER_URL, options)
            
            setupEventListeners()
            socket?.connect()
            
            Log.d(TAG, "Attempting to connect to $SERVER_URL")
            
        } catch (e: URISyntaxException) {
            Log.e(TAG, "Failed to create socket connection", e)
        }
    }
    
    /**
     * Disconnect from socket server
     */
    fun disconnect() {
        socket?.disconnect()
        socket?.off()
        socket = null
        isConnected = false
        currentConversationId = null
        Log.d(TAG, "Socket disconnected")
        
        // Notify listeners about disconnection
        connectionListeners.forEach { it(false) }
    }
      /**
     * Join a conversation room for real-time messaging
     */
    fun joinConversation(conversationId: Int) {
        Log.d(TAG, "Attempting to join conversation: $conversationId")
        
        if (!isConnected) {
            Log.w(TAG, "Socket not connected, storing conversation ID for later join")
            currentConversationId = conversationId
            return
        }
        
        // Leave current conversation if any
        currentConversationId?.let { currentId ->
            if (currentId != conversationId) {
                socket?.emit("leave_conversation", currentId)
                Log.d(TAG, "Left previous conversation: $currentId")
            }
        }
        
        // Join new conversation
        socket?.emit("join_conversation", conversationId)
        currentConversationId = conversationId
        Log.d(TAG, "Joined conversation: $conversationId")
    }
    
    /**
     * Join a conversation room for real-time messaging with string conversationId
     */
    fun joinConversation(conversationId: String) {
        try {
            joinConversation(conversationId.toInt())
        } catch (e: NumberFormatException) {
            Log.e(TAG, "Invalid conversation ID: $conversationId", e)
        }
    }

    /**
     * Leave current conversation room
     */
    fun leaveConversation() {
        currentConversationId?.let { conversationId ->
            socket?.emit("leave_conversation", conversationId)
            currentConversationId = null
            Log.d(TAG, "Left conversation: $conversationId")
        }
    }
    
    /**
     * Leave specific conversation room
     */
    fun leaveConversation(conversationId: String) {
        try {
            val convId = conversationId.toInt()
            socket?.emit("leave_conversation", convId)
            if (currentConversationId == convId) {
                currentConversationId = null
            }
            Log.d(TAG, "Left conversation: $convId")
        } catch (e: NumberFormatException) {
            Log.e(TAG, "Invalid conversation ID: $conversationId", e)
        }
    }

    /**
     * Send a message through socket
     */
    fun sendMessage(conversationId: String, content: String) {
        if (socket == null) {
            Log.e(TAG, "Socket is null, cannot send message")
            return
        }

        try {
            val messageData = JSONObject().apply {
                put("conversationId", conversationId.toInt())
                put("content", content)

                // Thêm thông tin người dùng hiện tại nếu có
                currentUserData?.let { userData ->
                    val senderData = JSONObject().apply {
                        put("id", userData.optString("userId"))
                        put("username", userData.optString("userName"))
                        put("profilePicUrl", userData.optString("avatarUrl"))
                    }
                    put("sender", senderData)
                }
            }

            Log.d(TAG, "Sending message via socket: $messageData")
            socket?.emit("send_message", messageData)
            Log.d(TAG, "Message sent via socket: $content with sender info")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message via socket", e)
        }
    }

    /**
     * Send a message with detailed user information
     */
    fun sendMessage(conversationId: String, content: String, userId: String, userName: String, profilePicUrl: String) {
        if (socket == null) {
            Log.e(TAG, "Socket is null, cannot send message")
            return
        }

        try {
            val senderData = JSONObject().apply {
                put("id", userId)
                put("username", userName)
                put("profilePicUrl", profilePicUrl)
            }

            val messageData = JSONObject().apply {
                put("conversationId", conversationId.toInt())
                put("content", content)
                put("sender", senderData)
            }

            Log.d(TAG, "Sending message via socket with user info: $messageData")
            socket?.emit("send_message", messageData)
            Log.d(TAG, "Message sent via socket with complete sender info: $content")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message via socket", e)
        }
    }

    /**
     * Send a message with detailed user information and message ID
     */
    fun sendMessage(conversationId: String, content: String, userId: String, userName: String, profilePicUrl: String, messageId: String?) {
        if (socket == null) {
            Log.e(TAG, "Socket is null, cannot send message")
            return
        }

        try {
            val senderData = JSONObject().apply {
                put("id", userId)
                put("username", userName)
                put("profilePicUrl", profilePicUrl)
            }

            val messageData = JSONObject().apply {
                put("conversationId", conversationId.toInt())
                put("content", content)
                put("sender", senderData)
                messageId?.let { put("messageId", it) }
            }

            Log.d(TAG, "Sending message via socket with user info and ID: $messageData")
            socket?.emit("send_message", messageData)
            Log.d(TAG, "Message sent via socket with complete sender info and ID: $content (ID: $messageId)")
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message via socket", e)
        }
    }

    /**
     * Send typing indicator
     */
    fun startTyping() {
        currentConversationId?.let { conversationId ->
            val data = JSONObject().apply {
                put("conversationId", conversationId)
            }
            socket?.emit("typing_start", data)
        }
    }

    /**
     * Send typing indicator with specific conversation ID and typing status
     */
    fun sendTyping(conversationId: String, isTyping: Boolean) {
        if (!isConnected) {
            Log.w(TAG, "Socket not connected, cannot send typing indicator")
            return
        }

        try {
            val convId = conversationId.toInt()
            val data = JSONObject().apply {
                put("conversationId", convId)
                put("isTyping", isTyping)
            }

            if (isTyping) {
                socket?.emit("typing_start", data)
            } else {
                socket?.emit("typing_stop", data)
            }
        } catch (e: NumberFormatException) {
            Log.e(TAG, "Invalid conversation ID: $conversationId", e)
        }
    }

    /**
     * Stop typing indicator
     */
    fun stopTyping() {
        currentConversationId?.let { conversationId ->
            val data = JSONObject().apply {
                put("conversationId", conversationId)
            }
            socket?.emit("typing_stop", data)
        }
    }
    
    /**
     * Mark message as read
     */
    fun markMessageAsRead(messageId: String) {
        currentConversationId?.let { conversationId ->
            val data = JSONObject().apply {
                put("conversationId", conversationId)
                put("messageId", messageId)
            }
            socket?.emit("message_read", data)
        }
    }
    
    /**
     * Setup socket event listeners
     */
    private fun setupEventListeners() {
        socket?.apply {
            // Connection events
            on(Socket.EVENT_CONNECT, onConnect)
            on(Socket.EVENT_DISCONNECT, onDisconnect)
            on(Socket.EVENT_CONNECT_ERROR, onConnectError)

            // Message events
            on("new_message", onNewMessage)
            on("message_updated", onMessageUpdated)
            on("message_deleted", onMessageDeleted)

            // User status events
            on("user_online", onUserOnline)
            on("user_offline", onUserOffline)
            
            // Typing events
            on("user_typing", onUserTyping)
            on("user_stopped_typing", onUserStoppedTyping)
            
            // Message read events
            on("message_read_by_user", onMessageRead)
        }
    }
      // Event handlers
    private val onConnect = Emitter.Listener {
        isConnected = true
        Log.d(TAG, "Socket connected successfully")
        
        // Tự động rejoin conversation nếu có conversation đang active
        currentConversationId?.let { conversationId ->
            Log.d(TAG, "Rejoining conversation after reconnect: $conversationId")
            socket?.emit("join_conversation", conversationId)
        }
        
        connectionListeners.forEach { it(true) }
    }
    
    private val onDisconnect = Emitter.Listener {
        isConnected = false
        // KHÔNG reset currentConversationId để có thể rejoin sau khi reconnect
        Log.d(TAG, "Socket disconnected")
        connectionListeners.forEach { it(false) }
    }
    
    private val onConnectError = Emitter.Listener { args ->
        isConnected = false
        Log.e(TAG, "Socket connection error: ${args.contentToString()}")
        connectionListeners.forEach { it(false) }
    }
    
    private val onNewMessage = Emitter.Listener { args ->
        if (args.isNotEmpty() && args[0] is JSONObject) {
            val messageData = args[0] as JSONObject
            Log.d(TAG, "New message received: $messageData")

            // DEBUG POINT: Đặt breakpoint ở đây để theo dõi tin nhắn nhận được qua socket
            val attachmentUrl = messageData.optString("attachment_url")
            if (attachmentUrl.isNotEmpty()) {
                Log.d(TAG, "BREAKPOINT HERE - Message with attachment received: $attachmentUrl")
            }

            messageListeners.forEach { it(messageData) }
        }
    }
    
    private val onMessageUpdated = Emitter.Listener { args ->
        if (args.isNotEmpty() && args[0] is JSONObject) {
            val messageData = args[0] as JSONObject
            Log.d(TAG, "Message updated: $messageData")
            messageListeners.forEach { it(messageData) }
        }
    }
    
    private val onMessageDeleted = Emitter.Listener { args ->
        if (args.isNotEmpty() && args[0] is JSONObject) {
            val deleteData = args[0] as JSONObject
            Log.d(TAG, "Message deleted: $deleteData")
            // Handle message deletion
        }
    }
    
    private val onUserOnline = Emitter.Listener { args ->
        if (args.isNotEmpty() && args[0] is JSONObject) {
            val userData = args[0] as JSONObject
            val userId = userData.optString("userId")
            Log.d(TAG, "User online: $userId")
            userStatusListeners.forEach { it(userId, true) }
        }
    }
    
    private val onUserOffline = Emitter.Listener { args ->
        if (args.isNotEmpty() && args[0] is JSONObject) {
            val userData = args[0] as JSONObject
            val userId = userData.optString("userId")
            Log.d(TAG, "User offline: $userId")
            userStatusListeners.forEach { it(userId, false) }
        }
    }
      private val onUserTyping = Emitter.Listener { args ->
        if (args.isNotEmpty() && args[0] is JSONObject) {
            val typingData = args[0] as JSONObject
            val userId = typingData.optString("userId")
            val conversationId = typingData.optString("conversationId")
            Log.d(TAG, "User typing: $userId in conversation: $conversationId")
            typingListeners.forEach { it(userId, true) }
        }
    }
    
    private val onUserStoppedTyping = Emitter.Listener { args ->
        if (args.isNotEmpty() && args[0] is JSONObject) {
            val typingData = args[0] as JSONObject
            val userId = typingData.optString("userId")
            val conversationId = typingData.optString("conversationId")
            Log.d(TAG, "User stopped typing: $userId in conversation: $conversationId")
            typingListeners.forEach { it(userId, false) }
        }
    }
    
    private val onMessageRead = Emitter.Listener { args ->
        if (args.isNotEmpty() && args[0] is JSONObject) {
            val readData = args[0] as JSONObject
            Log.d(TAG, "Message read: $readData")
            // Handle message read status
        }
    }
    
    // Listener management methods
    fun addMessageListener(listener: (JSONObject) -> Unit) {
        messageListeners.add(listener)
    }
    
    fun removeMessageListener(listener: (JSONObject) -> Unit) {
        messageListeners.remove(listener)
    }
    
    fun addUserStatusListener(listener: (String, Boolean) -> Unit) {
        userStatusListeners.add(listener)
    }
    
    fun removeUserStatusListener(listener: (String, Boolean) -> Unit) {
        userStatusListeners.remove(listener)
    }
    
    fun addTypingListener(listener: (String, Boolean) -> Unit) {
        typingListeners.add(listener)
    }
    
    fun removeTypingListener(listener: (String, Boolean) -> Unit) {
        typingListeners.remove(listener)
    }
    
    fun addConnectionListener(listener: (Boolean) -> Unit) {
        connectionListeners.add(listener)
    }
    
    fun removeConnectionListener(listener: (Boolean) -> Unit) {
        connectionListeners.remove(listener)
    }
    
    /**
     * Helper methods for common callbacks
     */
    fun onTyping(callback: (JSONObject) -> Unit) {
        socket?.on("user_typing") { args ->
            if (args.isNotEmpty() && args[0] is JSONObject) {
                callback(args[0] as JSONObject)
            }
        }
    }

    fun onUserStatus(callback: (JSONObject) -> Unit) {
        socket?.on("user_online") { args ->
            if (args.isNotEmpty() && args[0] is JSONObject) {
                val data = args[0] as JSONObject
                data.put("isOnline", true)
                callback(data)
            }
        }

        socket?.on("user_offline") { args ->
            if (args.isNotEmpty() && args[0] is JSONObject) {
                val data = args[0] as JSONObject
                data.put("isOnline", false)
                callback(data)
            }
        }
    }

    // Utility methods
    fun isConnected() = isConnected
    
    fun getCurrentConversationId() = currentConversationId

    /**
     * Cập nhật thông tin người dùng hiện tại
     */
    fun updateCurrentUserData(userId: String, userName: String, avatarUrl: String) {
        currentUserData = JSONObject().apply {
            put("userId", userId)
            put("userName", userName)
            put("avatarUrl", avatarUrl)
        }

        // Gửi thông tin người dùng hiện tại đến server
        socket?.emit("update_user_data", currentUserData)
    }

    /**
     * Lấy thông tin người dùng hiện tại
     */
    fun getCurrentUserData(): JSONObject? {
        return currentUserData
    }
}
