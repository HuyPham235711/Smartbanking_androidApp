package com.example.afinal.viewmodel.auth

import android.app.Activity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.auth.AuthRepository
import com.example.afinal.data.auth.AuthResult
import com.example.afinal.data.auth.OtpResult
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// State quản lý cả SĐT và mã OTP
data class PhoneAuthUiState(
    val phoneNumber: String = "",
    val otpCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val verificationId: String? = null,
    val isCodeSent: Boolean = false // Trạng thái để chuyển đổi giao diện
)

sealed class PhoneAuthEvent {
    data object VerificationSuccess : PhoneAuthEvent()
}

class PhoneAuthViewModel(private val repository: AuthRepository) : ViewModel() {

    private val _uiState = MutableStateFlow(PhoneAuthUiState())
    val uiState = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<PhoneAuthEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun onPhoneNumberChanged(phone: String) {
        if (phone.all { it.isDigit() }) {
            _uiState.update { it.copy(phoneNumber = phone, error = null) }
        }
    }

    fun onOtpChanged(otp: String) {
        if (otp.length <= 6 && otp.all { it.isDigit() }) {
            _uiState.update { it.copy(otpCode = otp, error = null) }
        }
    }

    fun sendOtp(activity: Activity) {
        viewModelScope.launch {
            val fullPhoneNumber = "+84" + _uiState.value.phoneNumber.removePrefix("0")
            repository.sendOtp(fullPhoneNumber, activity).collect { result ->
                when (result) {
                    is OtpResult.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is OtpResult.CodeSent -> _uiState.update {
                        it.copy(isLoading = false, isCodeSent = true, verificationId = result.verificationId)
                    }
                    is OtpResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                    is OtpResult.VerificationCompleted -> {}
                }
            }
        }
    }

    fun verifyOtp() {
        viewModelScope.launch {
            val id = _uiState.value.verificationId ?: return@launch
            val code = _uiState.value.otpCode

            // SỬ DỤNG HÀM MỚI "signInOrLinkWithOtp" TỪ REPOSITORY
            repository.signInOrLinkWithOtp(id, code).collect { result ->
                when (result) {
                    is AuthResult.Loading -> _uiState.update { it.copy(isLoading = true, error = null) }
                    is AuthResult.Success -> {
                        _uiState.update { it.copy(isLoading = false) }
                        _eventFlow.emit(PhoneAuthEvent.VerificationSuccess)
                    }
                    is AuthResult.Error -> _uiState.update { it.copy(isLoading = false, error = result.message) }
                }
            }
        }
    }
}