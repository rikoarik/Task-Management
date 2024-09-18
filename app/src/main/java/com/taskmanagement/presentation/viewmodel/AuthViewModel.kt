package com.taskmanagement.presentation.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.taskmanagement.domain.LoginUseCase
import com.taskmanagement.domain.RegisterUseCase

class AuthViewModel(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCase
) : ViewModel() {

    private val _authResult = MutableLiveData<Pair<Boolean, String?>>()
    val authResult: LiveData<Pair<Boolean, String?>> get() = _authResult

    fun register(name: String, email: String, password: String) {
        registerUseCase.execute(email, password) { success, message ->
            if (success) {
                val userId = registerUseCase.getCurrentUserId()
                if (userId != null) {
                    registerUseCase.saveUserDataToDatabase(userId, name, email) { saveSuccess ->
                        _authResult.postValue(Pair(saveSuccess, null))
                    }
                }
            } else {
                _authResult.postValue(Pair(false, message))
            }
        }
    }

    fun login(email: String, password: String) {
        loginUseCase.execute(email, password) { success, message ->
            _authResult.postValue(Pair(success, message))
        }
    }
}
