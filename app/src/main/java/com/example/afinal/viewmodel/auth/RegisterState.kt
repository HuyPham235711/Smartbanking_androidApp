package com.example.afinal.viewmodel.auth

data class RegisterUiState(
    val email: String = "",
    val pass: String = "",
    val isLoading: Boolean = false,
    val error: String? = null
)

sealed class RegisterEvent {
    data object RegistrationSuccess : RegisterEvent()
}