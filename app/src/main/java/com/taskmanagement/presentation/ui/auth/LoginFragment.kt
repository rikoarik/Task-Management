package com.example.taskmanagement.presentation.ui.auth

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.taskmanagement.presentation.ui.main.MainActivity
import com.taskmanagement.presentation.viewmodel.AuthViewModelFactory
import com.taskmanagement.databinding.FragmentLoginBinding
import com.taskmanagement.presentation.viewmodel.AuthViewModel

class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private lateinit var progressDialog: ProgressDialog
    private lateinit var authViewModel: AuthViewModel
    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        val factory = AuthViewModelFactory.getInstance()
        authViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Loading...")
            setCancelable(false)
        }

        binding.buttonLogin.setOnClickListener {
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()

            if (!isValidEmail(email)) {
                binding.textInputLayoutEmail.error = "Email tidak valid"
                return@setOnClickListener
            } else {
                binding.textInputLayoutEmail.error = null
            }

            if (!isValidPassword(password)) {
                binding.textInputLayoutPassword.error = "Password minimal 8 karakter"
                return@setOnClickListener
            } else {
                binding.textInputLayoutPassword.error = null
            }
            progressDialog.show()
            authViewModel.login(email, password)
        }
        sharedPreferences = requireActivity().getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        authViewModel.authResult.observe(viewLifecycleOwner) { result ->
            val (isSuccess, message) = result
            progressDialog.dismiss()
            if (isSuccess) {
                val editor = sharedPreferences.edit()
                editor.putBoolean("loggedIn", true)
                editor.apply()
                startActivity(Intent(requireContext(), MainActivity::class.java))
                requireActivity().finish()
            } else {
                Toast.makeText(requireContext(), "Login gagal: $message", Toast.LENGTH_SHORT).show()
            }
        }
        return binding.root
    }
    private fun isValidEmail(email: String): Boolean {
        return email.isNotEmpty() && Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    private fun isValidPassword(password: String): Boolean {
        return password.length >= 8
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
