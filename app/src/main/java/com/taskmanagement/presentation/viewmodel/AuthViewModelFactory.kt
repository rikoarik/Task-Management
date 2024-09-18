package com.taskmanagement.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.taskmanagement.data.repository.AuthRepository
import com.taskmanagement.domain.LoginUseCase
import com.taskmanagement.domain.RegisterUseCase

@Suppress("UNCHECKED_CAST")
class AuthViewModelFactory private constructor(
    private val authRepository: AuthRepository
) : ViewModelProvider.NewInstanceFactory() {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            val loginUseCase = LoginUseCase(authRepository)
            val registerUseCase = RegisterUseCase(authRepository)
            return AuthViewModel(loginUseCase, registerUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }

    companion object {
        private var INSTANCE: AuthViewModelFactory? = null

        fun getInstance(): AuthViewModelFactory {
            if (INSTANCE == null) {
                val authRepository = AuthRepository(FirebaseAuth.getInstance())
                INSTANCE = AuthViewModelFactory(authRepository)
            }
            return INSTANCE!!
        }
    }
}
