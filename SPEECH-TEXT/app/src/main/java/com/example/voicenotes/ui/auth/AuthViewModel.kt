package com.example.voicenotes.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.voicenotes.data.repository.AuthRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Initial)
    val authState: StateFlow<AuthState> = _authState

    fun signIn(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _authState.value = AuthState.Error("Please fill in all fields")
            return
        }

        _authState.value = AuthState.Loading
        
        viewModelScope.launch {
            when (val result = authRepository.signIn(email, password)) {
                is Result.Success -> {
                    _authState.value = AuthState.Success
                }
                is Result.Failure -> {
                    _authState.value = AuthState.Error(
                        result.exception.message ?: "Authentication failed"
                    )
                }
            }
        }
    }

    fun signUp(name: String, email: String, password: String, confirmPassword: String) {
        when {
            name.isBlank() || email.isBlank() || password.isBlank() || confirmPassword.isBlank() -> {
                _authState.value = AuthState.Error("Please fill in all fields")
                return
            }
            password != confirmPassword -> {
                _authState.value = AuthState.Error("Passwords do not match")
                return
            }
            password.length < 6 -> {
                _authState.value = AuthState.Error("Password must be at least 6 characters")
                return
            }
        }

        _authState.value = AuthState.Loading

        viewModelScope.launch {
            when (val result = authRepository.signUp(name, email, password)) {
                is Result.Success -> {
                    _authState.value = AuthState.Success
                }
                is Result.Failure -> {
                    _authState.value = AuthState.Error(
                        result.exception.message ?: "Registration failed"
                    )
                }
            }
        }
    }

    fun resetState() {
        _authState.value = AuthState.Initial
    }
}

sealed class AuthState {
    object Initial : AuthState()
    object Loading : AuthState()
    object Success : AuthState()
    data class Error(val message: String) : AuthState()
}

// Helper sealed class for handling results
sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>() {
        constructor() : this(Unit as T)
    }
    data class Failure(val exception: Exception) : Result<Nothing>() {
        constructor(message: String) : this(Exception(message))
    }
}

// Extension functions for cleaner code
fun <T> Result<T>.onSuccess(block: (T) -> Unit): Result<T> {
    if (this is Result.Success) block(data)
    return this
}

fun <T> Result<T>.onFailure(block: (Exception) -> Unit): Result<T> {
    if (this is Result.Failure) block(exception)
    return this
}
