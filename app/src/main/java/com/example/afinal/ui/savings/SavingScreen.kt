package com.example.afinal.ui.savings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import com.example.afinal.viewmodel.savings.SavingViewModel

@Composable
fun SavingScreen(
    viewModel: SavingViewModel,
    accountId: String,
    modifier: Modifier = Modifier,
    showAddDialog: Boolean = false,
    onDialogDismiss: () -> Unit = {}
) {
    val savings by viewModel.savings.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    LaunchedEffect(accountId) {
        viewModel.loadSavings(accountId)
    }

    Box( // ✅ dùng Box để xài align
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            Text("Sổ Tiết Kiệm", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(Modifier.height(8.dp))
            Text("Tổng tiền gửi: ${"%,.0f".format(totalBalance)} VND")
            Spacer(Modifier.height(16.dp))

            if (savings.isEmpty()) {
                Text("Chưa có sổ tiết kiệm nào cho tài khoản này.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(savings) { saving ->
                        SavingItem(saving)
                    }
                }
            }
        }

        FloatingActionButton(
            onClick = { showDialog = true },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Add, contentDescription = "Thêm Sổ Tiết Kiệm")
        }


        if (showDialog || showAddDialog) {
            AddSavingDialog(
                onDismiss = {
                    showDialog = false
                    onDialogDismiss()
                },
                onConfirm = { amount, rate, term ->
                    viewModel.addSaving(accountId, amount, rate, term)
                    showDialog = false
                    onDialogDismiss()
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddSavingDialog(
    onDismiss: () -> Unit,
    onConfirm: (Double, Double, Int) -> Unit
) {
    var amount by remember { mutableStateOf(TextFieldValue("")) }
    var rate by remember { mutableStateOf(TextFieldValue("")) }
    var term by remember { mutableStateOf(TextFieldValue("")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val a = amount.text.toDoubleOrNull() ?: 0.0
                val r = rate.text.toDoubleOrNull() ?: 0.0
                val t = term.text.toIntOrNull() ?: 0
                if (a > 0 && r > 0 && t > 0) onConfirm(a, r, t)
            }) { Text("Thêm") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Hủy") } },
        title = { Text("Thêm Sổ Tiết Kiệm") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    label = { Text("Số tiền gửi (VND)") }
                )
                OutlinedTextField(
                    value = rate,
                    onValueChange = { rate = it },
                    label = { Text("Lãi suất (%/năm)") }
                )
                OutlinedTextField(
                    value = term,
                    onValueChange = { term = it },
                    label = { Text("Kỳ hạn (tháng)") }
                )
            }
        }
    )
}
