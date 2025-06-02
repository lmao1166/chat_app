package com.example.chatapp.ui.auth.login

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
import com.example.chatapp.databinding.FragmentLoginBinding
import com.example.chatapp.ui.auth.AuthViewModel
import com.example.chatapp.ui.home.HomeFragment
import com.example.chatapp.utils.TokenManager

class LoginFragment : Fragment() {
    private lateinit var binding: FragmentLoginBinding
    private lateinit var viewModel: LoginViewModel
    private lateinit var authViewModel: AuthViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_login, container, false)        // Khởi tạo ViewModel
        viewModel = ViewModelProvider(this)[LoginViewModel::class.java]
        authViewModel = ViewModelProvider(requireActivity())[AuthViewModel::class.java]

        // Thiết lập observer và listener
        setupObservers()
        setupListeners()

        return binding.root
    }

    private fun setupObservers() {
        viewModel.loginResult.observe(viewLifecycleOwner) { result ->
            when (result) {
                is LoginViewModel.LoginResult.Success -> {
                    // Ẩn loading khi đăng nhập thành công
                    binding.progressBar.visibility = View.GONE

                    // Lưu token vào SharedPreferences thông qua TokenManager
                    result.data.data?.let { authResponse ->
                        val accessToken = authResponse.accessToken
                        val refreshToken = authResponse.refreshToken
                        val tokenManager = TokenManager.getInstance(requireContext())
                        tokenManager.saveTokens(accessToken, refreshToken)                        // Hiển thị thông báo đăng nhập thành công
                        Toast.makeText(context, "Đăng nhập thành công", Toast.LENGTH_SHORT).show()

                        // Cập nhật trạng thái đăng nhập toàn cục
                        authViewModel.onLoginSuccess()

                        // Chuyển đến màn hình chính
                        (activity as? MainActivity)?.navigateToFragment(HomeFragment(), false)
                    } ?: run {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(context, "Lỗi: Không nhận được dữ liệu token", Toast.LENGTH_SHORT).show()
                    }
                }

                is LoginViewModel.LoginResult.Error -> {
                    // Ẩn loading khi có lỗi
                    binding.progressBar.visibility = View.GONE
                    
                    // Hiển thị thông báo lỗi
                    Toast.makeText(context, result.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupListeners() {
        binding.btnLogIn.setOnClickListener {
            val email = binding.edtEmail.text.toString().trim()
            val password = binding.edtPassword.text.toString().trim()

            // Validate input
            if (email.isEmpty()) {
                binding.edtEmail.error = "Vui lòng nhập email"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.edtPassword.error = "Vui lòng nhập mật khẩu"
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.edtEmail.error = "Email không hợp lệ"
                return@setOnClickListener
            }

            // Hiển thị loading
            binding.progressBar.visibility = View.VISIBLE

            // Gọi ViewModel để thực hiện login
            viewModel.login(email, password)
        }
    }
}

