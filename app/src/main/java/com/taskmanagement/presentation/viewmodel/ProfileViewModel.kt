package com.taskmanagement.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.taskmanagement.data.model.User
import com.taskmanagement.domain.GetCurrentUserDataUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val getCurrentUserDataUseCase: GetCurrentUserDataUseCase
) : ViewModel() {

    private val _userData = MutableStateFlow<Result<User?>>(Result.success(null))
    val userData: StateFlow<Result<User?>> = _userData

    init {
        fetchUserData()
    }

    private fun fetchUserData() {
        viewModelScope.launch {
            _userData.value = getCurrentUserDataUseCase.execute()
        }
    }
}
