package com.example.chatapp.ui.home.chat

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.chatapp.model.response.ConversationResponse
import com.example.chatapp.model.response.MessageResponse
import com.example.chatapp.repository.ConversationRepository
import com.example.chatapp.repository.MessageRepository
import kotlinx.coroutines.launch

/**
 * ViewModel for handling chat functionalities including conversations and messages
 */
class ChatViewModel : ViewModel() {
    private val conversationRepository = ConversationRepository()
    private val messageRepository = MessageRepository()

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
                        loadMessages(apiResponse.data.id.toString())
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
    fun loadMessages(conversationId: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = ""

            try {
                val response = messageRepository.getMessagesByConversationId(conversationId)

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
     * Sends a message in the current conversation
     * @param conversationId ID of the conversation
     * @param content Message content
     */
    fun sendMessage(conversationId: String, content: String) {
        if (content.isBlank()) return

        viewModelScope.launch {
            try {
                val response = messageRepository.sendMessage(conversationId, content)

                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true && apiResponse.data != null) {
                        // Refresh messages to include the new one
                        loadMessages(conversationId)
                    } else {
                        _error.value = apiResponse?.message ?: "Failed to send message"
                    }
                } else {
                    _error.value = "Error ${response.code()}: ${response.message()}"
                }
            } catch (e: Exception) {
                Log.e("ChatViewModel", "Error sending message", e)
                _error.value = e.localizedMessage ?: "Unknown error"
            }
        }
    }
}
