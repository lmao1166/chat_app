package com.example.chatapp.ui.splash

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import com.example.chatapp.MainActivity
import com.example.chatapp.R
import com.example.chatapp.utils.TokenManager

class SplashActivity : AppCompatActivity() {
    
    private companion object {
        private const val SPLASH_DELAY = 2000L // 2 giây
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        // Kiểm tra trạng thái đăng nhập sau một khoảng thời gian ngắn
        Handler(Looper.getMainLooper()).postDelayed({
            checkLoginStatus()
        }, SPLASH_DELAY)
    }

    private fun checkLoginStatus() {
        val tokenManager = TokenManager.getInstance(this)
        
        if (tokenManager.isLoggedIn()) {
            // Token còn hạn, chuyển thẳng đến HomeFragment
            navigateToHome()
        } else {
            // Token hết hạn hoặc chưa đăng nhập, chuyển đến LoginFragment
            navigateToLogin()
        }
    }

    private fun navigateToHome() {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "home")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("navigate_to", "login")
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
