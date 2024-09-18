package com.taskmanagement.presentation.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseAuth
import com.taskmanagement.data.model.FileData
import com.taskmanagement.domain.LoadFilesUseCase
import com.taskmanagement.domain.UploadFileUseCase

class GalleryViewModel(
    private val uploadFileUseCase: UploadFileUseCase,
    private val loadFilesUseCase: LoadFilesUseCase
) : ViewModel() {

    private val _uploadStatus = MutableLiveData<String>()
    val uploadStatus: LiveData<String> get() = _uploadStatus

    private val _files = MutableLiveData<List<FileData>?>()
    val files: LiveData<List<FileData>?> get() = _files

    private val _fileUploadResult = MutableLiveData<String?>()
    val fileUploadResult: LiveData<String?> get() = _fileUploadResult

    private val _uploadError = MutableLiveData<String?>()
    val uploadError: LiveData<String?> get() = _uploadError

    private val userId = FirebaseAuth.getInstance().currentUser?.uid

    fun uploadFile(fileId: String, fileName: String, fileUri: Uri) {
        userId?.let { uid ->
            viewModelScope.launch {
                try {
                    val fileUrl = uploadFileUseCase.execute(uid, fileId, fileName, fileUri)
                    _fileUploadResult.postValue(fileUrl)
                } catch (e: Exception) {
                    _uploadError.postValue(e.message)
                }
            }
        } ?: run {
            _uploadError.postValue("User ID is not available.")
        }
    }

    fun loadFiles() {
        userId?.let { uid ->
            viewModelScope.launch {
                try {
                    val fileList = loadFilesUseCase.execute(uid)
                    _files.postValue(fileList)
                } catch (e: Exception) {
                    _uploadStatus.postValue("Failed to load files: ${e.message}")
                }
            }
        } ?: run {
            _uploadStatus.postValue("User ID is not available.")
        }
    }
}
