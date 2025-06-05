package com.example.chatapp.ui.home.contacts.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.util.Log
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
import com.example.chatapp.ui.home.HomeFragment
import com.squareup.picasso.Picasso

class UserProfileFragment : Fragment() {

    private lateinit var binding: FragmentUserProfileBinding
    private lateinit var viewModel: UserProfileViewModel
    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            userId = it.getString("userId")
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

        // Highlight the chat button to provide visual feedback
        binding.chatButton.isEnabled = false  // Temporarily disable to prevent multiple clicks
        binding.chatButton.alpha = 0.7f  // Reduce opacity to show it's been clicked
        binding.chatButton.setBackgroundColor(resources.getColor(R.color.green, null))  // Change background color to green
        binding.chatButton.text = "Đang trò chuyện..."  // Update button text

        // Create and navigate to chat fragment
        val chatRoomFragment = ChatFragment()
        val bundle = Bundle().apply {
            putString("userId", userData.id.toString())
            putString("userName", userData.username)
            putString("userAvatar", userData.profilePicUrl)
            putBoolean("isDirectChat", true)  // Flag to indicate this is a direct chat from profile
        }
        chatRoomFragment.arguments = bundle

        // Find the parent HomeFragment to highlight the chat menu button
        requireActivity().supportFragmentManager.fragments.forEach { fragment ->
            if (fragment is HomeFragment) {
                // Highlight the chat navigation item
                fragment.selectNavigationItem(R.id.nav_chat)
            }
        }

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, chatRoomFragment)
            .addToBackStack("chatRoom")  // Named backstack entry for better navigation
            .commit()
    }

    companion object {
        fun newInstance(userId: String): UserProfileFragment {
            val fragment = UserProfileFragment()
            val args = Bundle().apply {
                putString("userId", userId)
            }
            fragment.arguments = args
            return fragment
        }
    }
}
