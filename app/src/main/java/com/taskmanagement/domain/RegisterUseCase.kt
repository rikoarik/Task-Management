package com.taskmanagement.domain

import com.taskmanagement.data.repository.AuthRepository

class RegisterUseCase(private val authRepository: AuthRepository) {
    fun execute(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        authRepository.registerUser(email, password, callback)
    }

    fun getCurrentUserId(): String? {
        return authRepository.getCurrentUser()?.uid
    }

    fun saveUserDataToDatabase(userId: String, name: String, email: String, callback: (Boolean) -> Unit) {
        authRepository.saveUserDataToDatabase(userId, name, email, callback)
    }
}

