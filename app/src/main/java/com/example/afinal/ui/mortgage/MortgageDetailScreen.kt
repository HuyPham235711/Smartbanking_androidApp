package com.example.afinal.ui.mortgage

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.afinal.viewmodel.mortgage.MortgageDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MortgageDetailScreen(
    viewModel: MortgageDetailViewModel,
    onBack: () -> Unit
) {
    val mortgage = viewModel.mortgage.collectAsState().value
    val schedule = viewModel.schedule.collectAsState().value
    val formatter = remember { SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết khoản vay") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { inner ->
        Column(
            modifier = Modifier
                .padding(inner)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (mortgage != null) {
                // Thông tin khoản vay
                Card(Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Số tiền vay: ${"%,.0f".format(mortgage.principal)} ₫")
                        Text("Lãi suất: ${mortgage.annualInterestRate}%/năm")
                        Text("Kỳ hạn: ${mortgage.termMonths} tháng")
                        Text("Còn nợ: ${"%,.0f".format(mortgage.remainingBalance)} ₫")
                        Text("Trạng thái: ${mortgage.status}")
                    }
                }

                // Lịch thanh toán
                Text("Lịch thanh toán:", style = MaterialTheme.typography.titleMedium)
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxHeight(0.6f)
                ) {
                    items(schedule) { item ->
                        val isPaid = item.status == "PAID"
                        Card(
                            Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = if (isPaid)
                                    MaterialTheme.colorScheme.secondaryContainer
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Kỳ ${item.period}")
                                Text("Ngày đến hạn: ${formatter.format(Date(item.dueDate))}")
                                Text("Gốc: ${"%,.0f".format(item.principalAmount)} ₫")
                                Text("Lãi: ${"%,.0f".format(item.interestAmount)} ₫")
                                Text("Tổng: ${"%,.0f".format(item.totalAmount)} ₫")
                                Text("Trạng thái: ${item.status}")

                            }
                        }
                    }
                }

                // Kiểm tra xem còn kỳ nào PENDING không
                val hasPending = schedule.any { it.status == "PENDING" }
                if (!hasPending) {
                    Text("Tất cả các kỳ đã được thanh toán 🎉")
                }
            } else {
                CircularProgressIndicator()
                Text("Đang tải dữ liệu...")
            }
        }
    }
}
