package com.example.chatapp.ui.home.profile

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentEditProfileBinding

class EditProfileFragment : Fragment() {
    private lateinit var binding: FragmentEditProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Sử dụng data binding để inflate layout
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false)

        // Thiết lập các sự kiện click
        setupClickListeners()

        // Khởi tạo dữ liệu người dùng hiện tại
        setupInitialData()

        return binding.root
    }

    private fun setupInitialData() {
        // TODO: Lấy thông tin người dùng hiện tại từ database hoặc preferences
        binding.nameEditText.setText("Nguyễn Văn A")
    }

    private fun setupClickListeners() {
        // Xử lý sự kiện nút quay lại - fixing by correctly finding the button in the Toolbar
        binding.backButton.setOnClickListener{
            parentFragmentManager.popBackStack()
        }

        // Xử lý sự kiện lưu thông tin
        binding.saveButton.setOnClickListener {
            // Giả lập việc cập nhật thông tin
            val name = binding.nameEditText.text.toString().trim()
            if (name.isEmpty()) {
                Toast.makeText(context, "Tên người dùng không được để trống", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Hiển thị dialog xác nhận hoặc trực tiếp hiển thị thông báo
            showConfirmationDialog(name)
        }

        // Xử lý sự kiện thay đổi ảnh đại diện
        binding.profileImage.setOnClickListener {
            // TODO: Thêm chức năng chọn ảnh từ thư viện
            Toast.makeText(context, "Tính năng đang được phát triển", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showConfirmationDialog(name: String) {
        // Hiển thị AlertDialog để xác nhận thay đổi
        val alertDialog = androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Xác nhận thay đổi")
            .setMessage("Bạn có chắc muốn cập nhật thông tin cá nhân?")
            .setPositiveButton("Xác nhận") { _, _ ->
                // Mô phỏng việc lưu thông tin thành công
                saveUserInformation(name)
            }
            .setNegativeButton("Hủy", null)
            .create()

        alertDialog.show()
    }

    private fun saveUserInformation(name: String) {
        // TODO: Thực hiện lưu thông tin vào database hoặc backend

        // Hiển thị thông báo thành công
        Toast.makeText(context, "Đã cập nhật thông tin thành công", Toast.LENGTH_SHORT).show()

        // Quay lại màn hình Profile
        requireActivity().onBackPressedDispatcher.onBackPressed()
    }
}
