package com.example.afinal.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.auth.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ForgotPasswordUiState(
    val email: String = "",
    val isLoading: Boolean = false,
    val message: String? = null, // Thông báo thành công hoặc lỗi
    val isSuccess: Boolean = false
)

class ForgotPasswordViewModel(private val authRepository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState = _uiState.asStateFlow()

    fun onEmailChange(email: String) {
        _uiState.update { it.copy(email = email, message = null) }
    }

    fun sendResetEmail() {
        val email = _uiState.value.email.trim()
        if (email.isEmpty()) {
            _uiState.update { it.copy(message = "Vui lòng nhập email") }
            return
        }

        _uiState.update { it.copy(isLoading = true, message = null) }

        viewModelScope.launch {
            authRepository.sendPasswordResetEmail(email).collect { result ->
                // Do ở bước 1 ta dùng Result chuẩn của Kotlin, ta xử lý như sau:
                if (result.isSuccess) {
                    _uiState.update {
                        it.copy(isLoading = false, isSuccess = true, message = result.getOrNull())
                    }
                } else {
                    _uiState.update {
                        it.copy(isLoading = false, isSuccess = false, message = result.exceptionOrNull()?.message)
                    }
                }
            }
        }
    }

    fun resetState() {
        _uiState.value = ForgotPasswordUiState()
    }
}