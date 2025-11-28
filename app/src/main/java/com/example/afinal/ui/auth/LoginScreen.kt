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
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit,
    onNavigateToForgotPassword: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetState()
        }
    }

    // Lắng nghe sự kiện (điều hướng)
    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is LoginEvent.NavigateToHome -> onLoginSuccess()
            }
        }
    }

    // Lắng nghe sự kiện (lỗi)
    LaunchedEffect(uiState.error) {
        uiState.error?.let { errorMessage ->
            scope.launch {
                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    duration = SnackbarDuration.Short
                )
            }
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
                    onClick = {
                        val email = uiState.email.trim()
                        val pass = uiState.pass.trim()

                        if (email.isEmpty() && pass.isEmpty()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Vui lòng nhập email và mật khẩu")
                            }
                        } else if (email.isEmpty()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Vui lòng nhập email")
                            }
                        } else if (pass.isEmpty()) {
                            scope.launch {
                                snackbarHostState.showSnackbar("Vui lòng nhập mật khẩu")
                            }
                        } else {
                            viewModel.login()
                        }
                    },
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
                        Text("Đăng nhập")
                    }
                }

                TextButton(onClick = onNavigateToForgotPassword) {
                    Text("Quên mật khẩu?")
                }

                TextButton(onClick = onNavigateToRegister) {
                    Text("Chưa có tài khoản? Đăng ký ngay")
                }
            }
        }
    }
}