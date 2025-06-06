package com.example.chatapp.utils

import android.view.View
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.example.chatapp.R
import com.example.chatapp.viewmodel.NotificationViewModel
import com.google.android.material.badge.BadgeDrawable
import com.google.android.material.bottomnavigation.BottomNavigationView

class NotificationBadgeHelper(
    private val bottomNavigationView: BottomNavigationView,
    private val notificationViewModel: NotificationViewModel,
    private val lifecycleOwner: LifecycleOwner
) {
    private var badge: BadgeDrawable? = null

    fun setupBadge() {
        // Create badge for notifications tab
        badge = bottomNavigationView.getOrCreateBadge(R.id.nav_notifications)
        badge?.apply {
            backgroundColor = android.graphics.Color.parseColor("#FF4444")
            badgeTextColor = android.graphics.Color.WHITE
            isVisible = false
        }

        // Observe unread count
        notificationViewModel.unreadCount.observe(lifecycleOwner, Observer { count ->
            updateBadge(count)
        })

        // Load initial unread count
        notificationViewModel.loadUnreadCount()
    }

    private fun updateBadge(unreadCount: Int) {
        badge?.let { badgeDrawable ->
            if (unreadCount > 0) {
                badgeDrawable.number = unreadCount
                badgeDrawable.isVisible = true
            } else {
                badgeDrawable.isVisible = false
            }
        }
    }

    fun removeBadge() {
        bottomNavigationView.removeBadge(R.id.nav_notifications)
        badge = null
    }
}
