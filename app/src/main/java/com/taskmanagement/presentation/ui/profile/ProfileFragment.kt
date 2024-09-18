package com.taskmanagement.presentation.ui.profile

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.FirebaseAuth
import com.taskmanagement.R
import com.taskmanagement.data.repository.AuthRepository
import com.taskmanagement.databinding.FragmentProfileBinding
import com.taskmanagement.domain.GetCurrentUserDataUseCase
import com.taskmanagement.presentation.ui.auth.AuthActivity
import com.taskmanagement.presentation.viewmodel.ProfileViewModel
import com.taskmanagement.presentation.viewmodel.ProfileViewModelFactory
import kotlinx.coroutines.launch

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences = requireContext().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        val authRepository = AuthRepository(FirebaseAuth.getInstance())
        val viewModelFactory = ProfileViewModelFactory(GetCurrentUserDataUseCase(authRepository))
        profileViewModel = ViewModelProvider(this, viewModelFactory).get(ProfileViewModel::class.java)

        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.userData.collect { result ->
                result.onSuccess { user ->
                    binding.etUserName.setText(user?.name)
                    binding.etUserEmail.setText(user?.email)
                }.onFailure { exception ->
                    // Handle error
                }
            }
        }

        binding.btnLogout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            startActivity(Intent(requireContext(), AuthActivity::class.java))
            val editor = sharedPreferences.edit()
            editor.putBoolean("loggedIn", true)
            editor.clear()
            editor.apply()
            requireActivity().finish()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
