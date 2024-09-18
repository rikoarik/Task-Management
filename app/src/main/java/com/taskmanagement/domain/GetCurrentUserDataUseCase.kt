package com.taskmanagement.domain

import com.taskmanagement.data.repository.AuthRepository
import com.taskmanagement.data.model.User
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GetCurrentUserDataUseCase(private val authRepository: AuthRepository) {
    suspend fun execute(): Result<User?> = withContext(Dispatchers.IO) {
        authRepository.getCurrentUserData()
    }
}