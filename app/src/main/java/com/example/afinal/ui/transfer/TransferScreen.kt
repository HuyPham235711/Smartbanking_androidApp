package com.example.afinal.ui.transfer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import java.text.NumberFormat
import java.util.*
import com.example.afinal.viewmodel.transfer.TransferState
import com.example.afinal.viewmodel.transfer.TransferViewModelFactory

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransferScreen(
    currentAccountId: String,
    viewModel: TransferViewModel = viewModel(),
    onBack: () -> Unit
) {
    val currentAccount by viewModel.currentAccount.collectAsState()
    val availableAccounts by viewModel.availableAccounts.collectAsState()
    val transferState by viewModel.transferState.collectAsState()

    var selectedRecipient by remember { mutableStateOf<Account?>(null) }
    var amount by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showConfirmDialog by remember { mutableStateOf(false) }

    LaunchedEffect(currentAccountId) {
        viewModel.loadAccounts(currentAccountId)
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
                            text = "Số dư: ${formatCurrency(account.balance)}",
                            style = MaterialTheme.typography.bodyLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            // Chọn người nhận
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "Chọn người nhận",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    if (availableAccounts.isEmpty()) {
                        Text(
                            text = "Không có tài khoản khả dụng",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.error
                        )
                    } else {
                        availableAccounts.forEach { recipient ->
                            RecipientItem(
                                account = recipient,
                                isSelected = selectedRecipient?.id == recipient.id,
                                onClick = { selectedRecipient = recipient }
                            )
                            if (recipient != availableAccounts.last()) {
                                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))
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
                isError = amount.isNotEmpty() && amount.toDoubleOrNull() == null
            )

            // Ghi chú
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text("Nội dung chuyển khoản") },
                placeholder = { Text("Nhập ghi chú (không bắt buộc)") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )

            // Hiển thị trạng thái
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
            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = selectedRecipient != null &&
                        amount.isNotEmpty() &&
                        amount.toDoubleOrNull() != null &&
                        transferState !is TransferState.Loading
            ) {
                Text("Xác nhận chuyển tiền")
            }
        }
    }

    // Dialog xác nhận
    if (showConfirmDialog && selectedRecipient != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Xác nhận chuyển tiền") },
            text = {
                Column {
                    Text("Bạn có chắc muốn chuyển:")
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Số tiền: ${formatCurrency(amount.toDouble())}")
                    Text("• Đến: ${selectedRecipient!!.fullName}")
                    Text("• Nội dung: ${description.ifEmpty { "Không có" }}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.executeTransfer(
                            fromAccountId = currentAccountId,
                            toAccountId = selectedRecipient!!.id,
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

@Composable
fun RecipientItem(
    account: Account,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        color = if (isSelected)
            MaterialTheme.colorScheme.primaryContainer
        else
            Color.Transparent,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = account.fullName,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = account.email,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        }
    }
}

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(amount)
}