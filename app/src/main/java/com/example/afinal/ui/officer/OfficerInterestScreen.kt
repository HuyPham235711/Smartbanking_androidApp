package com.example.afinal.ui.officer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.afinal.data.interest.InterestRate
import com.example.afinal.viewmodel.officer.InterestRateViewModel

@Composable
fun OfficerInterestScreen(
    modifier: Modifier = Modifier,
    vm: InterestRateViewModel = viewModel()
) {
    LaunchedEffect(Unit) {
        vm.load()
    }

    val rates by vm.rates.collectAsState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Chỉnh sửa lãi suất", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Divider()
        Spacer(Modifier.height(12.dp))

        LazyColumn(Modifier.fillMaxSize()) {
            items(rates) { rate ->
                InterestRateRow(rate = rate) { term, newRate ->
                    vm.save(term, newRate)
                }
            }

            item {
                Spacer(Modifier.height(8.dp))
                CreateRateRow(onSave = { term, rate ->
                    vm.save(term, rate)
                })
            }
        }
    }
}

@Composable
fun InterestRateRow(rate: InterestRate, onSave: (Int, Double) -> Unit) {
    var rateText by remember(rate.termMonths) { mutableStateOf(rate.rate.toString()) }

    Card(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Kỳ hạn: ${rate.termMonths} tháng")
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = rateText,
                onValueChange = { rateText = it },
                label = { Text("Lãi suất (%/năm)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))
            Button(onClick = {
                rateText.toDoubleOrNull()?.let {
                    onSave(rate.termMonths, it)
                }
            }) {
                Text("Lưu thay đổi")
            }
        }
    }
}

@Composable
fun CreateRateRow(onSave: (Int, Double) -> Unit) {
    var termText by remember { mutableStateOf("") }
    var rateText by remember { mutableStateOf("") }

    Card(
        Modifier.fillMaxWidth().padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column(Modifier.padding(12.dp)) {
            Text("Thêm kỳ hạn mới")
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = termText,
                onValueChange = { termText = it.filter { c -> c.isDigit() } },
                label = { Text("Kỳ hạn (tháng)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Number
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = rateText,
                onValueChange = { rateText = it },
                label = { Text("Lãi suất (%/năm)") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal
                ),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(Modifier.height(8.dp))

            Button(onClick = {
                val term = termText.toIntOrNull()
                val rate = rateText.toDoubleOrNull()
                if (term != null && rate != null) onSave(term, rate)
            }) {
                Text("Lưu kỳ hạn mới")
            }
        }
    }
}
