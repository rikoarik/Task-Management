package com.taskmanagement.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskmanagement.data.model.Task
import com.taskmanagement.data.repository.TaskRepository
import kotlinx.coroutines.launch

class TaskViewModel(private val repository: TaskRepository) : ViewModel() {

    private val _tasks = MutableLiveData<List<Task>>()
    val tasks: LiveData<List<Task>> get() = _tasks

    private val _tasksByDate = MutableLiveData<List<Task>>()
    val tasksByDate: LiveData<List<Task>> get() = _tasksByDate

    private val _task = MutableLiveData<Task?>()
    val task: LiveData<Task?> get() = _task

    private val _updateTask = MutableLiveData<Result<Unit>>()
    val updateTask: LiveData<Result<Unit>> = _updateTask

    fun updateTaskStatus(userId: String, taskId: String, newStatus: String) {
        repository.updateStatusTask(userId, taskId, newStatus)
    }

    fun fetchTasks(userId: String) {
        viewModelScope.launch {
            try {
                val taskList = repository.getTasks(userId)
                _tasks.postValue(taskList)
            } catch (e: Exception) {
                _tasks.postValue(emptyList())
            }
        }
    }

    fun fetchTasksByDate(userId: String, selectedDate: String) {
        viewModelScope.launch {
            try {
                val taskList = repository.getTasksByDate(userId, selectedDate)
                _tasksByDate.postValue(taskList)
            } catch (e: Exception) {
                _tasksByDate.postValue(emptyList())
            }
        }
    }

    fun fetchTaskById(userId: String, idTask: String) {
        viewModelScope.launch {
            try {
                val fetchedTask = repository.getTaskById(userId, idTask)
                _task.postValue(fetchedTask)
            } catch (e: Exception) {
                _task.postValue(null)
            }
        }
    }

    fun updateTask(userId: String, idTask: String, updatedTask: Task) {
        viewModelScope.launch {
            try {
                val result = repository.updateTaskById(userId, idTask, updatedTask)
                _updateTask.value = result
            } catch (e: Exception) {
                _updateTask.value = Result.failure(e)
            }
        }
    }


}