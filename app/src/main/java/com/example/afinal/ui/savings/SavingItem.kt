package com.example.afinal.ui.savings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.afinal.data.savings.SavingsAccount
import java.text.NumberFormat
import java.util.Locale

@Composable
fun SavingItem(
    saving: SavingsAccount,
    onClick: (() -> Unit)? = null
) {
    val currency = NumberFormat.getCurrencyInstance(Locale("vi", "VN"))

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant
        )
    ) {
        Column(Modifier.padding(12.dp)) {
            Text(
                text = "Sổ tiết kiệm #${saving.id}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(4.dp))
            Text("Số dư: ${currency.format(saving.balance)}")
            Text("Lãi suất: ${"%.2f".format(saving.interestRate)}%/năm")
            Text("Kỳ hạn: ${saving.termMonths} tháng")
            Text("Mở: ${saving.openDate} • Đáo hạn: ${saving.maturityDate}")
        }
    }
}
