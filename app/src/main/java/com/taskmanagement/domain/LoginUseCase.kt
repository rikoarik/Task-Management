package com.taskmanagement.domain

import com.taskmanagement.data.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {
    fun execute(email: String, password: String, callback: (Boolean, String?) -> Unit) {
        authRepository.loginUser(email, password, callback)
    }
}
