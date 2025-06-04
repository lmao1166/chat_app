package com.example.chatapp.ui.home.profile

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentEditProfileBinding
import com.example.chatapp.model.response.UserResponse
import com.example.chatapp.utils.PermissionUtils
import com.squareup.picasso.Picasso

class EditProfileFragment : Fragment() {
    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var viewModel: EditProfileViewModel
    private var selectedImageUri: Uri? = null
    private var currentUser: UserResponse? = null    // Activity result launcher for image selection
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            result.data?.data?.let { uri ->
                selectedImageUri = uri                // Display selected image
                Picasso.get()
                    .load(uri)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .fit()
                    .centerCrop()
                    .into(binding.profileImage)
            }
        }
    }

    // Permission launcher for storage access
    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            launchImagePicker()
        } else {
            Toast.makeText(
                requireContext(),
                "Cần cấp quyền truy cập ảnh để thay đổi ảnh đại diện",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_edit_profile, container, false)
        viewModel = ViewModelProvider(this)[EditProfileViewModel::class.java]

        setupObservers()
        setupClickListeners()
        loadCurrentUserData()

        return binding.root
    }

    private fun setupObservers() {
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.saveButton.isEnabled = !isLoading
            binding.saveButton.text = if (isLoading) "Đang lưu..." else "Lưu thay đổi"
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
            }
        }

        viewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Cập nhật thông tin thành công", Toast.LENGTH_SHORT).show()
                
                // Refresh profile data in parent fragment
                (parentFragmentManager.fragments.find { it is ProfileFragment } as? ProfileFragment)?.let {
                    // You could call refresh method if ProfileViewModel has one
                    // For now, we'll just go back and let onResume handle the refresh
                }
                
                parentFragmentManager.popBackStack()
            }
        }

        viewModel.updatedUser.observe(viewLifecycleOwner) { user ->
            // User data updated successfully
            currentUser = user
        }
    }    private fun loadCurrentUserData() {
        // Get user data from arguments
        arguments?.let { args ->
            val username = args.getString("username", "")
            val profilePicUrl = args.getString("profilePicUrl", "")

            binding.usernameEditText.setText(username)            // Load profile image if available
            if (!profilePicUrl.isNullOrEmpty()) {
                Picasso.get()
                    .load(profilePicUrl)
                    .placeholder(R.drawable.default_avatar)
                    .error(R.drawable.default_avatar)
                    .fit()
                    .centerCrop()
                    .into(binding.profileImage)
            } else {
                binding.profileImage.setImageResource(R.drawable.default_avatar)
            }
        }
    }

    private fun setupClickListeners() {
        binding.backButton.setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        binding.saveButton.setOnClickListener {
            saveUserInformation()
        }

        binding.profileImage.setOnClickListener {
            openImagePicker()
        }

        binding.changeProfileImageText.setOnClickListener {
            openImagePicker()
        }
    }    private fun openImagePicker() {
        if (PermissionUtils.hasImagePickerPermission(requireContext())) {
            launchImagePicker()
        } else {
            // Request permission using modern API
            val permission = PermissionUtils.getRequiredPermission()
            permissionLauncher.launch(permission)
        }
    }

    @SuppressLint("IntentReset")
    private fun launchImagePicker() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        intent.type = "image/*"
        imagePickerLauncher.launch(intent)
    }private fun saveUserInformation() {
        val username = binding.usernameEditText.text.toString().trim()

        if (username.isEmpty()) {
            Toast.makeText(context, "Tên người dùng không được để trống", Toast.LENGTH_SHORT).show()
            return
        }

        viewModel.updateProfile(username, selectedImageUri)
    }
}
