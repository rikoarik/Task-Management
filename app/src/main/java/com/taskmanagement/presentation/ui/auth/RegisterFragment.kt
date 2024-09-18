package com.taskmanagement.presentation.ui.auth

import android.app.ProgressDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.taskmanagement.presentation.viewmodel.AuthViewModelFactory
import com.taskmanagement.databinding.FragmentRegisterBinding
import com.taskmanagement.presentation.viewmodel.AuthViewModel

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var authViewModel: AuthViewModel
    private lateinit var progressDialog: ProgressDialog
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)

        val factory = AuthViewModelFactory.getInstance()
        authViewModel = ViewModelProvider(this, factory).get(AuthViewModel::class.java)

        progressDialog = ProgressDialog(requireContext()).apply {
            setMessage("Mendaftar...")
            setCancelable(false)
        }

        binding.buttonRegister.setOnClickListener {
            val name = binding.editTextName.text.toString().trim()
            val email = binding.editTextEmail.text.toString().trim()
            val password = binding.editTextPassword.text.toString().trim()
            val confirmPassword = binding.editTextConfirmPassword.text.toString().trim()

            if (validateInput(name, email, password, confirmPassword)) {
                progressDialog.show()
                authViewModel.register(name, email, password)
            }
        }

        authViewModel.authResult.observe(viewLifecycleOwner) { result ->
            val (isSuccess, message) = result
            progressDialog.dismiss()
            if (isSuccess) {
                clearInput()
                Toast.makeText(requireContext(), "Pendaftaran akun berhasil silahkan login", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Pendaftaran akun gagal: $message", Toast.LENGTH_SHORT).show()
            }
        }

        return binding.root
    }

    private fun validateInput(name: String, email: String, password: String, confirmPassword: String): Boolean {
        if (name.isEmpty()) {
            binding.textInputName.error = "Nama tidak boleh kosong"
            return false
        }
        if (email.isEmpty()) {
            binding.textInputEmail.error = "Email tidak boleh kosong"
            return false
        }
        if (password.length < 8) {
            binding.textInputPassword.error = "Password harus minimal 8 karakter"
            return false
        }
        if (password != confirmPassword) {
            binding.textInputConfirmPassword.error = "Konfirmasi password tidak cocok"
            return false
        }

        binding.textInputName.error = null
        binding.textInputEmail.error = null
        binding.textInputPassword.error = null
        binding.textInputConfirmPassword.error = null
        return true
    }

    private fun clearInput() {
        binding.editTextName.text?.clear()
        binding.editTextEmail.text?.clear()
        binding.editTextPassword.text?.clear()
        binding.editTextConfirmPassword.text?.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
