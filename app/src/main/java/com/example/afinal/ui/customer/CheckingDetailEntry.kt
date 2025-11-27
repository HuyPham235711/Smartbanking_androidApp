package com.example.afinal.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.afinal.viewmodel.account.CheckingDetailViewModel

@Composable
fun CheckingDetailEntry(
    navController: NavController,
    viewModel: CheckingDetailViewModel,
    loggedInAccountId: String?,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {

    LaunchedEffect(loggedInAccountId) {
        if (loggedInAccountId != null) {
            viewModel.loadAccount(loggedInAccountId)
        }
    }

    val account = viewModel.account.collectAsState().value

    if (account == null) {
        Column(
            modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator()
            Spacer(Modifier.height(16.dp))
            Text("Đang tải thông tin tài khoản...")

            Spacer(Modifier.height(32.dp))

            // ⭐ NÚT LOGOUT KHẨN CẤP
            Button(onClick = onLogout) {
                Text("Đăng xuất")
            }
        }
    } else {
        CheckingDetailScreen(
            viewModel = viewModel,
            onBack = { navController.popBackStack() },
            onOpenTransactions = {
                navController.navigate("transaction/${account.id}")
            },
            onTransfer = {
                navController.navigate("transfer/${account.id}")
            },
            onBillPayment = {
                navController.navigate("bill_payment/${account.id}")
            },
            onLogout = onLogout
        )
    }
}
