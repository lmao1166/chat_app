package com.example.chatapp.ui.home.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.chatapp.MainActivity
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentProfileBinding
import com.example.chatapp.ui.auth.login.LoginFragment
import com.example.chatapp.utils.TokenManager

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)

        setupUserInfo()
        setupClickListeners()

        return binding.root
    }

    @SuppressLint("SetTextI18n")
    private fun setupUserInfo() {
        // Here you would load the user's information from your data source
        // For now, we'll use placeholder data
        binding.userName.text = "Nguyễn Văn A"
        binding.userEmail.text = "example@email.com"

        // You would also load the user's profile image here
        // binding.profileImage.setImageBitmap(...)
    }

    private fun setupClickListeners() {
        binding.editProfileOption.setOnClickListener {
            // Navigate to edit profile screen
            navigateToFragment(EditProfileFragment())
        }

        binding.changePasswordOption.setOnClickListener {
            // Navigate to change password screen
            navigateToFragment(ChangePasswordFragment())
        }

        binding.logoutOption.setOnClickListener {
            // Handle logout
            val tokenManager = TokenManager.getInstance(requireContext())
            tokenManager.clearTokens()
            Toast.makeText(context, "Đã đăng xuất", Toast.LENGTH_SHORT).show()

            // Navigate back to login screen
            (activity as? MainActivity)?.navigateToFragment(LoginFragment(), false)
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
    }

}
