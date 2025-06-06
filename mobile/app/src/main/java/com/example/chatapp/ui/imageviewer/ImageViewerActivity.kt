package com.example.chatapp.ui.imageviewer

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import com.example.chatapp.R
import com.example.chatapp.databinding.ActivityImageViewerBinding
import com.squareup.picasso.Callback
import com.squareup.picasso.Picasso

class ImageViewerActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityImageViewerBinding
    
    companion object {
        const val EXTRA_IMAGE_URL = "extra_image_url"
        const val EXTRA_TITLE = "extra_title"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // First, initialize the binding
        binding = DataBindingUtil.setContentView(this, R.layout.activity_image_viewer)
        
        // Only after binding is initialized, call these methods
        setupUI()
        loadImage()
    }
    
    private fun setupUI() {
        // Setup back button
        binding.backButton.setOnClickListener {
            finish()
        }
        
        // Set title if provided
        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Hình ảnh"
        binding.titleText.text = title
    }
    
    private fun loadImage() {
        val imageUrl = intent.getStringExtra(EXTRA_IMAGE_URL)
        
        if (imageUrl.isNullOrEmpty()) {
            // Handle error case
            binding.errorText.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
            binding.errorText.text = "Không thể tải hình ảnh: URL không hợp lệ"
            return
        }
        
        // Show loading state
        binding.progressBar.visibility = View.VISIBLE
        binding.errorText.visibility = View.GONE

        // Load image with Picasso
        Picasso.get()
            .load(imageUrl)
            .error(R.drawable.image_error)
            .into(binding.imageView, object : Callback {
                override fun onSuccess() {
                    binding.progressBar.visibility = View.GONE
                }
                
                override fun onError(e: Exception?) {
                    binding.progressBar.visibility = View.GONE
                    binding.errorText.visibility = View.VISIBLE
                    binding.errorText.text = "Lỗi tải hình ảnh: ${e?.message ?: "Không xác định"}"
                }
            })
    }
}
