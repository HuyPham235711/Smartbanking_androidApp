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
import com.example.afinal.viewmodel.mortgage.MortgageViewModel
import java.util.*
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MortgageListScreen(
    viewModel: MortgageViewModel,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val accounts by viewModel.accounts.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Khoản vay thế chấp") })
        }
    ) { innerPadding ->
        Column(
            modifier = modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (accounts.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Chưa có khoản vay nào.")
                }
            } else {
                LazyColumn(
                    contentPadding = PaddingValues(bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(accounts) { acc ->
                        MortgageItem(acc, onSelect)
                    }
                }
            }
        }
    }
}

@Composable
private fun MortgageItem(
    account: MortgageAccountEntity,
    onSelect: (String) -> Unit
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
            Text("Số tiền vay: %,.0f VND".format(account.principal))
            Text("Lãi suất: %.2f%% / năm".format(account.annualInterestRate))
            Text("Kỳ hạn: ${account.termMonths} tháng")
            Text("Bắt đầu: $startDate")
        }
    }
}
