package com.example.chatapp.ui.home.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.chatapp.MainActivity
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentChangePasswordBinding

class ChangePasswordFragment : Fragment() {
    private lateinit var binding: FragmentChangePasswordBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Sử dụng data binding để inflate layout
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_change_password, container, false)

        // Thiết lập các sự kiện click
        setupClickListeners()

        return binding.root
    }

    private fun setupClickListeners() {
        // Xử lý sự kiện đổi mật khẩu
        binding.changePasswordButton.setOnClickListener {
            // Lấy dữ liệu từ các trường
            val currentPassword = binding.currentPasswordEditText.text.toString().trim()
            val newPassword = binding.newPasswordEditText.text.toString().trim()
            val confirmNewPassword = binding.confirmPasswordEditText.text.toString().trim()

            // Kiểm tra dữ liệu nhập vào
            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmNewPassword.isEmpty()) {
                Toast.makeText(context, "Vui lòng điền đầy đủ thông tin", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword != confirmNewPassword) {
                Toast.makeText(context, "Mật khẩu mới không khớp", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (newPassword.length < 6) {
                Toast.makeText(context, "Mật khẩu mới phải có ít nhất 6 ký tự", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Hiển thị dialog xác nhận
            showConfirmationDialog()
        }

        binding.backButton.setOnClickListener{
            parentFragmentManager.popBackStack()
        }
    }

    private fun showConfirmationDialog() {
        // Hiển thị AlertDialog để xác nhận thay đổi mật khẩu
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận thay đổi")
            .setMessage("Bạn có chắc muốn đổi mật khẩu?")
            .setPositiveButton("Xác nhận") { _, _ ->
                // Mô phỏng việc đổi mật khẩu thành công
                changePassword()
            }
            .setNegativeButton("Hủy", null)
            .create()

        alertDialog.show()
    }

    private fun changePassword() {
        // TODO: Thực hiện thay đổi mật khẩu thực tế, gọi API hoặc thay đổi trong database

        // Hiển thị thông báo thành công
        Toast.makeText(context, "Đã đổi mật khẩu thành công", Toast.LENGTH_SHORT).show()

        // Quay lại màn hình Profile
        parentFragmentManager.popBackStack()
    }
}
