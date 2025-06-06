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

        // Initialize authentication for API requests
        initializeAuthentication()

        // Only load fragment if this is the first time the activity is created
        if (savedInstanceState == null) {
            handleNavigation()
        }
    }

    private fun initializeAuthentication() {
        // Update Retrofit with the authentication token (if available)
        RetrofitInstance.updateWithToken(applicationContext)
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

}
