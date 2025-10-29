package com.example.afinal.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.auth.AuthRepository
import com.example.afinal.data.auth.AuthResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class RegisterViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<RegisterEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onEmailChanged(email: String) {
        _uiState.update { it.copy(email = email, error = null) }
    }

    fun onPasswordChanged(password: String) {
        _uiState.update { it.copy(pass = password, error = null) }
    }

    fun register() {
        viewModelScope.launch {
            val currentState = _uiState.value
            repository.registerUser(currentState.email, currentState.pass).collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        _uiState.update { it.copy(isLoading = true, error = null) }
                    }
                    is AuthResult.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _eventFlow.emit(RegisterEvent.RegistrationSuccess)
                    }
                    is AuthResult.Error -> {
                        _uiState.update { it.copy(isLoading = false, error = result.message) }
                    }
                }
            }
        }
    }
}