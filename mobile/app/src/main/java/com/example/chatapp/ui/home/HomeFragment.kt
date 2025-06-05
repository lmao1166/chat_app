package com.example.chatapp.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentHomeBinding
import com.example.chatapp.ui.home.chat.ChatFragment
import com.example.chatapp.ui.home.contacts.ContactsFragment
import com.example.chatapp.ui.home.profile.ProfileFragment

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)

        // Set initial fragment
        if (savedInstanceState == null) {
            loadFragment(ChatFragment())
        }

        setupBottomNavigation()
        return binding.root
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    loadFragment(ChatFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.nav_contacts -> {
                    loadFragment(ContactsFragment())
                    return@setOnItemSelectedListener true
                }
                R.id.nav_profile -> {
                    loadFragment(ProfileFragment())
                    return@setOnItemSelectedListener true
                }
            }
            false
        }
    }

    fun loadFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    // Method to highlight a specific navigation item
    fun selectNavigationItem(itemId: Int) {
        binding.bottomNavigation.selectedItemId = itemId
    }
}
