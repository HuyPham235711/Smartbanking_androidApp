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
    onAccountSelected: (String) -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    val account = viewModel.account.collectAsState().value

    LaunchedEffect(account?.id) {
        if (account != null) onAccountSelected(account.id)
    }

    if (account == null) {
        Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator()
                Spacer(Modifier.height(16.dp))
                Text("Đang tải thông tin tài khoản...")
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
