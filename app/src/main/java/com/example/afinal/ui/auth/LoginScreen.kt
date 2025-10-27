package com.example.afinal.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.afinal.viewmodel.auth.LoginEvent
import com.example.afinal.viewmodel.auth.LoginViewModel
import kotlinx.coroutines.flow.collectLatest

/**
 * Composable cho màn hình Đăng nhập.
 * - Đây là một "Dumb View", chỉ nhận State từ ViewModel để hiển thị.
 * - Mọi tương tác của người dùng (nhập liệu, click) đều được gửi đến ViewModel.
 * - Các hành động điều hướng được truyền ra ngoài thông qua các callback lambda.
 */
@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    // Lấy trạng thái UI từ ViewModel và tự động cập nhật khi có thay đổi
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Lắng nghe các sự kiện một lần (như điều hướng) từ ViewModel
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is LoginEvent.NavigateToHome -> onLoginSuccess()
            }
        }
    }

    // Lắng nghe và hiển thị lỗi nếu có từ ViewModel
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text("Đăng Nhập", style = MaterialTheme.typography.headlineMedium)

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = viewModel::onEmailChanged,
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.error != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true
                )

                OutlinedTextField(
                    value = uiState.pass,
                    onValueChange = viewModel::onPasswordChanged,
                    label = { Text("Mật khẩu") },
                    visualTransformation = PasswordVisualTransformation(),
                    modifier = Modifier.fillMaxWidth(),
                    isError = uiState.error != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    singleLine = true
                )

                Button(
                    onClick = viewModel::login,
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !uiState.isLoading
                ) {
                    if (uiState.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Login")
                    }
                }

                TextButton(onClick = onNavigateToRegister) {
                    Text("Chưa có tài khoản? Đăng ký ngay")
                }
            }
        }
    }
}