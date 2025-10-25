package com.example.afinal.ui.transaction

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
import com.example.afinal.data.transaction.TransactionEntity
import java.util.*
import com.example.afinal.viewmodel.transaction.TransactionViewModel

@Composable
fun TransactionHistoryScreen(
    viewModel: TransactionViewModel,
    accountId: String   // ✅ thêm dòng này
) {
    val transactions by viewModel.transactions.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Lịch sử giao dịch",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )

        Spacer(Modifier.height(12.dp))

        // ✅ Giờ có accountId thật
        Button(onClick = {
            val fakeTx = TransactionEntity(
                accountId = accountId,   // ✅ dùng accountId thật
                amount = 5000.0,
                type = "DEPOSIT",
                description = "Giao dịch test thủ công",
                currency = "VND",
                timestamp = System.currentTimeMillis()
            )
            viewModel.addTransaction(fakeTx)
        }) {
            Text("Thêm giao dịch test")
        }

        Spacer(Modifier.height(12.dp))

        if (transactions.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có giao dịch nào.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                items(transactions) { item ->
                    TransactionItem(item)
                }
            }
        }
    }
}


@Composable
fun TransactionItem(item: TransactionEntity) {
    val formattedDate = remember(item.timestamp) {
        val cal = Calendar.getInstance().apply { timeInMillis = item.timestamp }
        DateFormat.format("yyyy-MM-dd HH:mm", cal).toString()
    }

    Card(
        Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = "${if (item.amount > 0) "+" else ""}${item.amount} ${item.currency}",
                style = MaterialTheme.typography.titleMedium
            )
            Text("Loại: ${item.type}", style = MaterialTheme.typography.bodySmall)
            Text("Mô tả: ${item.description ?: "-"}", style = MaterialTheme.typography.bodySmall)
            Text("Thời gian: $formattedDate", style = MaterialTheme.typography.bodySmall)
        }
    }
}
