package com.example.afinal.ui.customer

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight


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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {

                        // Tên
                        Text(
                            text = "👤  ${account.fullName}",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )

                        Spacer(Modifier.height(4.dp))

                        // Email & phone & role
                        Text("Email: ${account.email}")
                        Text("SĐT: ${account.phone}")
                        Text("Vai trò: ${account.role}")

                        Spacer(Modifier.height(12.dp))

                        // Dòng divider
                        Divider(color = Color.Gray.copy(alpha = 0.3f))

                        Spacer(Modifier.height(12.dp))

                        // Balance Section
                        Text(
                            text = "Số dư tài khoản",
                            style = MaterialTheme.typography.titleMedium,
                            color = Color.Gray
                        )

                        val balanceFormatted = "%,.0f ₫".format(account.balance)

                        Text(
                            text = balanceFormatted,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E7D32) // xanh đô mix ngân hàng
                        )
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



