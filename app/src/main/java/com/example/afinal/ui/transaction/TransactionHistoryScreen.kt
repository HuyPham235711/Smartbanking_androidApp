package com.example.afinal.ui.transaction

import android.text.format.DateFormat
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.afinal.data.transaction.TransactionEntity
import com.example.afinal.viewmodel.transaction.TransactionViewModel
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransactionHistoryScreen(
    viewModel: TransactionViewModel,
    accountId: String,
    onClose: () -> Unit
) {
    val transactions by viewModel.transactions.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Lịch sử giao dịch") },
                navigationIcon = {
                    IconButton(onClick = onClose) {
                        Icon(Icons.Default.Close, contentDescription = "Đóng")
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp)
        ) {

            if (transactions.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Chưa có giao dịch nào.")
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(transactions) { item ->
                        TransactionItem(item)
                    }
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
