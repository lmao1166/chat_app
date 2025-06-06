package com.example.chatapp.ui.home.chat

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.databinding.ItemConversationBinding
import com.example.chatapp.model.response.ConversationResponse
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*
import java.util.TimeZone

class ConversationAdapter(
    private val onConversationClick: (ConversationResponse) -> Unit
) : ListAdapter<ConversationResponse, ConversationAdapter.ConversationViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val binding = ItemConversationBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return ConversationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ConversationViewHolder(
        private val binding: ItemConversationBinding
    ) : RecyclerView.ViewHolder(binding.root) {        @SuppressLint("SetTextI18n")
        fun bind(conversation: ConversationResponse) {
            // Lấy thông tin của member khác (không phải user hiện tại)
            val otherMember = conversation.members.firstOrNull()
            
            // Hiển thị tên
            binding.contactName.text = otherMember?.username ?: "Unknown"
            
            // Hiển thị tin nhắn cuối cùng
            if (conversation.lastMessage != null) {
                val lastMsg = conversation.lastMessage
                when (lastMsg.messageType) {
                    "image" -> {
                        binding.lastMessage.text = "📷 Hình ảnh"
                    }
                    "file" -> {
                        binding.lastMessage.text = "📎 File đính kèm"
                    }
                    else -> {
                        binding.lastMessage.text = lastMsg.content
                    }
                }
                // Hiển thị thời gian tin nhắn cuối
                binding.lastMessageTime.text = formatTime(lastMsg.timestamp)
            } else {
                binding.lastMessage.text = "Nhấn để bắt đầu trò chuyện"
                binding.lastMessageTime.text = formatTime(conversation.createdAt)
            }
            
            // Ẩn badge số tin nhắn chưa đọc (có thể implement sau)
            binding.unreadCount.visibility = android.view.View.GONE
            
            // Load avatar - prioritize conversation thumbnail, then member's profile pic
            val avatarUrl = if (!conversation.thumbnail.isNullOrEmpty()) {
                conversation.thumbnail
            } else {
                otherMember?.profilePicUrl
            }
            
            if (!avatarUrl.isNullOrEmpty()) {
                // Check if URL already contains the full path (from backend) or just filename
                val fullAvatarUrl = if (avatarUrl.startsWith("http")) {
                    avatarUrl // Already a full URL from backend
                } else {
                    "http://192.168.1.10:3000/api/v1/uploads/profiles/$avatarUrl" // Just filename, add prefix
                }
                
                Picasso.get()
                    .load(fullAvatarUrl)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .into(binding.profileImage)
            } else {
                binding.profileImage.setImageResource(R.drawable.default_avatar)
            }
            
            // Click listener
            binding.root.setOnClickListener {
                onConversationClick(conversation)
            }
        }
          private fun formatTime(dateString: String): String {
            return try {
                val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
                inputFormat.timeZone = TimeZone.getTimeZone("UTC")
                val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
                val date = inputFormat.parse(dateString)
                outputFormat.format(date ?: Date())
            } catch (e: Exception) {
                "00:00"
            }
        }
    }    class DiffCallback : DiffUtil.ItemCallback<ConversationResponse>() {
        override fun areItemsTheSame(oldItem: ConversationResponse, newItem: ConversationResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: ConversationResponse, newItem: ConversationResponse): Boolean {
            return oldItem == newItem
        }
    }
}
