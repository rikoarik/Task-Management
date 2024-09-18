package com.taskmanagement.domain

import android.net.Uri
import com.taskmanagement.data.model.FileData
import com.taskmanagement.data.repository.FileRepository

class UploadFileUseCase(private val repository: FileRepository) {
    suspend fun execute(userId: String, fileId: String, fileName: String, fileUri: Uri): String? {
        val fileData = FileData(id = fileId, name = fileName, url = "")
        return repository.uploadFile(userId = userId, fileId = fileId, fileName = fileName, fileUri = fileUri, fileData = fileData)
    }
}
