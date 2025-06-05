package com.example.chatapp.ui.home.contacts

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.chatapp.databinding.ItemContactBinding
import com.example.chatapp.model.response.UserResponse
//import com.squareup.picasso.Picasso
import com.example.chatapp.R
import com.squareup.picasso.Picasso

class ContactsAdapter(
    private val onProfileClick: (UserResponse) -> Unit,
    private val onChatClick: (UserResponse) -> Unit
) : ListAdapter<UserResponse, ContactsAdapter.ContactViewHolder>(ContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val binding = ItemContactBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ContactViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ContactViewHolder(
        private val binding: ItemContactBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        @SuppressLint("SetTextI18n")
        fun bind(user: UserResponse) {
            binding.apply {
                contactName.text = user.username ?: "Unknown User"
                contactStatus.text = "Offline" // You can implement online status later

                // Load profile image
                if (!user.profilePicUrl.isNullOrEmpty()) {
                    Picasso.get()
                        .load(user.profilePicUrl)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .fit()
                        .centerCrop()
                        .into(contactImage)
                } else {
                    contactImage.setImageResource(R.drawable.default_avatar)
                }

                // Handle click events - view profile when clicking on the contact
                root.setOnClickListener {
                    onProfileClick(user)
                }

                // Chat button leads to chat room
                contactActionButton.setOnClickListener {
                    onChatClick(user)
                }
            }
        }
    }

    class ContactDiffCallback : DiffUtil.ItemCallback<UserResponse>() {
        override fun areItemsTheSame(oldItem: UserResponse, newItem: UserResponse): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: UserResponse, newItem: UserResponse): Boolean {
            return oldItem == newItem
        }
    }
}
