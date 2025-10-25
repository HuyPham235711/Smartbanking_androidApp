package com.example.afinal.ui.customer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.afinal.data.savings.SavingsAccount
import com.example.afinal.viewmodel.customer.CustomerBalanceViewModel

@Composable
fun CustomerBalanceScreen(
    customerId: String,
    vm: CustomerBalanceViewModel = viewModel()
) {
    LaunchedEffect(customerId) {
        vm.load(customerId)
    }

    val accounts by vm.accounts.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Tổng số dư", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "%,.0f đ".format(vm.totalBalance()),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )
        Spacer(Modifier.height(8.dp))

        Text("Tổng lãi dự kiến", style = MaterialTheme.typography.titleMedium)
        Text(
            text = "%,.0f đ".format(vm.totalProjectedInterest()),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(16.dp))
        Divider()
        Spacer(Modifier.height(8.dp))

        Text("Danh sách sổ tiết kiệm", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        if (accounts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có sổ tiết kiệm nào.")
            }
        } else {
            LazyColumn(Modifier.fillMaxSize()) {
                items(accounts) { account ->
                    SavingsAccountCard(account)
                }
            }
        }
    }
}

@Composable
fun SavingsAccountCard(account: SavingsAccount) {
    Card(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Kỳ hạn: ${account.termMonths} tháng", fontWeight = FontWeight.SemiBold)
                Text("%,.0f đ".format(account.balance), fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(4.dp))
            Text("Lãi suất: ${account.interestRate}%/năm")
            Text("Mở: ${account.openDate}   –   Đáo hạn: ${account.maturityDate}")
        }
    }
}
