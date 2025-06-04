package com.example.chatapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.chatapp.ui.auth.login.LoginFragment
import com.example.chatapp.ui.home.HomeFragment
import com.example.chatapp.ui.home.chat.ChatFragment
import com.example.chatapp.ui.home.contacts.ContactsFragment
import com.example.chatapp.ui.home.profile.ProfileFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Only load fragment if this is the first time the activity is created
        if (savedInstanceState == null) {
            handleNavigation()
        }
    }

    private fun handleNavigation() {
        val navigateTo = intent.getStringExtra("navigate_to")
        
        when (navigateTo) {
            "home" -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commitAllowingStateLoss()
            }
            "login", null -> {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, LoginFragment())
                    .commitAllowingStateLoss()
            }
        }
    }

    // Method to navigate to a specific fragment
    fun navigateToFragment(fragment: Fragment, addToBackStack: Boolean = true) {
        val transaction = supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)

        if (addToBackStack) {
            transaction.addToBackStack(null)
        }

        transaction.commitAllowingStateLoss()
    }

    // Method to navigate to a specific tab in the home screen
    fun navigateToHomeTab(tabIndex: Int) {
        // First check if we're already on HomeFragment
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)

        if (currentFragment !is HomeFragment) {
            // If not on HomeFragment, navigate to it first
            navigateToFragment(HomeFragment(), false)

            // Need to post this action because the HomeFragment needs time to initialize
            findViewById<android.view.View>(android.R.id.content).post {
                // Now navigate to the specific tab
                when (tabIndex) {
                    0 -> navigateToSpecificTab(ChatFragment())
                    1 -> navigateToSpecificTab(ContactsFragment())
                    2 -> navigateToSpecificTab(ProfileFragment())
                }
            }
        } else {
            // If already on HomeFragment, just navigate to the specific tab
            when (tabIndex) {
                0 -> navigateToSpecificTab(ChatFragment())
                1 -> navigateToSpecificTab(ContactsFragment())
                2 -> navigateToSpecificTab(ProfileFragment())
            }
        }
    }

    private fun navigateToSpecificTab(fragment: Fragment) {
        val homeFragment = supportFragmentManager.findFragmentById(R.id.fragment_container) as? HomeFragment
        homeFragment?.loadFragment(fragment)
    }
}
