package com.example.afinal.ui.auth

import android.app.Activity
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.afinal.viewmodel.auth.PhoneAuthEvent
import com.example.afinal.viewmodel.auth.PhoneAuthViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun PhoneAuthScreen(
    viewModel: PhoneAuthViewModel,
    onVerificationSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Lắng nghe các sự kiện một lần
    LaunchedEffect(key1 = Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is PhoneAuthEvent.VerificationSuccess -> onVerificationSuccess()
            }
        }
    }

    // Hiển thị lỗi
    LaunchedEffect(uiState.error) {
        uiState.error?.let {
            snackbarHostState.showSnackbar(message = it, duration = SnackbarDuration.Short)
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        // Dựa vào isCodeSent để quyết định hiển thị giao diện nào
        if (!uiState.isCodeSent) {
            EnterPhoneNumberUI(
                modifier = Modifier.padding(padding),
                viewModel = viewModel
            )
        } else {
            EnterOtpUI(
                modifier = Modifier.padding(padding),
                viewModel = viewModel
            )
        }
    }
}

@Composable
private fun EnterPhoneNumberUI(
    modifier: Modifier = Modifier,
    viewModel: PhoneAuthViewModel
) {
    val uiState by viewModel.uiState.collectAsState()
    val activity = LocalContext.current as? Activity

    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Xác thực số điện thoại", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(16.dp))
        Text("Vui lòng nhập số điện thoại của bạn để nhận mã xác thực.", textAlign = TextAlign.Center)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.phoneNumber,
            onValueChange = viewModel::onPhoneNumberChanged,
            label = { Text("Số điện thoại") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.error != null
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = {
                if (activity != null) {
                    viewModel.sendOtp(activity)
                } else {
                    Log.e("PhoneAuth", "Activity is null")
                }
            },
            enabled = !uiState.isLoading && uiState.phoneNumber.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Gửi mã OTP")
            }
        }
    }
}

@Composable
private fun EnterOtpUI(
    modifier: Modifier = Modifier,
    viewModel: PhoneAuthViewModel
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Nhập mã OTP", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(8.dp))
        Text(
            "Mã OTP đã được gửi đến số +84${uiState.phoneNumber.removePrefix("0")}",
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = uiState.otpCode,
            onValueChange = viewModel::onOtpChanged,
            label = { Text("Mã OTP (6 số)") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth(),
            isError = uiState.error != null
        )
        Spacer(Modifier.height(16.dp))

        Button(
            onClick = viewModel::verifyOtp,
            enabled = !uiState.isLoading && uiState.otpCode.length == 6,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (uiState.isLoading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp), color = MaterialTheme.colorScheme.onPrimary)
            } else {
                Text("Xác nhận")
            }
        }
    }
}