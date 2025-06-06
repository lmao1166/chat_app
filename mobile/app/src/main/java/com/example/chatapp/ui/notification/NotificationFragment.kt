package com.example.chatapp.ui.notification

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentNotificationBinding
import com.example.chatapp.model.response.NotificationResponse
import com.example.chatapp.ui.home.chat.ChatFragment
import com.example.chatapp.viewmodel.NotificationViewModel

class NotificationFragment : Fragment() {
    private lateinit var binding: FragmentNotificationBinding
    private lateinit var viewModel: NotificationViewModel
    private lateinit var notificationAdapter: NotificationAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_notification, container, false)
        
        setupViewModel()
        setupRecyclerView()
        setupClickListeners()
        observeViewModel()
        
        // Load notifications
        viewModel.loadNotifications()
        viewModel.loadUnreadCount()
        
        return binding.root
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[NotificationViewModel::class.java]
    }

    private fun setupRecyclerView() {
        notificationAdapter = NotificationAdapter(
            onNotificationClick = { notification ->
                handleNotificationClick(notification)
            },
            onMarkAsRead = { notification ->
                viewModel.markAsRead(notification.notification_id.toString())
            },
            onDelete = { notification ->
                showDeleteConfirmation(notification)
            }
        )

        binding.notificationsRecyclerView.apply {
            adapter = notificationAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupClickListeners() {
        binding.markAllReadButton.setOnClickListener {
            viewModel.markAllAsRead()
        }
        
        binding.swipeRefreshLayout.setOnRefreshListener {
            refreshNotifications()
        }
    }

    private fun observeViewModel() {
        viewModel.notifications.observe(viewLifecycleOwner) { notifications ->
            if (notifications.isEmpty()) {
                showEmptyState()
            } else {
                hideEmptyState()
                notificationAdapter.submitList(notifications)
            }
        }

        viewModel.unreadCount.observe(viewLifecycleOwner) { count ->
            binding.unreadCountText.text = if (count > 0) {
                "Bạn có $count thông báo chưa đọc"
            } else {
                "Tất cả thông báo đã được đọc"
            }
            
            binding.markAllReadButton.visibility = if (count > 0) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.swipeRefreshLayout.isRefreshing = isLoading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error.isNotEmpty()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun handleNotificationClick(notification: NotificationResponse) {
        when (notification.type) {
            "new_message" -> {
                // Navigate to chat with related entity (message/conversation)
                notification.related_entity_id?.let { messageId ->
                    navigateToChat(messageId)
                }
            }
            "friend_request" -> {
                // Navigate to contacts/friend requests
                // TODO: Implement friend request navigation
            }
            else -> {
                // Default behavior - just mark as read
            }
        }
    }

    private fun navigateToChat(messageId: Int) {
        try {
            // Create ChatFragment with message context
            val chatFragment = ChatFragment()
            val bundle = Bundle().apply {
                putInt("messageId", messageId)
                putBoolean("isDirectChat", true)
            }
            chatFragment.arguments = bundle

            // Navigate to chat
            requireActivity().supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, chatFragment)
                .addToBackStack("notification_to_chat")
                .commit()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Không thể mở cuộc trò chuyện", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showDeleteConfirmation(notification: NotificationResponse) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xóa thông báo")
            .setMessage("Bạn có chắc chắn muốn xóa thông báo này?")
            .setPositiveButton("Xóa") { _, _ ->
                viewModel.deleteNotification(notification.notification_id.toString())
            }
            .setNegativeButton("Hủy", null)
            .show()
    }

    private fun refreshNotifications() {
        viewModel.loadNotifications()
        viewModel.loadUnreadCount()
    }

    private fun showEmptyState() {
        binding.notificationsRecyclerView.visibility = View.GONE
        binding.emptyStateView.visibility = View.VISIBLE
    }

    private fun hideEmptyState() {
        binding.notificationsRecyclerView.visibility = View.VISIBLE
        binding.emptyStateView.visibility = View.GONE
    }
}
