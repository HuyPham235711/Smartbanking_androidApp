package com.example.afinal.ui.mortgage

import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.afinal.data.mortgage.MortgageAccountEntity
import java.util.*

@Composable
fun MortgageListScreen(
    viewModel: MortgageViewModel,
    onSelect: (Long) -> Unit // nhấn vào 1 khoản vay để xem chi tiết
) {
    val accounts by viewModel.accounts.collectAsState()

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text(
            text = "Khoản vay thế chấp",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        Button(onClick = {
            // Thêm mẫu 1 khoản vay để test
            viewModel.addMortgage(
                accountName = "Vay mua nhà mẫu",
                principal = 500_000_000L,
                annualRate = 10.0,
                termMonths = 24
            )
        }) {
            Text("➕ Thêm khoản vay mẫu")
        }

        Spacer(Modifier.height(16.dp))

        if (accounts.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có khoản vay nào.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(accounts) { acc ->
                    MortgageItem(acc, onSelect)
                }
            }
        }
    }
}

@Composable
private fun MortgageItem(
    account: MortgageAccountEntity,
    onSelect: (Long) -> Unit
) {
    val startDate = remember(account.startDate) {
        val cal = Calendar.getInstance().apply { timeInMillis = account.startDate }
        DateFormat.format("yyyy-MM-dd", cal).toString()
    }

    Card(
        onClick = { onSelect(account.id) },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(account.accountName, fontWeight = FontWeight.Bold)
            Text("Số tiền vay: %,d VND".format(account.principal))
            Text("Lãi suất: %.2f%% / năm".format(account.annualInterestRate))
            Text("Kỳ hạn: ${account.termMonths} tháng")
            Text("Bắt đầu: $startDate")
        }
    }
}
