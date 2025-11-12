package com.example.afinal.viewmodel.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.account.Account // 1. THÊM IMPORT
import com.example.afinal.data.account.AccountRepository // 2. THÊM IMPORT
import com.example.afinal.data.auth.AuthRepository
import com.example.afinal.data.auth.AuthResult
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// 3. SỬA CONSTRUCTOR ĐỂ NHẬN ACCOUNT REPOSITORY
class RegisterViewModel(
    private val repository: AuthRepository,
    private val accountRepository: AccountRepository
) : ViewModel() {

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
                        // 4. ⭐️ SAU KHI ĐĂNG KÝ AUTH THÀNH CÔNG
                        // TẠO NGAY BẢN GHI TRONG DATABASE
                        val newUserAccount = Account(
                            id = result.user.uid, // Dùng UID từ Firebase Auth làm ID chính
                            email = result.user.email ?: currentState.email,
                            password = currentState.pass, // Lưu ý: không nên lưu mật khẩu rõ
                            fullName = "New User", // Tên mặc định
                            phone = "",
                            role = "Customer" // Mặc định là Customer
                        )
                        // Lưu vào Room (việc này sẽ tự động sync lên Firestore)
                        accountRepository.insertAccount(newUserAccount, isRemote = false)

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

    /**
     * ✅ HÀM MỚI: Reset State về trạng thái rỗng
     */
    fun resetState() {
        _uiState.value = RegisterUiState()
    }
}