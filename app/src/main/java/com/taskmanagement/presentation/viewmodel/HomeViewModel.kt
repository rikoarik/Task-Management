package com.taskmanagement.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskmanagement.data.model.Task
import com.taskmanagement.data.repository.TaskRepository
import kotlinx.coroutines.launch

class HomeViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _pendingTasks = MutableLiveData<List<Task>>()
    val pendingTasks: LiveData<List<Task>> = _pendingTasks

    private val _ongoingTasks = MutableLiveData<List<Task>>()
    val ongoingTasks: LiveData<List<Task>> = _ongoingTasks

    private val _doneTasks = MutableLiveData<List<Task>>()
    val doneTasks: LiveData<List<Task>> = _doneTasks

    fun loadTasks(userId: String) {
        viewModelScope.launch {
            val tasks = repository.getTasks(userId)
            _pendingTasks.value = tasks.filter { it.status == "Pending" }
            _ongoingTasks.value = tasks.filter { it.status == "Ongoing" }
            _doneTasks.value = tasks.filter { it.status == "Done" }
        }
    }
}
