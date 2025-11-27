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
fun ElectricBillPaymentScreen(
    currentAccountId: String,
    viewModel: BillPaymentViewModel,
    onBack: () -> Unit
) {
    val currentAccount by viewModel.currentAccount.collectAsState()
    val paymentState by viewModel.paymentState.collectAsState()
    val billInfo by viewModel.billInfo.collectAsState()

    var customerCode by remember { mutableStateOf("") }
    var selectedProvider by remember { mutableStateOf("") }
    var expandedDropdown by remember { mutableStateOf(false) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    val electricProviders = listOf(
        "EVN Hồ Chí Minh",
        "EVN Hà Nội",
        "EVN Miền Bắc",
        "EVN Miền Nam",
        "EVN Miền Trung",
        "Công ty CP Điện nước An Giang"
    )

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
                        text = "Thanh toán từ tài khoản",
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

        // Card nhập thông tin hóa đơn
        Card(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "Thông tin hóa đơn tiền điện",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )

                // Dropdown chọn nhà cung cấp
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = !expandedDropdown }
                ) {
                    OutlinedTextField(
                        value = selectedProvider,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Nhà cung cấp") },
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
                        electricProviders.forEach { provider ->
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

                // Nhập mã khách hàng
                OutlinedTextField(
                    value = customerCode,
                    onValueChange = { customerCode = it },
                    label = { Text("Mã khách hàng") },
                    placeholder = { Text("Nhập mã khách hàng EVN") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                // Nút tra cứu hóa đơn
                if (billInfo == null) {
                    Button(
                        onClick = {
                            viewModel.lookupBill(BillType.ELECTRIC, customerCode, selectedProvider)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        enabled = customerCode.isNotEmpty() &&
                                selectedProvider.isNotEmpty() &&
                                paymentState !is PaymentState.Loading
                    ) {
                        Text("Tra cứu hóa đơn")
                    }
                }
            }
        }

        // Hiển thị thông tin hóa đơn
        billInfo?.let { info ->
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "Chi tiết hóa đơn",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(12.dp))

                    InfoRow("Nhà cung cấp", info.serviceProvider)
                    info.address?.let { InfoRow("Địa chỉ", it) }
                    info.period?.let { InfoRow("Kỳ hóa đơn", it) }

                    if (info.amount > 0) {
                        Divider(modifier = Modifier.padding(vertical = 12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                "Tổng tiền:",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                formatCurrency(info.amount),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }

            Button(
                onClick = { showConfirmDialog = true },
                modifier = Modifier.fillMaxWidth(),
                enabled = paymentState !is PaymentState.Processing
            ) {
                Text("Thanh toán")
            }
        }

        // Hiển thị trạng thái
        when (paymentState) {
            is PaymentState.Loading -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    "Đang tra cứu hóa đơn...",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            is PaymentState.Processing -> {
                LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
                Text(
                    "Đang xử lý thanh toán...",
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
                    Text(
                        "✅ Thanh toán thành công!",
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
    }

    // Dialog xác nhận thanh toán
    if (showConfirmDialog && billInfo != null) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("Xác nhận thanh toán") },
            text = {
                Column {
                    Text("Bạn có chắc muốn thanh toán:")
                    Spacer(Modifier.height(8.dp))
                    Text("• Nhà cung cấp: $selectedProvider")
                    Text("• Mã khách hàng: $customerCode")
                    Text("• Số tiền: ${formatCurrency(billInfo!!.amount)}")
                    billInfo!!.period?.let { Text("• Kỳ hóa đơn: $it") }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel.executeBillPayment(
                            accountId = currentAccountId,
                            billType = BillType.ELECTRIC,
                            serviceProvider = selectedProvider,
                            customerCode = customerCode,
                            amount = billInfo!!.amount,
                            billPeriod = billInfo!!.period
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
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}