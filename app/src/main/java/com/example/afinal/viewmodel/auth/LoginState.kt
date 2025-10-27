package com.example.afinal.viewmodel.auth

data class LoginUiState(
    val email: String = "",
    val pass: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class LoginEvent {
    data object NavigateToHome : LoginEvent()
}