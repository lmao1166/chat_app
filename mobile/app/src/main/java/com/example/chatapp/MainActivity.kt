package com.example.chatapp

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.chatapp.ui.auth.login.LoginFragment
import com.example.chatapp.ui.home.HomeFragment

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
                // Chuyển thẳng đến HomeFragment (đã đăng nhập)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, HomeFragment())
                    .commit()
            }
            "login", null -> {
                // Chuyển đến LoginFragment (chưa đăng nhập hoặc token hết hạn)
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, LoginFragment())
                    .commit()
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

        transaction.commit()
    }
}
