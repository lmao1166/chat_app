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
import com.example.chatapp.model.response.UserResponse
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

            // Debug logging
            android.util.Log.d("ProfileFragment", "User data: $user")
            android.util.Log.d("ProfileFragment", "ProfilePicUrl: ${user?.profilePicUrl}")

            // Load profile image if available
            user?.profilePicUrl?.let { profileUrl ->
                if (profileUrl.isNotEmpty()) {
                    android.util.Log.d("ProfileFragment", "Loading image from: $profileUrl")

                    // Đảm bảo URL không có khoảng trắng và được định dạng đúng
                    val cleanUrl = profileUrl.trim()

                    // Xử lý đặc biệt cho URL localhost - chuyển sang IP thực của máy phát triển
                    val finalUrl = cleanUrl.replace("http://localhost", "http://192.168.1.10")

                    // Debug URL
                    android.util.Log.d("ProfileFragment", "Final URL: $finalUrl")

                    try {
                        // Tắt SSL check khi chạy debug (chỉ dùng trong phát triển)
                        if (finalUrl.startsWith("http://")) {
                            android.util.Log.d("ProfileFragment", "Using non-HTTPS URL")
                        }

                        // Thêm logging cho Picasso
                        Picasso.get().setLoggingEnabled(true)

                        // Tải ảnh với cài đặt cơ bản trước
                        Picasso.get()
                            .load(finalUrl)
                            .placeholder(R.drawable.default_avatar)
                            .error(R.drawable.default_avatar)
                            .into(binding.profileImage, object : com.squareup.picasso.Callback {
                                override fun onSuccess() {
                                    android.util.Log.d("ProfileFragment", "Image loaded successfully")
                                }
                                override fun onError(e: Exception?) {
                                    android.util.Log.e("ProfileFragment", "Image load error: ${e?.message}", e)
                                    binding.profileImage.setImageResource(R.drawable.default_avatar)

                                    // Hiển thị thông báo lỗi để debug
                                    Toast.makeText(
                                        requireContext(),
                                        "Không thể tải ảnh: ${e?.message ?: "Lỗi không xác định"}",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            })
                    } catch (e: Exception) {
                        android.util.Log.e("ProfileFragment", "Exception loading image: ${e.message}", e)
                        binding.profileImage.setImageResource(R.drawable.default_avatar)
                    }
                } else {
                    android.util.Log.d("ProfileFragment", "ProfileUrl is empty, using default avatar")
                    // Set default avatar if no profile picture
                    binding.profileImage.setImageResource(R.drawable.default_avatar)
                }
            } ?: run {
                android.util.Log.d("ProfileFragment", "ProfilePicUrl is null, using default avatar")
                // Set default avatar if profilePicUrl is null
                binding.profileImage.setImageResource(R.drawable.default_avatar)
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
    }    private fun setupClickListeners() {
        binding.editProfileOption.setOnClickListener {
            // Navigate to edit profile screen with current user data
            val currentUser = viewModel.userProfile.value
            navigateToEditProfile(currentUser)
        }

        binding.changePasswordOption.setOnClickListener {
            // Navigate to change password screen
            navigateToFragment(ChangePasswordFragment())
        }

        binding.logoutOption.setOnClickListener {
            // Hiển thị hộp thoại xác nhận trước khi đăng xuất
            showLogoutConfirmationDialog()
        }
    }    private fun navigateToEditProfile(user: UserResponse?) {
        val fragment = EditProfileFragment().apply {
            arguments = Bundle().apply {
                user?.let {
                    putString("username", it.username)
                    putString("profilePicUrl", it.profilePicUrl)
                }
            }
        }
        parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .addToBackStack(null)
            .commit()
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
