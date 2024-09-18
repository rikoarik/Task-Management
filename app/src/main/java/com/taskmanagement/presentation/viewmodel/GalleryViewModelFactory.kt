package com.taskmanagement.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.taskmanagement.data.repository.FileRepository
import com.taskmanagement.domain.LoadFilesUseCase
import com.taskmanagement.domain.UploadFileUseCase

@Suppress("UNCHECKED_CAST")
class GalleryViewModelFactory(
    private val fileRepository: FileRepository

) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(GalleryViewModel::class.java)) {
            val uploadFileUseCase = UploadFileUseCase(fileRepository)
            val loadFilesUseCase = LoadFilesUseCase(fileRepository)
            return GalleryViewModel(uploadFileUseCase, loadFilesUseCase) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
