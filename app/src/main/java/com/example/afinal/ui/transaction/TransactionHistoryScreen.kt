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

@Composable
fun TransactionHistoryScreen(viewModel: TransactionViewModel) {
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

        Button(onClick = { viewModel.addSampleTransaction() }) {
            Text("➕ Thêm giao dịch mẫu")
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
