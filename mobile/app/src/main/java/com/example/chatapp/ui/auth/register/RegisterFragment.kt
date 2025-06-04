package com.example.chatapp.ui.auth.register

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
import com.example.chatapp.databinding.FragmentSignupBinding
import com.example.chatapp.repository.UserRepository
import com.example.chatapp.ui.auth.login.LoginFragment
import com.example.chatapp.viewmodel.RegisterViewModelFactory

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentSignupBinding
    private lateinit var viewModel: RegisterViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Khởi tạo data binding
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_signup, container, false)

        // Khởi tạo ViewModel
        val userRepository = UserRepository()
        val factory = RegisterViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this, factory)[RegisterViewModel::class.java]

        // Thiết lập sự kiện click cho các button
        setupClickListeners()

        // Theo dõi trạng thái đăng ký
        observeRegistrationStatus()

        return binding.root
    }

    private fun setupClickListeners() {
        // Sự kiện click nút đăng ký
        binding.signupButton.setOnClickListener {
            validateAndRegister()
        }

        // Sự kiện click nút trở về
        binding.returnButton.setOnClickListener {
            // Quay lại màn hình trước đó
            requireActivity().supportFragmentManager.popBackStack()
        }

        // Sự kiện click vào đường dẫn đến trang đăng nhập
        binding.goLoginScreen.setOnClickListener {
            // Chuyển đến fragment đăng nhập
            (activity as? MainActivity)?.navigateToFragment(LoginFragment(), false)
        }
    }

    private fun validateAndRegister() {
        val username = binding.username.text.toString().trim()
        val email = binding.email.text.toString().trim()
        val password = binding.password.text.toString().trim()
        val confirmPassword = binding.confirmPassword.text.toString().trim()

        // Kiểm tra các trường dữ liệu
        if (username.isEmpty()) {
            binding.username.error = "Vui lòng nhập họ và tên"
            return
        }

        if (email.isEmpty()) {
            binding.email.error = "Vui lòng nhập email"
            return
        }

        if (!isValidEmail(email)) {
            binding.email.error = "Email không hợp lệ"
            return
        }

        if (password.isEmpty()) {
            binding.password.error = "Vui lòng nhập mật khẩu"
            return
        }

        if (password.length < 6) {
            binding.password.error = "Mật khẩu phải có ít nhất 6 ký tự"
            return
        }

        if (confirmPassword.isEmpty()) {
            binding.confirmPassword.error = "Vui lòng xác nhận mật khẩu"
            return
        }

        if (password != confirmPassword) {
            binding.confirmPassword.error = "Mật khẩu xác nhận không khớp"
            return
        }

        // Gọi ViewModel để đăng ký
        viewModel.register(username, email, password, confirmPassword)
    }

    private fun isValidEmail(email: String): Boolean {
        // Biểu thức chính quy kiểm tra email
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

    private fun observeRegistrationStatus() {
        viewModel.registrationStatus.observe(viewLifecycleOwner) { status ->
            when (status) {
                is RegistrationStatus.Loading -> {
                    // Hiển thị loading
                    binding.progressBar.visibility = View.VISIBLE
                    binding.signupButton.isEnabled = false
                }

                is RegistrationStatus.Success -> {
                    // Ẩn loading
                    binding.progressBar.visibility = View.GONE
                    binding.signupButton.isEnabled = true

                    // Hiển thị thông báo thành công
                    Toast.makeText(context, status.message, Toast.LENGTH_SHORT).show()

                    // Chuyển đến màn hình đăng nhập sau khi đăng ký thành công
                    (activity as? MainActivity)?.navigateToFragment(LoginFragment(), false)
                }

                is RegistrationStatus.Error -> {
                    // Ẩn loading
                    binding.progressBar.visibility = View.GONE
                    binding.signupButton.isEnabled = true

                    // Hiển thị thông báo lỗi
                    Toast.makeText(context, status.message, Toast.LENGTH_SHORT).show()
                }

                else -> {
                    // Reset UI
                    binding.progressBar.visibility = View.GONE
                    binding.signupButton.isEnabled = true
                }
            }
        }
    }
}
