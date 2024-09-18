package com.taskmanagement.presentation.viewmodel


import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.firebase.auth.FirebaseAuth
import com.taskmanagement.data.repository.AuthRepository
import com.taskmanagement.domain.GetCurrentUserDataUseCase
import com.taskmanagement.domain.LoginUseCase
import com.taskmanagement.domain.RegisterUseCase


class ProfileViewModelFactory(
    private val getCurrentUserDataUseCase: GetCurrentUserDataUseCase
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProfileViewModel::class.java)) {
            return ProfileViewModel(getCurrentUserDataUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
