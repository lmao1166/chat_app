package com.example.chatapp.ui.home.chat

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.R
import com.example.chatapp.RetrofitInstance
import com.example.chatapp.databinding.FragmentChatBinding
import com.example.chatapp.databinding.FragmentChatRoomBinding
import com.example.chatapp.model.response.ConversationResponse
import com.example.chatapp.utils.TokenManager
import kotlinx.coroutines.launch

class ChatFragment : Fragment() {
    private var _binding: Any? = null
    private val isDirectChat by lazy { arguments?.getBoolean("isDirectChat", false) ?: false }
    private val userId by lazy { arguments?.getString("userId") }
    private val userName by lazy { arguments?.getString("userName") }
    private val userAvatar by lazy { arguments?.getString("userAvatar") }
    
    private lateinit var conversationAdapter: ConversationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return if (isDirectChat) {
            // This is a direct chat initiated from user profile - use chat_room layout
            val chatRoomBinding = DataBindingUtil.inflate<FragmentChatRoomBinding>(
                inflater, R.layout.fragment_chat_room, container, false
            )
            _binding = chatRoomBinding
            setupChatRoomUI(chatRoomBinding)
            chatRoomBinding.root
        } else {
            // This is a regular chat listing - use the original layout
            val chatBinding = DataBindingUtil.inflate<FragmentChatBinding>(
                inflater, R.layout.fragment_chat, container, false
            )
            _binding = chatBinding
            setupChatListUI(chatBinding)
            chatBinding.root
        }
    }

    private fun setupChatListUI(binding: FragmentChatBinding) {
        // Initialize RecyclerView and adapter
        conversationAdapter = ConversationAdapter { conversation ->
            onConversationClick(conversation)
        }
        
        binding.conversationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = conversationAdapter
        }
        
        // Load conversations
        loadConversations(binding)
    }
    
    private fun loadConversations(binding: FragmentChatBinding) {
        val tokenManager = TokenManager.getInstance(requireContext())
        
        // Update Retrofit with current token
        RetrofitInstance.updateWithToken(requireContext())
        
        lifecycleScope.launch {
            try {
                binding.emptyStateView.visibility = View.GONE
                binding.conversationsRecyclerView.visibility = View.VISIBLE
                
                val response = RetrofitInstance.conversationApi.getConversations()
                
                if (response.isSuccessful) {
                    val apiResponse = response.body()
                    if (apiResponse?.success == true) {
                        val conversations = apiResponse.data ?: emptyList()
                        if (conversations.isEmpty()) {
                            showEmptyState(binding)
                        } else {
                            conversationAdapter.submitList(conversations)
                        }
                        Log.d("ChatFragment", "Loaded ${conversations.size} conversations")
                    } else {
                        Log.e("ChatFragment", "API call failed: ${apiResponse?.message}")
                        showEmptyState(binding)
                    }
                } else {
                    Log.e("ChatFragment", "HTTP error: ${response.code()} - ${response.message()}")
                    showEmptyState(binding)
                }
            } catch (e: Exception) {
                Log.e("ChatFragment", "Error loading conversations", e)
                showEmptyState(binding)
            }
        }
    }
    
    private fun showEmptyState(binding: FragmentChatBinding) {
        binding.conversationsRecyclerView.visibility = View.GONE
        binding.emptyStateView.visibility = View.VISIBLE
        
        // Setup new chat button click listener
        binding.newChatButton.setOnClickListener {
            // TODO: Navigate to user selection screen to start new chat
            Log.d("ChatFragment", "New chat button clicked")
        }
    }
    
    private fun onConversationClick(conversation: ConversationResponse) {
        // TODO: Navigate to chat room with this conversation
        Log.d("ChatFragment", "Conversation clicked: ${conversation.id}")
        
        // Get the other member info
        val otherMember = conversation.members.firstOrNull()
        
        // Navigate to chat room
        val bundle = Bundle().apply {
            putBoolean("isDirectChat", true)
            putString("userId", otherMember?.id.toString())
            putString("userName", otherMember?.username)
            putString("userAvatar", otherMember?.profilePicUrl)
        }
        
        val chatRoomFragment = ChatFragment().apply {
            arguments = bundle
        }
        
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, chatRoomFragment)
            .addToBackStack(null)
            .commit()
    }

    private fun setupChatRoomUI(binding: FragmentChatRoomBinding) {
        // Setup chat room UI with user data
        binding.contactName.text = userName ?: "Chat"

        // Load user avatar if available
        userAvatar?.let { avatarUrl ->
            if (avatarUrl.isNotEmpty()) {
                com.squareup.picasso.Picasso.get()
                    .load(avatarUrl)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .into(binding.contactImage)
            }
        }

        // Setup back button navigation
        binding.backButton.setOnClickListener {
            // Simply perform back navigation without trying to manipulate the navigation menu
            // This avoids crashes when the HomeFragment can't be found
            parentFragmentManager.popBackStack()
        }

        // Setup chat message list
        setupMessageList(binding)

        // Configure chat functionality with the user ID
        userId?.let {
            // Load chat history with this user
            loadChatHistory(it)
        }

        // Initialize message sending functionality
        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message, userId ?: "")
                binding.messageInput.text?.clear()
            }
        }

        // Setup attachment button if needed
        binding.attachmentButton.setOnClickListener {
            // TODO: Implement attachment functionality
        }

        // Setup menu button if needed
        binding.menuButton.setOnClickListener {
            // TODO: Show chat options menu
        }
    }

    private fun setupMessageList(binding: FragmentChatRoomBinding) {
        // Setup the RecyclerView for messages
        val layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)
        layoutManager.stackFromEnd = true  // Messages appear from bottom
        binding.messagesRecyclerView.layoutManager = layoutManager

        // TODO: Create and set adapter for the messages
    }

    private fun loadChatHistory(userId: String) {
        // TODO: Load chat history with this user from your backend
    }

    private fun sendMessage(message: String, userId: String) {
        // TODO: Implement sending message to the userId
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
