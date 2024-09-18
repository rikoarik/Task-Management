package com.taskmanagement.presentation.viewmodel

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskmanagement.common.utils.TaskAlarmReceiver
import com.taskmanagement.data.model.Task
import com.taskmanagement.data.repository.TaskRepository
import kotlinx.coroutines.launch

class AddTaskViewModel(private val repository: TaskRepository) : ViewModel() {

    val taskSaved = MutableLiveData<Boolean>()
    val taskError = MutableLiveData<String?>()

    fun saveTask(task: Task) {
        viewModelScope.launch {
            try {
                repository.saveTask(task)
                taskSaved.postValue(true)
            } catch (e: Exception) {
                taskError.postValue(e.message)
            }
        }
    }


}
