package com.taskmanagement.domain

import com.taskmanagement.data.model.FileData
import com.taskmanagement.data.repository.FileRepository

class LoadFilesUseCase(private val repository: FileRepository) {
    suspend fun execute(userId: String): List<FileData> {
        return repository.getAllFiles(userId)
    }
}
