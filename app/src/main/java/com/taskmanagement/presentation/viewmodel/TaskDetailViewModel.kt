package com.taskmanagement.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskmanagement.data.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskDetailViewModel(private val repository: TaskRepository) : ViewModel() {

    fun updateTaskStatus(userId: String, taskId: String, newStatus: String) {
        repository.updateStatusTask(userId, taskId, newStatus)
    }
}
