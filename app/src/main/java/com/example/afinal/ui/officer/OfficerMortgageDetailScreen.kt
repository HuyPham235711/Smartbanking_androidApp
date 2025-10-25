package com.example.afinal.ui.officer.mortgage

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.afinal.viewmodel.officer.OfficerMortgageViewModel
import com.example.afinal.viewmodel.officer.OfficerMortgageDetailViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerMortgageDetailScreen(
    navController: NavController,
    viewModel: OfficerMortgageDetailViewModel
) {
    val account by viewModel.account.collectAsState()
    val schedules by viewModel.schedules.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chi tiết khoản vay") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            account?.let { acc ->
                Card(
                    Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Column(Modifier.padding(12.dp)) {
                        Text("Số tiền vay: ${"%,.0f".format(acc.principal)} đ")
                        Text("Lãi suất: ${acc.annualInterestRate}%/năm")
                        Text("Kỳ hạn: ${acc.termMonths} tháng")
                        Text("Còn nợ: ${"%,.0f".format(acc.remainingBalance)} đ")
                        Text("Trạng thái: ${acc.status}")
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text("Lịch thanh toán:", style = MaterialTheme.typography.titleMedium)

            if (schedules.isEmpty()) {
                Text("Tất cả các kỳ đã được thanh toán 🎉", Modifier.padding(top = 8.dp))
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(schedules) { s ->
                        val date = remember(s.dueDate) { Date(s.dueDate) }

                        Card(Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(12.dp)) {
                                Text("Kỳ ${s.period}")
                                Text("Đến hạn: ${SimpleDateFormat("dd/MM/yyyy").format(date)}")
                                Text("Gốc: ${"%,.0f".format(s.principalAmount)} VND")
                                Text("Lãi: ${"%,.0f".format(s.interestAmount)} VND")
                                Text("Tổng: ${"%,.0f".format(s.totalAmount)} VND")
                                Text("Trạng thái: ${s.status}")

                                if (s.status == "PENDING") {
                                    Button(
                                        onClick = { viewModel.markAsPaid(s.id, s.mortgageId) },
                                        modifier = Modifier.padding(top = 8.dp)
                                    ) {
                                        Text("Đánh dấu đã trả")
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

