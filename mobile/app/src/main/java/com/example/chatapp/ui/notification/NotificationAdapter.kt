package com.example.chatapp.ui.notification

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.databinding.ItemNotificationBinding
import com.example.chatapp.model.response.NotificationResponse
import java.text.SimpleDateFormat
import java.util.*

class NotificationAdapter(
    private val onNotificationClick: (NotificationResponse) -> Unit,
    private val onMarkAsRead: (NotificationResponse) -> Unit,
    private val onDelete: (NotificationResponse) -> Unit
) : ListAdapter<NotificationResponse, NotificationAdapter.NotificationViewHolder>(NotificationDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NotificationViewHolder {
        val binding = ItemNotificationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return NotificationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: NotificationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class NotificationViewHolder(
        private val binding: ItemNotificationBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(notification: NotificationResponse) {
            binding.apply {
                // Set notification content
                notificationTitle.text = getNotificationTitle(notification.type)
                notificationContent.text = notification.content
                notificationTime.text = formatTime(notification.created_at)

                // Set read/unread state
                if (notification.is_read) {
                    root.alpha = 0.6f
                    unreadIndicator.visibility = android.view.View.GONE
                } else {
                    root.alpha = 1.0f
                    unreadIndicator.visibility = android.view.View.VISIBLE
                }

                // Set notification icon based on type
                notificationIcon.setImageResource(getNotificationIcon(notification.type))

                // Click listeners
                root.setOnClickListener {
                    onNotificationClick(notification)
                    if (!notification.is_read) {
                        onMarkAsRead(notification)
                    }
                }

                // Long click for delete
                root.setOnLongClickListener {
                    onDelete(notification)
                    true
                }
            }
        }

        private fun getNotificationTitle(type: String): String {
            return when (type) {
                "new_message" -> "Tin nhắn mới"
                "friend_request" -> "Lời mời kết bạn"
                "system" -> "Thông báo hệ thống"
                else -> "Thông báo"
            }
        }

        private fun getNotificationIcon(type: String): Int {
            return when (type) {
                "new_message" -> R.drawable.ic_chat
                "friend_request" -> R.drawable.ic_contacts
                "system" -> R.drawable.ic_notifications
                else -> R.drawable.ic_notifications
            }
        }

        private fun formatTime(timestamp: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val date = inputFormat.parse(timestamp)
                
                val now = Date()
                val diffInMillis = now.time - (date?.time ?: 0)
                val diffInMinutes = diffInMillis / (1000 * 60)
                val diffInHours = diffInMinutes / 60
                val diffInDays = diffInHours / 24

                when {
                    diffInMinutes < 1 -> "Vừa xong"
                    diffInMinutes < 60 -> "${diffInMinutes.toInt()} phút trước"
                    diffInHours < 24 -> "${diffInHours.toInt()} giờ trước"
                    diffInDays < 7 -> "${diffInDays.toInt()} ngày trước"
                    else -> {
                        val outputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                        outputFormat.format(date ?: Date())
                    }
                }
            } catch (e: Exception) {
                timestamp
            }
        }
    }

    class NotificationDiffCallback : DiffUtil.ItemCallback<NotificationResponse>() {
        override fun areItemsTheSame(oldItem: NotificationResponse, newItem: NotificationResponse): Boolean {
            return oldItem.notification_id == newItem.notification_id
        }

        override fun areContentsTheSame(oldItem: NotificationResponse, newItem: NotificationResponse): Boolean {
            return oldItem == newItem
        }
    }
}
