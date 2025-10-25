package com.example.afinal.ui.customer

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.afinal.viewmodel.account.CheckingDetailViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextField
import androidx.compose.runtime.*


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckingDetailScreen(
    viewModel: CheckingDetailViewModel,
    onBack: () -> Unit,
    onOpenTransactions: () -> Unit
) {
    val account = viewModel.account.collectAsState().value

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết tài khoản") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (account != null) {
                // --- Card thông tin người dùng ---
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(6.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Text("👤  ${account.fullName}", style = MaterialTheme.typography.titleLarge)
                        Spacer(Modifier.height(4.dp))
                        Text("Email: ${account.email}")
                        Text("SĐT: ${account.phone}")
                        Text("Vai trò: ${account.role}")
                    }
                }

                // --- Các nút hành động ---
                var showDialog by remember { mutableStateOf(false) }
                var isWithdraw by remember { mutableStateOf(false) }
                var amountText by remember { mutableStateOf("") }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            isWithdraw = false
                            showDialog = true
                        }
                    ) {
                        Text("Nạp tài khoản")
                    }

                    Button(
                        modifier = Modifier.weight(1f),
                        onClick = {
                            isWithdraw = true
                            showDialog = true
                        }
                    ) {
                        Text("Rút tiền")
                    }
                }

                if (showDialog && account != null) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = { Text(if (isWithdraw) "Rút tiền" else "Chuyển khoản") },
                        text = {
                            TextField(
                                value = amountText,
                                onValueChange = { amountText = it },
                                label = { Text("Nhập số tiền") }
                            )
                        },
                        confirmButton = {
                            TextButton(onClick = {
                                val amount = amountText.toDoubleOrNull() ?: 0.0
                                val type = if (isWithdraw) "WITHDRAW" else "DEPOSIT"
                                val newBalance = if (isWithdraw)
                                    (account.balance - amount).coerceAtLeast(0.0)
                                else
                                    account.balance + amount

                                viewModel.updateBalance(account.id, newBalance)
                                viewModel.recordTransaction(account.id, type, amount)

                                showDialog = false
                            }) {
                                Text("Xác nhận")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = { showDialog = false }) {
                                Text("Huỷ")
                            }
                        }
                    )
                }


                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenTransactions
                ) {
                    Text("Xem lịch sử giao dịch")
                }
            } else {
                CircularProgressIndicator()
                Text("Đang tải dữ liệu...", Modifier.padding(top = 8.dp))
            }
        }
    }
}



