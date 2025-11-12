package com.example.afinal.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.afinal.viewmodel.account.CheckingDetailViewModel

@Composable
fun CheckingDetailEntry(
    navController: NavController,
    viewModel: CheckingDetailViewModel,
    onAccountSelected: (String) -> Unit,
    onLogout: () -> Unit, // 1. Nhận hàm logout
    modifier: Modifier = Modifier
) {
    val account = viewModel.account.collectAsState().value

    LaunchedEffect(account?.id) {
        if (account != null) onAccountSelected(account.id)
    }

    if (account == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("Chưa có tài khoản nào được chọn.")
        }
    } else {
        CheckingDetailScreen(
            viewModel = viewModel,
            onBack = { navController.popBackStack() },
            onOpenTransactions = { navController.navigate("transaction/${account.id}") },
            onLogout = onLogout // 2. Truyền hàm logout xuống
        )
    }
}