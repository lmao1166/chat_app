package com.example.chatapp.ui.home.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.chatapp.MainActivity
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentProfileBinding
import com.example.chatapp.ui.auth.login.LoginFragment
import com.example.chatapp.utils.TokenManager
import com.squareup.picasso.Picasso

class ProfileFragment : Fragment() {
    private lateinit var binding: FragmentProfileBinding
    private lateinit var viewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_profile, container, false)
        viewModel = ViewModelProvider(this)[ProfileViewModel::class.java]

        setupObservers()
        setupClickListeners()

        return binding.root
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) {
            // You could show a loading indicator here if needed
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }

        viewModel.userProfile.observe(viewLifecycleOwner) { user ->
            // Use username field instead of name
            binding.userName.text = user?.username
            binding.userEmail.text = user?.email

            // Load profile image if available
            user?.profilePicUrl?.let { profileUrl ->
                if (profileUrl.isNotEmpty()) {
                    Picasso.get()
                        .load(profileUrl)
                        .placeholder(R.drawable.default_avatar)
                        .error(R.drawable.default_avatar)
                        .into(binding.profileImage)
                }
            }
        }

        viewModel.logoutStatus.observe(viewLifecycleOwner) { success ->
            if (success) {
//                // Xóa token khi đăng xuất thành công
                val tokenManager = TokenManager.getInstance(requireContext())
                tokenManager.clearTokens()

                Toast.makeText(context, "Đã đăng xuất thành công", Toast.LENGTH_SHORT).show()

                // Chuyển hướng về trang đăng nhập
                (activity as? MainActivity)?.navigateToFragment(LoginFragment(), false)
            }
        }
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
            // Hiển thị hộp thoại xác nhận trước khi đăng xuất
            showLogoutConfirmationDialog()
        }
    }

    private fun showLogoutConfirmationDialog() {
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận đăng xuất")
            .setMessage("Bạn có chắc chắn muốn đăng xuất không?")
            .setPositiveButton("Đăng xuất") { _, _ ->
                // Gọi API đăng xuất
                viewModel.logout()
            }
            .setNegativeButton("Hủy", null)
            .create()

        alertDialog.show()
    }

    private fun navigateToFragment(fragment: Fragment) {
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)  // Sửa từ fragment_container thành fragmentContainer (không có dấu gạch dưới)
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        // Refresh user profile data when fragment resumes
        viewModel.fetchUserProfile()
    }
}
