package com.example.afinal.ui.savings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import com.example.afinal.viewmodel.savings.SavingViewModel

@Composable
fun SavingEntry(
    navController: NavController,
    viewModel: SavingViewModel,
    accountId: String?,
    modifier: Modifier = Modifier,
    showAddDialog: Boolean = false,             // 🆕
    onDialogDismiss: () -> Unit = {}            // 🆕
) {
    if (accountId == null) {
        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("⚠️ Chưa có tài khoản nào để hiển thị Sổ Tiết Kiệm.")
        }
    } else {
        SavingScreen(
            viewModel = viewModel,
            accountId = accountId,
            modifier = modifier,
            showAddDialog = showAddDialog,      // 🆕
            onDialogDismiss = onDialogDismiss   // 🆕
        )
    }
}

