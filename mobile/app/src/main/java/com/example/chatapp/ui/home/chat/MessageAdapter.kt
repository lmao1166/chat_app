package com.example.chatapp.ui.home.chat

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.R
import com.example.chatapp.databinding.ItemMessageReceivedBinding
import com.example.chatapp.databinding.ItemMessageSentBinding
import com.example.chatapp.model.response.MessageResponse
import com.example.chatapp.ui.imageviewer.ImageViewerActivity
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.*

class MessageAdapter(private val currentUserId: String) : ListAdapter<MessageResponse, RecyclerView.ViewHolder>(DiffCallback()) {

    companion object {
        private const val VIEW_TYPE_SENT = 1
        private const val VIEW_TYPE_RECEIVED = 2
    }    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.sender.id.toString() == currentUserId) {
            VIEW_TYPE_SENT
        } else {
            VIEW_TYPE_RECEIVED
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SENT -> {
                val binding = ItemMessageSentBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                SentMessageViewHolder(binding)
            }
            VIEW_TYPE_RECEIVED -> {
                val binding = ItemMessageReceivedBinding.inflate(
                    LayoutInflater.from(parent.context), parent, false
                )
                ReceivedMessageViewHolder(binding)
            }
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val message = getItem(position)
        when (holder) {
            is SentMessageViewHolder -> holder.bind(message)
            is ReceivedMessageViewHolder -> holder.bind(message)
        }
    }

    inner class SentMessageViewHolder(private val binding: ItemMessageSentBinding) :
        RecyclerView.ViewHolder(binding.root) {        fun bind(message: MessageResponse) {
            // Handle text content visibility
            if (message.content.isNotEmpty() && message.content != "📷 Đã gửi một hình ảnh") {
                binding.messageContent.visibility = View.VISIBLE
                binding.messageContent.text = message.content
            } else {
                binding.messageContent.visibility = View.GONE
            }
              binding.messageTime.text = formatTime(message.timestamp)
              // Handle attachment if available
            if (!message.attachmentUrl.isNullOrEmpty() && 
                message.attachmentUrl != "null" && 
                message.attachmentUrl.startsWith("http")) {
                binding.attachmentContainer.visibility = View.VISIBLE
                
                // Use the existing ImageView from layout
                val imageView = binding.attachmentImage

                // Backend should return full URL, use it directly
                val imageUrl = message.attachmentUrl

                Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .into(imageView)
                    
                // Add click listener to open image viewer
                imageView.setOnClickListener {
                    val intent = Intent(binding.root.context, ImageViewerActivity::class.java).apply {
                        putExtra(ImageViewerActivity.EXTRA_IMAGE_URL, imageUrl)
                        putExtra(ImageViewerActivity.EXTRA_TITLE, "Hình ảnh của bạn")
                    }
                    binding.root.context.startActivity(intent)
                }
                    
            } else {
                binding.attachmentContainer.visibility = View.GONE
            }
        }
    }

    inner class ReceivedMessageViewHolder(private val binding: ItemMessageReceivedBinding) :
        RecyclerView.ViewHolder(binding.root) {        fun bind(message: MessageResponse) {
            // Handle text content visibility
            if (message.content.isNotEmpty() && message.content != "📷 Đã gửi một hình ảnh") {
                binding.messageContent.visibility = View.VISIBLE
                binding.messageContent.text = message.content
            } else {
                binding.messageContent.visibility = View.GONE
            }
            
            binding.messageTime.text = formatTime(message.timestamp)
            binding.senderName.text = message.sender.username
            
            // Load sender profile picture
            val profilePicUrl = message.sender.profilePicUrl
            if (profilePicUrl != null) {
                val fullUrl = if (profilePicUrl.startsWith("http")) {
                    profilePicUrl
                } else {
                    "http://192.168.1.10:3000/api/v1/uploads/profiles/$profilePicUrl"
                }
                
                Picasso.get()
                    .load(fullUrl)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .into(binding.senderImage)
            } else {
                binding.senderImage.setImageResource(R.drawable.default_avatar)
            }            // Handle attachment if available
            if (!message.attachmentUrl.isNullOrEmpty() && 
                message.attachmentUrl != "null" && 
                message.attachmentUrl.startsWith("http")) {
                binding.attachmentContainer.visibility = View.VISIBLE

                // Use the existing ImageView from layout
                val imageView = binding.attachmentImage

                // Backend should return full URL, use it directly
                val imageUrl = message.attachmentUrl
                Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_image_placeholder)
                    .error(R.drawable.ic_image_error)
                    .into(imageView)
                    
                // Add click listener to open image viewer
                imageView.setOnClickListener {
                    val intent = Intent(binding.root.context, ImageViewerActivity::class.java).apply {
                        putExtra(ImageViewerActivity.EXTRA_IMAGE_URL, imageUrl)
                        putExtra(ImageViewerActivity.EXTRA_TITLE, "Hình ảnh từ ${message.sender.username}")
                    }
                    binding.root.context.startActivity(intent)
                }

            } else {
                binding.attachmentContainer.visibility = View.GONE
            }
        }
    }

    private fun formatTime(timestamp: String): String {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            inputFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = inputFormat.parse(timestamp)
            val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            timestamp
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<MessageResponse>() {
        override fun areItemsTheSame(oldItem: MessageResponse, newItem: MessageResponse): Boolean {
            return oldItem.timestamp == newItem.timestamp && oldItem.sender.id == newItem.sender.id
        }

        override fun areContentsTheSame(oldItem: MessageResponse, newItem: MessageResponse): Boolean {
            return oldItem == newItem
        }
    }
}
