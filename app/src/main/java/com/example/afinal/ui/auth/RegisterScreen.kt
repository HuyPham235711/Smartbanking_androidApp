package com.example.afinal.ui.auth

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.afinal.viewmodel.auth.RegisterEvent
import com.example.afinal.viewmodel.auth.RegisterViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@Composable
fun RegisterScreen(
    viewModel: RegisterViewModel,
    onRegistrationSuccess: () -> Unit,
    onNavigateBackToLogin: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    val scope = rememberCoroutineScope()


    LaunchedEffect(key1 = Unit) {
        viewModel.resetState()
    }

    LaunchedEffect(key1 = true) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is RegisterEvent.RegistrationSuccess -> {
                    Toast.makeText(context, "Đăng ký thành công!", Toast.LENGTH_SHORT).show()
                    onRegistrationSuccess()
                }
            }
        }
    }

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
        Column(
            modifier = Modifier.fillMaxSize().padding(padding).padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text("Đăng Ký", style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.height(16.dp))

            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChanged,
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.error != null
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.pass,
                onValueChange = viewModel::onPasswordChanged,
                label = { Text("Mật khẩu (ít nhất 6 ký tự)") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth(),
                isError = uiState.error != null
            )
            Spacer(Modifier.height(16.dp))

            Button(
                onClick = {
                    val email = uiState.email.trim()
                    val pass = uiState.pass.trim()

                    if (email.isEmpty() || pass.isEmpty()) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Vui lòng nhập đầy đủ email và mật khẩu")
                        }
                    } else if (pass.length < 6) {
                        scope.launch {
                            snackbarHostState.showSnackbar("Mật khẩu phải có ít nhất 6 ký tự")
                        }
                    }
                    else {
                        viewModel.register()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !uiState.isLoading
            ) {
                if (uiState.isLoading) {
                    CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
                } else {
                    Text("Đăng ký")
                }
            }
            Spacer(Modifier.height(8.dp))

            TextButton(onClick = onNavigateBackToLogin) {
                Text("Đã có tài khoản? Đăng nhập")
            }
        }
    }
}