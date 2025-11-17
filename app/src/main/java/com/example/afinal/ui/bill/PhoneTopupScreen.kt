package com.example.afinal.ui.bill

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.afinal.data.bill.BillType
import com.example.afinal.viewmodel.bill.BillPaymentViewModel
import com.example.afinal.viewmodel.bill.PaymentState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneTopupScreen(
    currentAccountId: String,
    viewModel: BillPaymentViewModel,
    onBack: () -> Unit
) {
    val currentAccount by viewModel.currentAccount.collectAsState()
    val paymentState by viewModel.paymentState.collectAsState()

    var phoneNumber by remember { mutableStateOf("") }
    var amount by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    val phoneProviders = listOf(
        "VinaPhone",
        "MobiPhone",
        "Viettel",
        "VietnamMobile"
    )

    // Quick amount buttons
    val quickAmounts = listOf(10000, 20000, 50000, 100000, 200000, 500000)

    Column(
        modifier = Modifier
            .fillMaxSize()
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
                        text = "Nạp tiền từ tài khoản",
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

        // Card nhập thông tin nạp tiền
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Thông tin nạp tiền",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Dropdown chọn nhà mạng
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedProvider,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Nhà mạng") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor(),
                        colors = ExposedDropdownMenuDefaults.outlinedTextFieldColors()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = { expandedDropdown = false }
                    ) {
                        phoneProviders.forEach { provider ->
                            DropdownMenuItem(
                                text = { Text(provider) },
                                onClick = {
                                    selectedProvider = provider
                                    expandedDropdown = false
                                }
                            )
                        }
                    }
                }

                // Nhập số điện thoại
                OutlinedTextField(
                    value = phoneNumber,
                    onValueChange = {
                        if (it.length <= 11) {
                            phoneNumber = it.filter { char -> char.isDigit() }
                            errorMessage = ""
                        }
                    },
                    label = { Text("Số điện thoại") },
                    placeholder = { Text("Nhập số điện thoại") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                    modifier = Modifier.fillMaxWidth(),
                    isError = errorMessage.isNotEmpty(),
                    supportingText = if (errorMessage.isNotEmpty()) {
                        { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                    } else null
                )

                // Nhập số tiền
                OutlinedTextField(
                    value = amount,
                    onValueChange = {
                        amount = it.filter { char -> char.isDigit() }
                        errorMessage = ""
                    },
                    label = { Text("Số tiền") },
                    placeholder = { Text("Tối thiểu 10.000 VND") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth(),
                    trailingIcon = { Text("VND", modifier = Modifier.padding(end = 8.dp)) },
                    isError = errorMessage.isNotEmpty()
                )

                // Quick amount buttons
                Text(
                    "Chọn nhanh mệnh giá:",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )

                // Grid of quick amount buttons (2 columns)
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    quickAmounts.chunked(2).forEach { rowAmounts ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            rowAmounts.forEach { quickAmount ->
                                OutlinedButton(
                                    onClick = { amount = quickAmount.toString() },
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text(formatCurrency(quickAmount.toDouble()))
                                }
                            }
                            // Add empty space if odd number in row
                            if (rowAmounts.size == 1) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }
        }

        // Nút nạp tiền
        Button(
            onClick = {
                val amountValue = amount.toIntOrNull() ?: 0

                when {
                    selectedProvider.isEmpty() -> errorMessage = "Vui lòng chọn nhà mạng"
                    phoneNumber.length < 10 -> errorMessage = "Số điện thoại không hợp lệ"
                    amountValue < 10000 -> errorMessage = "Số tiền tối thiểu là 10.000 VND"
                    else -> {
                        errorMessage = ""
                        showConfirmDialog = true
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = phoneNumber.isNotEmpty() &&
                    amount.isNotEmpty() &&
                    selectedProvider.isNotEmpty() &&
                    paymentState !is PaymentState.Processing
        ) {
            Text("Nạp tiền")
        }

        // Hiển thị trạng thái
        when (paymentState) {
            is PaymentState.Processing -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    "Đang xử lý nạp tiền...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            is PaymentState.Success -> {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            "✅ Nạp tiền thành công!",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Số điện thoại $phoneNumber đã được nạp ${formatCurrency(amount.toDouble())}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
                LaunchedEffect(Unit) {
                    kotlinx.coroutines.delay(2000)
                    onBack()
                }
            }
            is PaymentState.Error -> {
                val errorState = paymentState as PaymentState.Error
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.errorContainer
                    )
                ) {
                    Text(
                        "❌ ${errorState.message}",
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            else -> {}
        }

        // Thông tin lưu ý
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text(
                    "📌 Lưu ý:",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    "• Số tiền tối thiểu: 10.000 VND",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "• Tiền sẽ được nạp ngay sau khi thanh toán",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    "• Vui lòng kiểm tra kỹ thông tin trước khi xác nhận",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }

    // Dialog xác nhận nạp tiền
    if (showConfirmDialog) {
        val finalAmount = amount.toDoubleOrNull() ?: 0.0

        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Xác nhận nạp tiền") },
            text = {
                Column {
                    Text("Bạn có chắc muốn nạp tiền:")
                    Spacer(Modifier.height(8.dp))
                    Text("• Nhà mạng: $selectedProvider")
                    Text("• Số điện thoại: $phoneNumber")
                    Text("• Số tiền: ${formatCurrency(finalAmount)}")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.executeBillPayment(
                            accountId = currentAccountId,
                            billType = BillType.PHONE_TOPUP,
                            serviceProvider = selectedProvider,
                            customerCode = phoneNumber,
                            amount = finalAmount,
                            billPeriod = null
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