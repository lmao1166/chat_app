package com.example.chatapp.ui.home.contacts.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentUserProfileBinding
import com.example.chatapp.model.response.UserResponse
import com.example.chatapp.ui.home.chat.ChatFragment
import com.squareup.picasso.Picasso

class UserProfileFragment : Fragment() {

    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var viewModel: UserProfileViewModel
    private var userId: Int? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getInt("userId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_user_profile,
            container,
            false
        )

        setupViewModel()
        setupObservers()
        setupClickListeners()

        // Load user data
        userId?.let {
            viewModel.loadUserProfile(it)
        } ?: run {
            Toast.makeText(requireContext(), "Không thể tìm thấy người dùng này", Toast.LENGTH_SHORT).show()
            navigateBack()
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()
        // Simply refresh data without trying to highlight navigation buttons
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[UserProfileViewModel::class.java]
    }

    private fun setupObservers() {
        viewModel.userData.observe(viewLifecycleOwner) { userData ->
            displayUserData(userData)
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            binding.profileContent.visibility = if (isLoading) View.GONE else View.VISIBLE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                navigateBack()
            }
        }
    }

    private fun displayUserData(user: UserResponse) {
        binding.apply {
            username.text = user.username
            email.text = user.email

            // Load profile image
            if (!user.profilePicUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(user.profilePicUrl)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .into(profileImage)
            }
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            navigateBack()
        }

        binding.chatButton.setOnClickListener {
            navigateToChat()
        }
    }

    private fun navigateBack() {
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }

    @SuppressLint("SetTextI18n")
    private fun navigateToChat() {
        val userData = viewModel.userData.value ?: return

        // Show loading state
        binding.chatButton.isEnabled = false  // Temporarily disable to prevent multiple clicks
        binding.chatButton.alpha = 0.7f  // Reduce opacity to show it's been clicked
        binding.chatButton.text = "Đang kết nối..."  // Update button text to show loading state

        // Call API to get or create conversation
        viewModel.getOrCreateConversation(userData.id ?: return)

        // Observe conversation result
        viewModel.conversation.observe(viewLifecycleOwner) { conversation ->
            // Navigate to chat with the conversation ID
            val chatRoomFragment = ChatFragment()
            val bundle = Bundle().apply {
                putInt("conversationId", conversation.id)
                putString("conversationType", conversation.type)
                putString("conversationName", conversation.name ?: userData.username)
                putString("conversationThumbnail", conversation.thumbnail ?: userData.profilePicUrl)
                putBoolean("isDirectChat", true)
            }
            chatRoomFragment.arguments = bundle

            // Navigate to chat fragment
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, chatRoomFragment)
                .addToBackStack("chatRoom")
                .commit()
        }

        // Observe loading state
        viewModel.isConversationLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.chatButton.text = if (isLoading) "Đang kết nối..." else "Nhắn tin"
        }

        // Observe error state
        viewModel.conversationError.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
                // Reset button state
                binding.chatButton.isEnabled = true
                binding.chatButton.alpha = 1.0f
                binding.chatButton.text = "Nhắn tin"
            }
        }
    }

    companion object {
        fun newInstance(userId: Int): UserProfileFragment {
            val fragment = UserProfileFragment()
            val args = Bundle().apply {
                putInt("userId", userId)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
