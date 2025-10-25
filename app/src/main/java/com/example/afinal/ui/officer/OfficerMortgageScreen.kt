package com.example.afinal.ui.officer.mortgage

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.afinal.data.account.Account
import com.example.afinal.data.account.AccountRepository
import com.example.afinal.data.database.AppDatabase
import com.example.afinal.viewmodel.officer.OfficerMortgageViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerMortgageScreen(
    navController: NavController,
    viewModel: OfficerMortgageViewModel,
    modifier: Modifier = Modifier
) {
    val mortgages by viewModel.mortgages.collectAsState()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // ✅ Repo để lấy danh sách account
    val db = remember { AppDatabase.getDatabase(context) }
    val accountRepo = remember { AccountRepository(db.accountDao()) }
    var accounts by remember { mutableStateOf<List<Account>>(emptyList()) }

    // ✅ Lấy danh sách account 1 lần khi mở màn
    LaunchedEffect(viewModel) {
        accounts = accountRepo.getAllAccountsOnce()
        viewModel.loadAllMortgages()
    }

    // 🔹 State cho dialog
    val showDialog by viewModel.showDialog.collectAsState()
    var accountName by remember { mutableStateOf(TextFieldValue("")) }
    var principal by remember { mutableStateOf(TextFieldValue("")) }
    var interest by remember { mutableStateOf(TextFieldValue("")) }
    var term by remember { mutableStateOf(TextFieldValue("")) }
    var selectedAccount by remember { mutableStateOf<Account?>(null) }
    var expanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Danh sách khoản vay") }) }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text("Danh sách khoản vay", style = MaterialTheme.typography.titleLarge)
            Spacer(Modifier.height(8.dp))

            if (mortgages.isEmpty()) {
                Text("Chưa có khoản vay nào.")
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(mortgages) { mortgage ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    navController.navigate("officer_mortgage_detail/${mortgage.id}")
                                },
                            elevation = CardDefaults.cardElevation(4.dp)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text("🏦 ${mortgage.accountName}", style = MaterialTheme.typography.titleMedium)
                                Text("Số tiền vay: ${"%,.0f".format(mortgage.principal)} VND")
                                Text("Lãi suất: ${mortgage.annualInterestRate}% / năm")
                                Text("Thời hạn: ${mortgage.termMonths} tháng")
                                Text("Trạng thái: ${mortgage.status}")
                                Text("👤 Account ID: ${mortgage.ownerAccountId}")
                            }
                        }
                    }
                }
            }
        }

        // 💬 Dialog tạo khoản vay mới (có dropdown chọn tài khoản)
        if (showDialog) {
            AlertDialog(
                onDismissRequest = { viewModel.closeDialog() },
                confirmButton = {
                    TextButton(onClick = {
                        val name = accountName.text.trim()
                        val p = principal.text.toDoubleOrNull() ?: 0.0
                        val i = interest.text.toDoubleOrNull() ?: 0.0
                        val t = term.text.toIntOrNull() ?: 0

                        val acc = selectedAccount
                        if (acc == null) {
                            println("⚠️ Chưa chọn account, không thể tạo mortgage")
                            return@TextButton
                        }
                        if (name.isEmpty() || p <= 0 || t <= 0) {
                            println("⚠️ Dữ liệu không hợp lệ")
                            return@TextButton
                        }

                        scope.launch {
                            try {
                                val id = viewModel.addMortgage(name, p, i, t, acc.id)
                                println("✅ Mortgage created for ${acc.username} (${acc.id}) → mortgageId=$id")

                                viewModel.closeDialog()

                                // 🔥 Navigate sang detail với ID thật
                                navController.navigate("officer_mortgage_detail/$id")

                                // Reset form
                                accountName = TextFieldValue("")
                                principal = TextFieldValue("")
                                interest = TextFieldValue("")
                                term = TextFieldValue("")
                                selectedAccount = null
                                expanded = false
                            } catch (e: Exception) {
                                println("❌ Mortgage create failed: ${e.message}")
                                e.printStackTrace()
                            }
                        }
                    }) {
                        Text("Tạo")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { viewModel.closeDialog() }) {
                        Text("Hủy")
                    }
                },
                title = { Text("Thêm khoản vay mới") },
                text = {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        ExposedDropdownMenuBox(
                            expanded = expanded,
                            onExpandedChange = { expanded = !expanded }
                        ) {
                            OutlinedTextField(
                                value = selectedAccount?.username ?: "",
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Chọn tài khoản") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
                                },
                                modifier = Modifier
                                    .menuAnchor()
                                    .fillMaxWidth()
                            )

                            ExposedDropdownMenu(
                                expanded = expanded,
                                onDismissRequest = { expanded = false }
                            ) {
                                accounts.forEach { acc ->
                                    DropdownMenuItem(
                                        text = { Text(acc.username) },
                                        onClick = {
                                            selectedAccount = acc
                                            expanded = false
                                        }
                                    )
                                }
                            }
                        }

                        OutlinedTextField(
                            value = accountName,
                            onValueChange = { accountName = it },
                            label = { Text("Tên khoản vay") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = principal,
                            onValueChange = { principal = it },
                            label = { Text("Số tiền vay (VND)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = interest,
                            onValueChange = { interest = it },
                            label = { Text("Lãi suất (%/năm)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = term,
                            onValueChange = { term = it },
                            label = { Text("Kỳ hạn (tháng)") },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            )
        }

    }
}
