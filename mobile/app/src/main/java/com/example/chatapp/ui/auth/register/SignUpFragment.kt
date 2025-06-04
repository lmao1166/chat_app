package com.example.chatapp.ui.auth.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.chatapp.MainActivity
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentSignupBinding
import com.example.chatapp.ui.auth.login.LoginFragment

class SignUpFragment : Fragment() {
    private lateinit var binding: FragmentSignupBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout using DataBindingUtil
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signup, container, false)

        // Set up event listeners after binding is initialized
        setUpListeners()

        return binding.root
    }

    private fun setUpListeners() {
        binding.goLoginScreen.setOnClickListener {
            (activity as? MainActivity)?.navigateToFragment(LoginFragment(), true)
        }
        binding.returnButton.setOnClickListener {
            // Navigate back to the previous fragment
            (activity as? MainActivity)?.onBackPressedDispatcher?.onBackPressed()
        }

        // Add event listener for the signup button
        binding.signupButton.setOnClickListener {
            val username = binding.username.text.toString().trim()
            val email = binding.email.text.toString().trim()
            val password = binding.password.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()

            // TODO: Implement signup functionality with username validation
            // For now, just print the values to verify functionality
            println("Username: $username")
            println("Email: $email")
            println("Password validation: ${password == confirmPassword}")
        }
    }
}
