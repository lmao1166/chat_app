package com.example.chatapp.ui.home.profile

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.chatapp.MainActivity
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentChangePasswordBinding
import com.example.chatapp.model.request.ChangePasswordRequest
import com.example.chatapp.repository.UserRepository
import com.example.chatapp.ui.auth.login.LoginFragment
import com.example.chatapp.utils.TokenManager
import com.example.chatapp.viewmodel.ChangePasswordViewModel
import com.example.chatapp.viewmodel.ChangePasswordViewModelFactory
import kotlinx.coroutines.launch

class ChangePasswordFragment : Fragment() {
    private lateinit var binding: FragmentChangePasswordBinding
    private lateinit var viewModel: ChangePasswordViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_change_password, container, false)

        val userRepository = UserRepository()
        val factory = ChangePasswordViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this, factory)[ChangePasswordViewModel::class.java]

        setupClickListeners()
        observeViewModel()

        return binding.root
    }

    private fun setupClickListeners() {
        binding.changePasswordButton.setOnClickListener {
            val currentPassword = binding.currentPasswordEditText.text.toString().trim()
            val newPassword = binding.newPasswordEditText.text.toString().trim()
            val confirmNewPassword = binding.confirmPasswordEditText.text.toString().trim()

            val changePasswordRequest = ChangePasswordRequest(currentPassword, newPassword, confirmNewPassword)
            showConfirmationDialog(changePasswordRequest)
        }

        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }
    }

    @SuppressLint("SetTextI18x")
    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                // Hiển thị loading với ProgressBar
                if (state.isLoading) {
                    binding.changePasswordButton.isEnabled = false
                    binding.progressBar.visibility = View.VISIBLE
                } else {
                    binding.changePasswordButton.isEnabled = true
                    binding.progressBar.visibility = View.GONE
                }

                // Xử lý thành công
                if (state.isSuccess) {
                    handleSuccessfulPasswordChange(state.successMessage)
                    viewModel.clearSuccess()
                    return@collect  // Stop processing here when we have success
                }

                // Xử lý lỗi (only if not success)
                state.error?.let { error ->
                    Toast.makeText(context, error, Toast.LENGTH_SHORT).show()

                    // Xử lý token hết hạn
                    if (error.contains("401") || error.contains("quyền truy cập") || error.contains("Unauthorized")) {
                        handleExpiredToken()
                    }

                    viewModel.clearError()
                }
            }
        }
    }

    private fun handleExpiredToken() {
        Toast.makeText(context, "Phiên đăng nhập đã hết hạn, vui lòng đăng nhập lại", Toast.LENGTH_LONG).show()
        val tokenManager = TokenManager.getInstance(requireContext())
        tokenManager.clearTokens()
        (activity as? MainActivity)?.navigateToFragment(LoginFragment(), false)
    }

    private fun handleSuccessfulPasswordChange(successMessage: String?) {
        val message = successMessage ?: "Đổi mật khẩu thành công"

        // Kiểm tra context tồn tại và fragment đang gắn với activity
        activity?.let { safeActivity ->
            if (isAdded && !isDetached) {
                Toast.makeText(safeActivity, message, Toast.LENGTH_SHORT).show()

                // Clear input fields
                binding.currentPasswordEditText.text?.clear()
                binding.newPasswordEditText.text?.clear()
                binding.confirmPasswordEditText.text?.clear()

                // Trì hoãn việc quay lại màn hình trước để Toast có thể hiển thị
                binding.root.postDelayed({
                    if (isAdded && !isDetached) {
                        parentFragmentManager.popBackStack()
                    }
                }, 500) // Trì hoãn 500ms
            }
        }
    }

    private fun showConfirmationDialog(request: ChangePasswordRequest) {
        AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận đổi mật khẩu")
            .setMessage("Bạn có chắc chắn muốn đổi mật khẩu? Bạn sẽ cần đăng nhập lại bằng mật khẩu mới.")
            .setPositiveButton("Xác nhận") { _, _ ->
                viewModel.changePassword(request)
            }
            .setNegativeButton("Hủy", null)
            .show()
    }
}
