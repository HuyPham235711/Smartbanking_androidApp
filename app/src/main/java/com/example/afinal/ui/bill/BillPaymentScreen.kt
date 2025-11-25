package com.example.afinal.ui.bill

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.afinal.data.bill.BillType
import com.example.afinal.viewmodel.bill.BillPaymentViewModel
import java.text.NumberFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BillPaymentScreen(
    currentAccountId: String,
    viewModel: BillPaymentViewModel,
    onBack: () -> Unit
) {
    val currentAccount by viewModel.currentAccount.collectAsState()
    var selectedScreen by remember { mutableStateOf<BillScreenType?>(null) }

    LaunchedEffect(currentAccountId) {
        viewModel.loadAccountAndHistory(currentAccountId)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Thanh toán hóa đơn") },
                navigationIcon = {
                    IconButton(onClick = {
                        if (selectedScreen != null) {
                            selectedScreen = null
                            viewModel.resetState()
                        } else {
                            onBack()
                        }
                    }) {
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
        when (selectedScreen) {
            null -> {
                // Màn hình chọn loại hóa đơn
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // Thông tin tài khoản
                    currentAccount?.let { account ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.primaryContainer
                            )
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
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

                    Text(
                        "Chọn loại hóa đơn",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )

                    BillTypeCard(
                        icon = Icons.Default.FlashOn,
                        title = "Tiền điện",
                        subtitle = "EVN Hồ Chí Minh, EVN Hà Nội, ...",
                        onClick = { selectedScreen = BillScreenType.ELECTRIC }
                    )

                    BillTypeCard(
                        icon = Icons.Default.WaterDrop,
                        title = "Tiền nước",
                        subtitle = "SAWACO, Hawaco, Biwase, ...",
                        onClick = { selectedScreen = BillScreenType.WATER }
                    )

                    BillTypeCard(
                        icon = Icons.Default.Phone,
                        title = "Nạp tiền điện thoại",
                        subtitle = "Viettel, Mobifone, Vinaphone, VietnamMobile",
                        onClick = { selectedScreen = BillScreenType.PHONE }
                    )
                }
            }
            BillScreenType.ELECTRIC -> {
                ElectricBillPaymentScreen(
                    currentAccountId = currentAccountId,
                    viewModel = viewModel,
                    onBack = {
                        selectedScreen = null
                        viewModel.resetState()
                    }
                )
            }
            BillScreenType.WATER -> {
                WaterBillPaymentScreen(
                    currentAccountId = currentAccountId,
                    viewModel = viewModel,
                    onBack = {
                        selectedScreen = null
                        viewModel.resetState()
                    }
                )
            }
            BillScreenType.PHONE -> {
                PhoneTopupScreen(
                    currentAccountId = currentAccountId,
                    viewModel = viewModel,
                    onBack = {
                        selectedScreen = null
                        viewModel.resetState()
                    }
                )
            }
        }
    }
}

@Composable
fun BillTypeCard(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(Modifier.width(16.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null)
        }
    }
}

enum class BillScreenType {
    ELECTRIC, WATER, PHONE
}

fun formatCurrency(amount: Double): String {
    val formatter = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))
    return formatter.format(amount)
}