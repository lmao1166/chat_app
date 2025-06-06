# Sửa lỗi: Không nhận tin nhắn real-time sau khi thoát và vào lại phòng offline user

## Vấn đề ban đầu
Khi user:
1. Vào phòng của người dùng offline
2. Thoát ra khỏi phòng 
3. Vào lại phòng của người dùng đang offline
4. → Không thể nhận tin nhắn real-time từ người dùng khác nữa

## Nguyên nhân
1. **Message listeners là global**: Listeners được setup 1 lần trong `init` và nhận tất cả message mà không kiểm tra conversation context
2. **Không có conversation context check**: Khi nhận message qua Socket.IO, không kiểm tra xem message có thuộc conversation hiện tại không
3. **Join conversation logic không clean**: Khi join conversation mới, không clear state đúng cách
4. **Không có reconnection handling**: Khi socket reconnect, không tự động rejoin conversation

## Các thay đổi đã thực hiện

### 1. Backend Changes (`socket.service.js`)

#### Thêm conversationId vào message data
```javascript
emitNewMessage(conversationId, message) {
    const messageWithConversationId = {
        ...message,
        conversationId: conversationId
    };
    this._emitToConversation(conversationId, 'new_message', messageWithConversationId);
}
```

#### Cải thiện typing events với conversation context
```javascript
typing_start: (data) => socket.to(`conversation_${data.conversationId}`).emit('user_typing', { 
    userId, 
    userInfo, 
    conversationId: data.conversationId,
    username: userInfo.username,
    isTyping: true
}),
```

#### Thêm logging cho join/leave conversation
```javascript
join_conversation: (id) => {
    socket.join(`conversation_${id}`);
    console.log(`User ${userInfo.username} joined conversation ${id}`);
},
```

### 2. Mobile Changes (`ChatViewModel.kt`)

#### Thêm conversation context check trong message listener
```kotlin
private fun setupSocketListeners() {
    socketManager.addMessageListener { messageData ->
        viewModelScope.launch {
            try {
                // KIỂM TRA CONVERSATION CONTEXT TRƯỚC
                val messageConversationId = messageData.optString("conversationId", "")
                if (messageConversationId.isNotEmpty() && messageConversationId != currentConversationId) {
                    Log.d("ChatViewModel", "Message from different conversation, ignoring")
                    return@launch
                }
                // ...existing message processing...
```

#### Cải thiện join/leave conversation logic
```kotlin
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
```

#### Thêm connection listener cho auto-rejoin
```kotlin
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
        }
    }
}
```

#### Thêm conversation context check cho typing events
```kotlin
// Listen for typing indicators
socketManager.onTyping { typingData ->
    viewModelScope.launch {
        try {
            // KIỂM TRA CONVERSATION CONTEXT cho typing events
            val typingConversationId = typingData.optString("conversationId", "")
            if (typingConversationId.isNotEmpty() && typingConversationId != currentConversationId) {
                Log.d("ChatViewModel", "Typing indicator from different conversation, ignoring")
                return@launch
            }
            // ...existing typing processing...
```

### 3. Mobile Changes (`SocketManager.kt`)

#### Cải thiện connection handling
```kotlin
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
```

#### Cải thiện join conversation logic
```kotlin
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
```

### 4. Debug Utilities

#### Thêm debug methods trong ChatViewModel
```kotlin
fun debugConnectionState() {
    Log.d("ChatViewModel", "=== DEBUG CONNECTION STATE ===")
    Log.d("ChatViewModel", "Current conversation ID: $currentConversationId")
    Log.d("ChatViewModel", "Socket connected: ${socketManager.isConnected()}")
    Log.d("ChatViewModel", "Processed messages count: ${processedMessageIds.size}")
    Log.d("ChatViewModel", "Current messages count: ${_messages.value?.size ?: 0}")
    Log.d("ChatViewModel", "===========================")
}
```

#### Tạo test HTML để kiểm tra (test-offline-user-chat.html)
- Test scenario mô phỏng vấn đề offline user
- Bidirectional messaging test
- Debug tools để kiểm tra connection state

## Kết quả mong đợi

Sau khi áp dụng các thay đổi này:

1. ✅ **Message filtering**: Chỉ nhận messages từ conversation hiện tại
2. ✅ **Clean join/leave**: State được reset đúng cách khi join conversation mới
3. ✅ **Auto-rejoin**: Tự động rejoin conversation sau khi reconnect
4. ✅ **Conversation context**: Typing events và messages đều có conversation context
5. ✅ **Better logging**: Dễ dàng debug các vấn đề connection

## Testing

1. Chạy backend server
2. Mở `test-offline-user-chat.html` trong browser
3. Get JWT tokens cho 2 users
4. Test scenario "Simulate Offline User Scenario"
5. Verify rằng messages được nhận đúng cách sau khi rejoin conversation

## Notes

- Các thay đổi này backward compatible
- Không ảnh hưởng đến performance
- Thêm proper error handling và logging
- Cải thiện user experience khi có network issues
