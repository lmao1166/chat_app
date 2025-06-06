package com.example.chatapp.ui.home.chat

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentChatBinding // Giữ nguyên cho layout danh sách chat
import com.example.chatapp.databinding.FragmentChatRoomBinding // Giữ nguyên cho layout phòng chat
import com.example.chatapp.model.response.ConversationResponse
import com.example.chatapp.utils.TokenManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class ChatFragment : Fragment() {

    // Khai báo binding an toàn hơn
    private var _fragmentChatBinding: FragmentChatBinding? = null
    // Chỉ truy cập _fragmentChatBinding khi nó đã được gán giá trị và không null
    private val binding get() = _fragmentChatBinding!!

    private var _fragmentChatRoomBinding: FragmentChatRoomBinding? = null
    // Chỉ truy cập _fragmentChatRoomBinding khi nó đã được gán giá trị và không null
    private val chatRoomBinding get() = _fragmentChatRoomBinding!!

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

    // Image picker launcher
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { imageUri ->
                handleSelectedImage(imageUri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        return if (isDirectChat) {
            val chatRoomBindingInflated = DataBindingUtil.inflate<FragmentChatRoomBinding>(
                inflater, R.layout.fragment_chat_room, container, false
            )
            _fragmentChatRoomBinding = chatRoomBindingInflated // Gán vào biến _fragmentChatRoomBinding
            setupChatRoomUI(chatRoomBindingInflated)
            chatRoomBindingInflated.root
        } else {
            val chatBindingInflated = DataBindingUtil.inflate<FragmentChatBinding>(
                inflater, R.layout.fragment_chat, container, false
            )
            _fragmentChatBinding = chatBindingInflated // Gán vào biến _fragmentChatBinding
            setupChatListUI(chatBindingInflated)
            chatBindingInflated.root
        }
    }    private fun setupChatListUI(binding: FragmentChatBinding) {
        val tokenManager = TokenManager.getInstance(requireContext())
        val currentUserId = tokenManager.getUserId() ?: ""
        
        conversationAdapter = ConversationAdapter({ conversation ->
            onConversationClick(conversation)
        }, currentUserId)

        binding.conversationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = conversationAdapter
        }

        setupConversationObservers(binding)
        viewModel.loadConversations()
    }

    private fun setupConversationObservers(binding: FragmentChatBinding) {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_LONG).show()
                showEmptyState(binding)
            }
        }

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

        binding.newChatButton.setOnClickListener {
            // TODO: Navigate to user selection screen to start new chat
            Log.d("ChatFragment", "New chat button clicked")
        }
    }    private fun onConversationClick(conversation: ConversationResponse) {
        Log.d("ChatFragment", "Conversation clicked: ${conversation.id}")

        // Notify ViewModel about conversation switch for real-time updates
        viewModel.switchToConversation(conversation.id.toString())

        val otherMember = conversation.members.firstOrNull()

        val bundle = Bundle().apply {
            putBoolean("isDirectChat", true)
            putInt("userId", otherMember?.id ?: -1)
            putString("userName", otherMember?.username)
            putString("userAvatar", otherMember?.profilePicUrl)
            putInt("conversationId", conversation.id)
            putString("conversationName", conversation.name) // Truyền conversationName
            putString("conversationThumbnail", conversation.thumbnail) // Truyền conversationThumbnail
        }

        val chatRoomFragment = ChatFragment().apply {
            arguments = bundle
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, chatRoomFragment)
            .addToBackStack("chatRoom")
            .commit()
    }

    private fun setupChatRoomUI(binding: FragmentChatRoomBinding) {
        // Ẩn bottom navigation khi vào chat room
        hideBottomNavigation()
        
        binding.contactName.text = conversationName ?: userName ?: "Chat"

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

        // Cập nhật xử lý nút back để quay về trang danh sách hội thoại
        binding.backButton.setOnClickListener {
            navigateBackToConversationList()
        }

        setupMessageList(binding)
        setupMessageObservers(binding)
        loadChatHistory()
        binding.sendButton.setOnClickListener {
            val message = binding.messageInput.text.toString().trim()
            if (message.isNotEmpty()) {
                sendTextMessage(message)
                binding.messageInput.text?.clear()
            }
        }

        setupTypingIndicator(binding)
        binding.attachmentButton.setOnClickListener {
            openImagePicker()
        }

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
                    isCurrentlyTyping = true
                    viewModel.sendTyping(true)
                }

                typingRunnable?.let { typingHandler?.removeCallbacks(it) }

                typingRunnable = Runnable {
                    if (isCurrentlyTyping) {
                        isCurrentlyTyping = false
                        viewModel.sendTyping(false)
                    }
                }

                typingHandler?.postDelayed(typingRunnable!!, 2000)
            }
        })
    }

    private fun setupMessageObservers(binding: FragmentChatRoomBinding) {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            val previousCount = messageAdapter.itemCount
            messageAdapter.submitList(messages) {
                if (messages.isNotEmpty()) {
                    val layoutManager = binding.messagesRecyclerView.layoutManager as LinearLayoutManager
                    val lastVisiblePosition = layoutManager.findLastVisibleItemPosition()

                    if (previousCount == 0 ||
                        messages.size > previousCount ||
                        lastVisiblePosition >= previousCount - 3) {

                        binding.messagesRecyclerView.post {
                            binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
                        }
                    }
                }
            }
        }

        viewModel.typingUsers.observe(viewLifecycleOwner) { typingUsers ->
            updateTypingIndicator(binding, typingUsers)

            if (typingUsers.isNotEmpty()) {
                val messageCount = messageAdapter.itemCount
                if (messageCount > 0) {
                    binding.messagesRecyclerView.post {
                        binding.messagesRecyclerView.scrollToPosition(messageCount - 1)
                    }
                }
            }
        }

        viewModel.onlineUsers.observe(viewLifecycleOwner) { onlineUsers ->
            updateOnlineStatus(binding, onlineUsers)
        }
    }

    private fun updateTypingIndicator(binding: FragmentChatRoomBinding, typingUsers: List<String>) {
        if (typingUsers.isEmpty()) {
            binding.typingIndicatorContainer.visibility = View.GONE
            binding.typingIndicator.visibility = View.GONE
        } else {
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
        val isOnline = userName?.let { onlineUsers.contains(it) } ?: false
        binding.onlineIndicator.visibility = if (isOnline) View.VISIBLE else View.GONE
    }

    private fun setupMessageList(binding: FragmentChatRoomBinding) {
        val tokenManager = TokenManager.getInstance(requireContext())
        val currentUserId = tokenManager.getUserId() ?: ""

        messageAdapter = MessageAdapter(currentUserId)

        val layoutManager = LinearLayoutManager(context)
        layoutManager.stackFromEnd = true
        layoutManager.reverseLayout = false

        binding.messagesRecyclerView.apply {
            this.layoutManager = layoutManager
            adapter = messageAdapter

            addOnLayoutChangeListener { _, _, _, _, bottom, _, _, _, oldBottom ->
                if (bottom < oldBottom) {
                    postDelayed({
                        val messageCount = messageAdapter.itemCount
                        if (messageCount > 0) {
                            scrollToPosition(messageCount - 1)
                        }
                    }, 100)
                }
            }
        }
    }

    private fun loadChatHistory() {
        if (conversationId != null && conversationId != -1) {
            viewModel.loadMessages(conversationId!!)
            viewModel.joinConversation(conversationId.toString())
        } else if (userId != null && userId != -1) {
            viewModel.loadOrCreateConversation(userId!!)

            viewModel.currentConversation.observe(viewLifecycleOwner) { conversation ->
                conversation?.let {
                    viewModel.joinConversation(it.id.toString())
                }
            }
        } else {
            Log.e("ChatFragment", "Cannot load messages - no conversation ID or user ID")
            showEmptyChat("Không thể xác định người nhận tin nhắn")
        }
    }

    private fun sendTextMessage(message: String) {
        val activeConversationId = conversationId ?: viewModel.currentConversation.value?.id

        if (activeConversationId != null) {
            viewModel.sendMessage(activeConversationId, message)

            // Sử dụng chatRoomBinding đã được khởi tạo
            chatRoomBinding.messagesRecyclerView.post {
                val messageCount = messageAdapter.itemCount
                if (messageCount > 0) {
                    chatRoomBinding.messagesRecyclerView.scrollToPosition(messageCount - 1)
                }
            }
        } else {
            Log.e("ChatFragment", "Cannot send message - no active conversation")
            Toast.makeText(context, "Không thể gửi tin nhắn - không có cuộc trò chuyện nào đang hoạt động", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendImageMessage(imagePart: MultipartBody.Part) {
        val activeConversationId = conversationId ?: viewModel.currentConversation.value?.id

        if (activeConversationId != null) {
            viewModel.sendMessageWithImage(activeConversationId, "", imagePart)

            chatRoomBinding.messagesRecyclerView.post {
                val messageCount = messageAdapter.itemCount
                if (messageCount > 0) {
                    chatRoomBinding.messagesRecyclerView.scrollToPosition(messageCount - 1)
                }
            }
        } else {
            Log.e("ChatFragment", "Cannot send image - no active conversation")
            Toast.makeText(context, "Không thể gửi hình ảnh - không có cuộc trò chuyện nào đang hoạt động", Toast.LENGTH_SHORT).show()
        }
    }

    private fun sendTextAndImageMessage(content: String, imagePart: MultipartBody.Part) {
        val activeConversationId = conversationId ?: viewModel.currentConversation.value?.id

        if (activeConversationId != null) {
            viewModel.sendMessageWithImage(activeConversationId, content, imagePart)

            chatRoomBinding.messagesRecyclerView.post {
                val messageCount = messageAdapter.itemCount
                if (messageCount > 0) {
                    chatRoomBinding.messagesRecyclerView.scrollToPosition(messageCount - 1)
                }
            }
        } else {
            Log.e("ChatFragment", "Cannot send text and image - no active conversation")
            Toast.makeText(context, "Không thể gửi tin nhắn - không có cuộc trò chuyện nào đang hoạt động", Toast.LENGTH_SHORT).show()
        }
    }

    private fun openImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI).apply {
            type = "image/*"
        }
        imagePickerLauncher.launch(intent)
    }

    private fun handleSelectedImage(imageUri: Uri) {
        try {
            val file = getFileFromUri(imageUri)
            if (file != null && file.exists()) {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val imagePart = MultipartBody.Part.createFormData("chatImage", file.name, requestFile)

                val currentText = _fragmentChatRoomBinding?.messageInput?.text.toString().trim() ?: ""

                if (currentText.isNotEmpty()) {
                    sendTextAndImageMessage(currentText, imagePart)
                    _fragmentChatRoomBinding?.messageInput?.text?.clear()
                } else {
                    sendImageMessage(imagePart)
                }
            } else {
                Toast.makeText(context, "Không thể đọc file hình ảnh", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Log.e("ChatFragment", "Error handling selected image", e)
            Toast.makeText(context, "Lỗi xử lý hình ảnh: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun getFileFromUri(uri: Uri): File? {
        return try {
            val contentResolver = requireContext().contentResolver
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File.createTempFile("chat_image", ".jpg", requireContext().cacheDir)

            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            Log.e("ChatFragment", "Error getting file from URI", e)
            null
        }
    }

    private fun showEmptyChat(errorMessage: String) {
        _fragmentChatRoomBinding?.let { binding ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            binding.messagesRecyclerView.visibility = View.GONE
            // Thêm logic hiển thị empty state view nếu có trong layout
        }
    }

    private fun hideBottomNavigation() {
        val homeFragment = parentFragment?.parentFragment as? com.example.chatapp.ui.home.HomeFragment
        homeFragment?.setBottomNavigationVisibility(false)
    }

    private fun showBottomNavigation() {
        val homeFragment = parentFragment?.parentFragment as? com.example.chatapp.ui.home.HomeFragment
        homeFragment?.setBottomNavigationVisibility(true)
    }

    override fun onResume() {
        super.onResume()
        
        // Refresh conversation list when returning to chat screen
        // This ensures we catch any messages from other conversations
        if (!isDirectChat) {
            viewModel.refreshConversationList()
            Log.d("ChatFragment", "Refreshed conversation list on resume")
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (isCurrentlyTyping) {
            viewModel.sendTyping(false)
        }

        typingRunnable?.let { typingHandler?.removeCallbacks(it) }
        typingHandler = null
        typingRunnable = null

        viewModel.leaveConversation()

        // Hiện lại bottom navigation khi thoát khỏi chat room
        if (isDirectChat) {
            showBottomNavigation()
        }

        _fragmentChatBinding = null
        _fragmentChatRoomBinding = null
    }

    override fun onPause() {
        super.onPause()
        if (isCurrentlyTyping) {
            isCurrentlyTyping = false
            viewModel.sendTyping(false)
        }
    }

    private fun navigateBackToConversationList() {
        // Hiện lại bottom navigation
        showBottomNavigation()

        // Quay về fragment danh sách hội thoại
        parentFragmentManager.popBackStack()
    }
}
