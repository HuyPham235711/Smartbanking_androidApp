package com.example.afinal.ui.customer

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.afinal.viewmodel.account.CheckingDetailViewModel
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextField
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.Alignment

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CheckingDetailScreen(
    viewModel: CheckingDetailViewModel,
    onBack: () -> Unit,
    onOpenTransactions: () -> Unit,
    onTransfer: () -> Unit,
    onBillPayment: () -> Unit,
    onLogout: () -> Unit
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
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
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
                // ============================================
                // 📊 Card thông tin người dùng & Số dư
                // ============================================
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    elevation = CardDefaults.cardElevation(6.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "👤  ${account.fullName}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = account.email,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                                )
                            }
                        }

                        Spacer(Modifier.height(16.dp))
                        HorizontalDivider(color = Color.Gray.copy(alpha = 0.3f))
                        Spacer(Modifier.height(16.dp))

                        // Số dư
                        Text(
                            text = "Số dư hiện tại",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(Modifier.height(4.dp))
                        val balanceFormatted = "%,.0f ₫".format(account.balance)
                        Text(
                            text = balanceFormatted,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF2E7D32)
                        )
                    }
                }

                // ============================================
                // 🎯 Các chức năng chính (Grid 2x2)
                // ============================================
                Text(
                    "Chức năng",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Nút Chuyển tiền
                    ElevatedCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        onClick = onTransfer,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.secondaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.SwapHoriz,
                                contentDescription = "Chuyển tiền",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Chuyển tiền",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Nút Thanh toán hóa đơn
                    ElevatedCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        onClick = onBillPayment,
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = MaterialTheme.colorScheme.tertiaryContainer
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Receipt,
                                contentDescription = "Thanh toán hóa đơn",
                                modifier = Modifier.size(32.dp),
                                tint = MaterialTheme.colorScheme.tertiary
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Hóa đơn",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // ============================================
                // 💰 Nạp/Rút tiền (Row thứ 2)
                // ============================================
                var showDialog by remember { mutableStateOf(false) }
                var isWithdraw by remember { mutableStateOf(false) }
                var amountText by remember { mutableStateOf("") }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Nút Nạp tiền
                    ElevatedCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        onClick = {
                            isWithdraw = false
                            showDialog = true
                        },
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = Color(0xFFE8F5E9) // Light green
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Add,
                                contentDescription = "Nạp tiền",
                                modifier = Modifier.size(32.dp),
                                tint = Color(0xFF2E7D32)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Nạp tiền",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Nút Rút tiền
                    ElevatedCard(
                        modifier = Modifier
                            .weight(1f)
                            .height(100.dp),
                        onClick = {
                            isWithdraw = true
                            showDialog = true
                        },
                        colors = CardDefaults.elevatedCardColors(
                            containerColor = Color(0xFFFFF3E0) // Light orange
                        )
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(12.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                Icons.Default.Remove,
                                contentDescription = "Rút tiền",
                                modifier = Modifier.size(32.dp),
                                tint = Color(0xFFE65100)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Rút tiền",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                // Dialog Nạp/Rút tiền
                if (showDialog && account != null) {
                    AlertDialog(
                        onDismissRequest = {
                            showDialog = false
                            amountText = ""
                        },
                        icon = {
                            Icon(
                                if (isWithdraw) Icons.Default.Remove else Icons.Default.Add,
                                contentDescription = null,
                                tint = if (isWithdraw) Color(0xFFE65100) else Color(0xFF2E7D32)
                            )
                        },
                        title = {
                            Text(
                                if (isWithdraw) "Rút tiền" else "Nạp tiền",
                                fontWeight = FontWeight.Bold
                            )
                        },
                        text = {
                            Column {
                                if (isWithdraw) {
                                    Text(
                                        "Số dư hiện tại: ${"%,.0f ₫".format(account.balance)}",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = Color.Gray
                                    )
                                    Spacer(Modifier.height(8.dp))
                                }
                                OutlinedTextField(
                                    value = amountText,
                                    onValueChange = { amountText = it.filter { char -> char.isDigit() } },
                                    label = { Text("Số tiền") },
                                    placeholder = { Text("Nhập số tiền") },
                                    suffix = { Text("₫") },
                                    singleLine = true,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }
                        },
                        confirmButton = {
                            Button(
                                onClick = {
                                    val amount = amountText.toDoubleOrNull() ?: 0.0
                                    if (amount > 0) {
                                        val type = if (isWithdraw) "WITHDRAW" else "DEPOSIT"
                                        val newBalance = if (isWithdraw)
                                            (account.balance - amount).coerceAtLeast(0.0)
                                        else
                                            account.balance + amount

                                        viewModel.updateBalance(account.id, newBalance)
                                        viewModel.recordTransaction(account.id, type, amount)

                                        showDialog = false
                                        amountText = ""
                                    }
                                },
                                enabled = amountText.isNotEmpty() && amountText.toDoubleOrNull() != null
                            ) {
                                Text("Xác nhận")
                            }
                        },
                        dismissButton = {
                            TextButton(onClick = {
                                showDialog = false
                                amountText = ""
                            }) {
                                Text("Hủy")
                            }
                        }
                    )
                }

                // ============================================
                // 📜 Lịch sử giao dịch
                // ============================================
                OutlinedButton(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = onOpenTransactions
                ) {
                    Icon(
                        Icons.Default.History,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Xem lịch sử giao dịch")
                }

                // ============================================
                // 🚪 Đăng xuất
                // ============================================
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = onLogout,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(
                        Icons.Default.Logout,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Đăng xuất")
                }

            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator()
                        Spacer(Modifier.height(16.dp))
                        Text("Đang tải dữ liệu...", style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}
