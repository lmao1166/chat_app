package com.example.chatapp.ui.home.chat

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentChatBinding
import com.example.chatapp.databinding.FragmentChatRoomBinding
import com.example.chatapp.model.response.ConversationResponse
import com.example.chatapp.utils.TokenManager

class ChatFragment : Fragment() {
    private var _binding: Any? = null
    private var _chatRoomBinding: FragmentChatRoomBinding? = null
    private val isDirectChat by lazy { arguments?.getBoolean("isDirectChat", false) ?: false }
    private val userId by lazy { arguments?.getInt("userId", -1) }
    private val userName by lazy { arguments?.getString("userName") }
    private val userAvatar by lazy { arguments?.getString("userAvatar") }
    private val conversationId by lazy { arguments?.getInt("conversationId", -1) }
    private val conversationName by lazy { arguments?.getString("conversationName") }
    private val conversationThumbnail by lazy { arguments?.getString("conversationThumbnail") }

    private lateinit var conversationAdapter: ConversationAdapter
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var viewModel: ChatViewModel
    
    // Typing indicator
    private var typingHandler: Handler? = null
    private var typingRunnable: Runnable? = null
    private var isCurrentlyTyping = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

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
        
        // Observe ViewModel data
        setupConversationObservers(binding)

        // Load conversations
        viewModel.loadConversations()
    }
    
    private fun setupConversationObservers(binding: FragmentChatBinding) {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe error state
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                showEmptyState(binding)
            }
        }

        // Observe conversations data
        viewModel.conversations.observe(viewLifecycleOwner) { conversations ->
            if (conversations.isEmpty()) {
                showEmptyState(binding)
            } else {
                binding.emptyStateView.visibility = View.GONE
                binding.conversationsRecyclerView.visibility = View.VISIBLE
                conversationAdapter.submitList(conversations)
                Log.d("ChatFragment", "Loaded ${conversations.size} conversations")
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
        Log.d("ChatFragment", "Conversation clicked: ${conversation.id}")
        
        // Get the other member info
        val otherMember = conversation.members.firstOrNull()
        
        // Navigate to chat room
        val bundle = Bundle().apply {
            putBoolean("isDirectChat", true)
            putInt("userId", otherMember?.id ?: -1)
            putString("userName", otherMember?.username)
            putString("userAvatar", otherMember?.profilePicUrl)
            putInt("conversationId", conversation.id)
        }
        
        val chatRoomFragment = ChatFragment().apply {
            arguments = bundle
        }
        
        // Use activity's fragment manager to replace the entire home fragment
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, chatRoomFragment)
            .addToBackStack("chatRoom")
            .commit()
    }

    private fun setupChatRoomUI(binding: FragmentChatRoomBinding) {
        // Store binding reference for later use
        _chatRoomBinding = binding
        
        // Setup chat room UI with user data - prefer conversationName over userName
        binding.contactName.text = conversationName ?: userName ?: "Chat"

        // Load user avatar if available - prefer conversationThumbnail over userAvatar
        val avatarUrl = conversationThumbnail ?: userAvatar
        avatarUrl?.let { url ->
            if (url.isNotEmpty()) {
                com.squareup.picasso.Picasso.get()
                    .load(url)
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
        setupMessageObservers(binding)        // Load chat history
        loadChatHistory()

        // Initialize message sending functionality
        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendMessage(message)
                binding.messageInput.text?.clear()
            }
        }

        // Setup typing indicator
        setupTypingIndicator(binding)

        // Setup attachment button if needed
        binding.attachmentButton.setOnClickListener {
            // TODO: Implement attachment functionality
        }

        // Setup menu button if needed
        binding.menuButton.setOnClickListener {
            // TODO: Show chat options menu
        }
    }

    private fun setupTypingIndicator(binding: FragmentChatRoomBinding) {
        typingHandler = Handler(Looper.getMainLooper())
        
        binding.messageInput.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val text = s?.toString()?.trim() ?: ""
                
                if (text.isNotEmpty() && !isCurrentlyTyping) {
                    // Start typing
                    isCurrentlyTyping = true
                    viewModel.sendTyping(true)
                }
                
                // Cancel previous runnable
                typingRunnable?.let { typingHandler?.removeCallbacks(it) }
                
                // Set new runnable to stop typing after 2 seconds of inactivity
                typingRunnable = Runnable {
                    if (isCurrentlyTyping) {
                        isCurrentlyTyping = false
                        viewModel.sendTyping(false)
                    }
                }
                
                typingHandler?.postDelayed(typingRunnable!!, 2000)
            }
        })
    }    private fun setupMessageObservers(binding: FragmentChatRoomBinding) {
        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe error state
        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }

        // Observe messages
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            val previousCount = messageAdapter.itemCount
            messageAdapter.submitList(messages) {
                // Callback sau khi danh sách được cập nhật
                if (messages.isNotEmpty()) {
                    // Chỉ cuộn đến dưới cùng nếu đang ở gần cuối danh sách hoặc có tin nhắn mới
                    val layoutManager = binding.messagesRecyclerView.layoutManager as LinearLayoutManager
                    val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

                    if (previousCount == 0 || // Lần đầu tiên tải tin nhắn
                        messages.size > previousCount || // Có tin nhắn mới
                        lastVisiblePosition >= previousCount - 3) { // Đang xem gần cuối danh sách

                        binding.messagesRecyclerView.post {
                            binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
                        }
                    }
                }
            }
        }

        // Observe typing indicators
        viewModel.typingUsers.observe(viewLifecycleOwner) { typingUsers ->
            updateTypingIndicator(binding, typingUsers)

            // Cuộn lên khi có người đang nhập tin nhắn
            if (typingUsers.isNotEmpty()) {
                val messageCount = messageAdapter.itemCount
                if (messageCount > 0) {
                    binding.messagesRecyclerView.post {
                        binding.messagesRecyclerView.scrollToPosition(messageCount - 1)
                    }
                }
            }
        }

        // Observe online users
        viewModel.onlineUsers.observe(viewLifecycleOwner) { onlineUsers ->
            updateOnlineStatus(binding, onlineUsers)
        }
    }    private fun updateTypingIndicator(binding: FragmentChatRoomBinding, typingUsers: List<String>) {
        if (typingUsers.isEmpty()) {
            // Hide typing indicator
            binding.typingIndicatorContainer.visibility = View.GONE
            binding.typingIndicator.visibility = View.GONE
        } else {
            // Show typing indicator
            binding.typingIndicatorContainer.visibility = View.VISIBLE
            binding.typingIndicator.visibility = View.VISIBLE
            binding.typingIndicatorText.text = when (typingUsers.size) {
                1 -> "${typingUsers[0]} is typing..."
                2 -> "${typingUsers[0]} and ${typingUsers[1]} are typing..."
                else -> "${typingUsers[0]} and ${typingUsers.size - 1} others are typing..."
            }
        }
    }

    private fun updateOnlineStatus(binding: FragmentChatRoomBinding, onlineUsers: List<String>) {
        // Update online status indicator if user is online
        val isOnline = userName?.let { onlineUsers.contains(it) } ?: false
        binding.onlineIndicator.visibility = if (isOnline) View.VISIBLE else View.GONE
    }

    private fun setupMessageList(binding: FragmentChatRoomBinding) {
        // Get current user ID
        val tokenManager = TokenManager.getInstance(requireContext())
        val currentUserId = tokenManager.getUserId() ?: ""
        
        // Initialize MessageAdapter with current user ID
        messageAdapter = MessageAdapter(currentUserId)
        
        // Setup the RecyclerView for messages
        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true  // Messages appear from bottom
        layoutManager.reverseLayout = false
        
        binding.messagesRecyclerView.apply {
            this.layoutManager = layoutManager
            adapter = messageAdapter

            // Thêm listener để cuộn tự động khi RecyclerView thay đổi kích thước
            addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                if (bottom < oldBottom) {
                    // Khi bàn phím hiện lên, cuộn đến tin nhắn cuối cùng
                    postDelayed({
                        val messageCount = messageAdapter.itemCount
                        if (messageCount > 0) {
                            scrollToPosition(messageCount - 1)
                        }
                    }, 100)
                }
            }
        }
    }    private fun loadChatHistory() {
        if (conversationId != null) {
            // Case 1: We have a conversation ID, load messages directly
            viewModel.loadMessages(conversationId!!)
            // Join the conversation room for real-time updates
            viewModel.joinConversation(conversationId.toString())
        } else if (userId != null) {
            // Case 2: No conversation ID but we have a userId - create or get conversation first
            try {
                val userIdInt = userId!!.toInt()
                viewModel.loadOrCreateConversation(userIdInt)
                
                // Observe current conversation to join room when it's available
                viewModel.currentConversation.observe(viewLifecycleOwner) { conversation ->
                    conversation?.let {
                        viewModel.joinConversation(it.id.toString())
                    }
                }
            } catch (e: NumberFormatException) {
                Log.e("ChatFragment", "Invalid user ID: $userId")
                showEmptyChat("ID người dùng không hợp lệ")
            }
        } else {
            // Case 3: Neither conversation ID nor user ID available
            Log.e("ChatFragment", "Cannot load messages - no conversation ID or user ID")
            showEmptyChat("Không thể xác định người nhận tin nhắn")
        }
    }

    private fun sendMessage(message: String) {
        // Get the conversation ID - either directly or from the ViewModel's current conversation
        val activeConversationId = conversationId ?: viewModel.currentConversation.value?.id

        if (activeConversationId != null) {
            viewModel.sendMessage(activeConversationId, message)

            // Đảm bảo RecyclerView cuộn đến tin nhắn mới nhất sau khi gửi
            _chatRoomBinding?.let { binding ->
                binding.messagesRecyclerView.post {
                    val messageCount = messageAdapter.itemCount
                    if (messageCount > 0) {
                        binding.messagesRecyclerView.scrollToPosition(messageCount - 1)
                    }
                }
            }
        } else {
            Log.e("ChatFragment", "Cannot send message - no active conversation")
            Toast.makeText(context, "Không thể gửi tin nhắn - không có cuộc trò chuyện nào đang hoạt động", Toast.LENGTH_SHORT).show()
        }
    }

    // Helper function to show empty chat or error state
    private fun showEmptyChat(errorMessage: String) {
        _chatRoomBinding?.let { binding ->
            // Show error message via toast
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()

            // If we have an empty state view, show it
            binding.messagesRecyclerView.visibility = View.GONE

            // You could add an empty state view in your layout like this:
            // binding.emptyStateView.visibility = View.VISIBLE
            // binding.emptyStateErrorText.text = errorMessage
        }
    }    override fun onDestroyView() {
        super.onDestroyView()
        
        // Stop typing indicator
        if (isCurrentlyTyping) {
            viewModel.sendTyping(false)
        }
        
        // Clean up typing handler
        typingRunnable?.let { typingHandler?.removeCallbacks(it) }
        typingHandler = null
        typingRunnable = null
        
        // Leave conversation room
        viewModel.leaveConversation()
        
        _binding = null
        _chatRoomBinding = null
    }

    override fun onPause() {
        super.onPause()
        // Stop typing when fragment is paused
        if (isCurrentlyTyping) {
            isCurrentlyTyping = false
            viewModel.sendTyping(false)
        }
    }
}
