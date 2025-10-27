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
import com.example.afinal.data.mortgage.MortgageScheduleEntity
import com.example.afinal.viewmodel.mortgage.MortgageViewModel
import java.util.*

@Composable
fun MortgageScheduleScreen(
    viewModel: MortgageViewModel,
    mortgageId: String,
    onBack: () -> Unit
) {
    val schedule by viewModel.schedule.collectAsState()

    LaunchedEffect(mortgageId) {
        viewModel.loadSchedule(mortgageId)
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Row(
            Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                "Lịch trả nợ",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Button(onClick = onBack) {
                Text("⬅ Quay lại")
            }
        }

        Spacer(Modifier.height(12.dp))

        if (schedule.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Chưa có lịch trả nợ.")
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(schedule) { item ->
                    ScheduleItem(item)
                }
            }
        }
    }
}

@Composable
private fun ScheduleItem(item: MortgageScheduleEntity) {
    val dueDate = remember(item.dueDate) {
        val cal = Calendar.getInstance().apply { timeInMillis = item.dueDate }
        DateFormat.format("yyyy-MM-dd", cal).toString()
    }

    Card(
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Kỳ ${item.period}", fontWeight = FontWeight.Bold)
            Text("Ngày đến hạn: $dueDate")
            Text("Gốc: %,d VND".format(item.principalAmount))
            Text("Lãi: %,d VND".format(item.interestAmount))
            Text("Tổng phải trả: %,d VND".format(item.totalAmount))
            Text("Trạng thái: ${item.status}")
        }
    }
}
