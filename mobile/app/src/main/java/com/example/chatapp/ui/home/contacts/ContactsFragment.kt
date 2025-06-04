package com.example.chatapp.ui.home.contacts

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.chatapp.R
import com.example.chatapp.databinding.FragmentContactsBinding
import com.example.chatapp.model.response.UserResponse
import com.example.chatapp.ui.home.chat.ChatFragment

class ContactsFragment : Fragment() {
    private lateinit var binding: FragmentContactsBinding
    private lateinit var viewModel: ContactsViewModel
    private lateinit var contactsAdapter: ContactsAdapter
    private var isSearchVisible = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_contacts, container, false)
        
        // Ensure RetrofitInstance is updated with current authentication tokens
        context?.let {
            com.example.chatapp.RetrofitInstance.updateWithToken(it)
        }

        setupViewModel()
        setupRecyclerView()
        setupSearchFunctionality()
        setupClickListeners()
        observeViewModel()
        
        // Load contacts when fragment is created
        viewModel.loadContacts()
        
        return binding.root
    }

    private fun setupViewModel() {
        viewModel = ViewModelProvider(this)[ContactsViewModel::class.java]
    }

    private fun setupRecyclerView() {
        contactsAdapter = ContactsAdapter { user ->
            navigateToChat(user)
        }
        
        binding.contactsRecyclerView.apply {
            adapter = contactsAdapter
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
        }
    }

    private fun setupSearchFunctionality() {
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                viewModel.searchContacts(s.toString())
            }
        })
    }

    private fun setupClickListeners() {
        // Search button click
        binding.searchButton.setOnClickListener {
            toggleSearchVisibility()
        }
        
        // Clear search button click
        binding.clearSearchButton.setOnClickListener {
            binding.searchEditText.text?.clear()
            hideSearch()
        }
        
        // Add contact button click (from empty state)
        binding.addContactButton.setOnClickListener {
            // You can implement add contact functionality here
            Toast.makeText(requireContext(), "Tính năng thêm liên hệ sẽ được phát triển", Toast.LENGTH_SHORT).show()
        }
    }

    private fun observeViewModel() {
        viewModel.filteredContacts.observe(viewLifecycleOwner) { contacts ->
            contactsAdapter.submitList(contacts)
            updateEmptyState(contacts.isEmpty())
        }
        
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            if (isLoading) {
                binding.contactsRecyclerView.visibility = View.GONE
                binding.emptyStateView.visibility = View.GONE
            }
        }
        
        viewModel.error.observe(viewLifecycleOwner) { errorMsg ->
            if (errorMsg.isNotEmpty()) {
                Toast.makeText(requireContext(), errorMsg, Toast.LENGTH_LONG).show()
                // Log the error message for debugging
                android.util.Log.e("ContactsFragment", "Error loading contacts: $errorMsg")
                // If we have an error and no contacts, show empty state
                if ((viewModel.filteredContacts.value?.isEmpty() == true)) {
                    updateEmptyState(true)
                }
            }
        }
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        if (isEmpty) {
            binding.emptyStateView.visibility = View.VISIBLE
            binding.contactsRecyclerView.visibility = View.GONE
        } else {
            binding.emptyStateView.visibility = View.GONE
            binding.contactsRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun toggleSearchVisibility() {
        if (isSearchVisible) {
            hideSearch()
        } else {
            showSearch()
        }
    }

    private fun showSearch() {
        binding.searchContainer.visibility = View.VISIBLE
        binding.searchEditText.requestFocus()
        isSearchVisible = true
    }

    private fun hideSearch() {
        binding.searchContainer.visibility = View.GONE
        binding.searchEditText.clearFocus()
        isSearchVisible = false
    }

    private fun navigateToChat(user: UserResponse) {
        val chatFragment = ChatFragment()
        val bundle = Bundle().apply {
            putString("userId", user.id.toString())
            putString("userName", user.username)
            putString("userAvatar", user.profilePicUrl)
        }
        chatFragment.arguments = bundle
          this.parentFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, chatFragment)
            .addToBackStack(null)
            .commit()
    }

    override fun onResume() {
        super.onResume()
        // Refresh contacts when returning to this fragment
        viewModel.refreshContacts()
    }
}
