package com.example.afinal.ui.transfer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.afinal.data.account.Account
import com.example.afinal.viewmodel.transfer.TransferViewModel
import com.example.afinal.viewmodel.transfer.TransferState
import com.example.afinal.viewmodel.transfer.RecipientSearchState
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    currentAccountId: String,
    viewModel: TransferViewModel = viewModel(),
    onBack: () -> Unit
) {
    val currentAccount by viewModel.currentAccount.collectAsState()
    val recipientSearchState by viewModel.recipientSearchState.collectAsState()
    val transferState by viewModel.transferState.collectAsState()

    var recipientId by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentAccountId) {
        viewModel.loadCurrentAccount(currentAccountId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chuyển tiền") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, "Quay lại")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Thông tin tài khoản nguồn
            currentAccount?.let { account ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "Từ tài khoản",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f)
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = account.fullName,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "ID: ${account.id}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.6f)
                        )
                        Text(
                            text = "Số dư: ${formatCurrency(account.balance)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Nhập ID người nhận
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Thông tin người nhận",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    // Input field với nút search
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = recipientId,
                            onValueChange = {
                                recipientId = it
                                // Reset search state khi user đổi input
                                if (it.isEmpty()) {
                                    viewModel.resetRecipientSearch()
                                }
                            },
                            label = { Text("ID tài khoản người nhận") },
                            placeholder = { Text("Nhập ID tài khoản") },
                            modifier = Modifier.weight(1f),
                            singleLine = true,
                            isError = recipientSearchState is RecipientSearchState.NotFound
                        )

                        IconButton(
                            onClick = {
                                if (recipientId.isNotEmpty()) {
                                    viewModel.searchRecipient(recipientId, currentAccountId)
                                }
                            },
                            enabled = recipientId.isNotEmpty() && recipientSearchState !is RecipientSearchState.Loading
                        ) {
                            Icon(
                                Icons.Default.Search,
                                contentDescription = "Tìm kiếm",
                                tint = if (recipientId.isNotEmpty())
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    // Hiển thị kết quả tìm kiếm
                    when (val state = recipientSearchState) {
                        is RecipientSearchState.Idle -> {
                            Text(
                                text = "💡 Nhập ID tài khoản và nhấn tìm kiếm",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                            )
                        }
                        is RecipientSearchState.Loading -> {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CircularProgressIndicator(modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Đang tìm kiếm...",
                                    style = MaterialTheme.typography.bodySmall
                                )
                            }
                        }
                        is RecipientSearchState.Found -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                                )
                            ) {
                                Column(modifier = Modifier.padding(12.dp)) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = state.account.fullName,
                                                style = MaterialTheme.typography.titleMedium,
                                                fontWeight = FontWeight.Bold
                                            )
                                            Text(
                                                text = state.account.email,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                            )
                                            Text(
                                                text = "SĐT: ${state.account.phone}",
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.Search,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.primary,
                                            modifier = Modifier.size(32.dp)
                                        )
                                    }
                                }
                            }
                        }
                        is RecipientSearchState.NotFound -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = "❌ Không tìm thấy tài khoản với ID này",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        is RecipientSearchState.SameAccount -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = "⚠️ Không thể chuyển tiền cho chính mình",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        is RecipientSearchState.Error -> {
                            Card(
                                modifier = Modifier.fillMaxWidth(),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.errorContainer
                                )
                            ) {
                                Text(
                                    text = "❌ ${state.message}",
                                    modifier = Modifier.padding(12.dp),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    }
                }
            }

            // Nhập số tiền
            OutlinedTextField(
                value = amount,
                onValueChange = { amount = it.filter { char -> char.isDigit() } },
                label = { Text("Số tiền") },
                placeholder = { Text("Nhập số tiền cần chuyển") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = { Text("VND", modifier = Modifier.padding(end = 8.dp)) },
                isError = amount.isNotEmpty() && amount.toDoubleOrNull() == null,
                enabled = recipientSearchState is RecipientSearchState.Found
            )

            // Ghi chú
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Nội dung chuyển khoản") },
                placeholder = { Text("Nhập ghi chú (không bắt buộc)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2,
                enabled = recipientSearchState is RecipientSearchState.Found
            )

            // Hiển thị trạng thái transfer
            when (transferState) {
                is TransferState.Loading -> {
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                }
                is TransferState.Success -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = "✅ Chuyển tiền thành công!",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    LaunchedEffect(Unit) {
                        kotlinx.coroutines.delay(2000)
                        onBack()
                    }
                }
                is TransferState.Error -> {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer
                        )
                    ) {
                        Text(
                            text = "❌ ${(transferState as TransferState.Error).message}",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
                else -> {}
            }

            // Nút xác nhận
            val recipientAccount = (recipientSearchState as? RecipientSearchState.Found)?.account
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = recipientAccount != null &&
                        amount.isNotEmpty() &&
                        amount.toDoubleOrNull() != null &&
                        transferState !is TransferState.Loading
            ) {
                Text("Xác nhận chuyển tiền")
            }
        }
    }

    // Dialog xác nhận
    val recipientAccount = (recipientSearchState as? RecipientSearchState.Found)?.account
    if (showConfirmDialog && recipientAccount != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Xác nhận chuyển tiền") },
            text = {
                Column {
                    Text("Bạn có chắc muốn chuyển:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Số tiền: ${formatCurrency(amount.toDouble())}")
                    Text("• Đến: ${recipientAccount.fullName}")
                    Text("• Email: ${recipientAccount.email}")
                    Text("• ID: ${recipientAccount.id}")
                    Text("• Nội dung: ${description.ifEmpty { "Không có" }}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.executeTransfer(
                            fromAccountId = currentAccountId,
                            toAccountId = recipientAccount.id,
                            amount = amount.toDouble(),
                            description = description.ifEmpty { "Chuyển khoản" }
                        )
                        showConfirmDialog = false
                    }
                ) {
                    Text("Xác nhận")
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("Hủy")
                }
            }
        )
    }
}

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(amount)
}