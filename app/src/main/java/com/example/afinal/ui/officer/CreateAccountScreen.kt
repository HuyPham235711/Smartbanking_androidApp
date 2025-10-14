package com.example.afinal.ui.officer

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.afinal.data.account.Account
import com.example.afinal.data.account.AccountRepository
import com.example.afinal.data.account.AppDatabase
import com.example.afinal.viewmodel.account.AccountViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Compose UI cho Officer quản lý tài khoản (Create / Update / Delete).
 */
@Composable
fun CreateAccountScreen() {
    val context = LocalContext.current

    // Lấy database & repository
    val db = AppDatabase.getDatabase(context)
    val repo = remember { AccountRepository(db.accountDao()) }

    // ViewModel
    val viewModel = remember { AccountViewModel(repo) }

    // ✅ Gọi loadAccounts() khi màn hình được tạo
    LaunchedEffect(Unit) {
        viewModel.loadAccounts()
    }

    // State cho form nhập
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }

    // ✅ Lưu account đang được chọn để edit
    var selectedAccount by remember { mutableStateOf<Account?>(null) }

    val scope = rememberCoroutineScope()
    val accounts by viewModel.accounts.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top
    ) {
        Text("Officer - Account Management", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        // 🧩 Form nhập thông tin tài khoản
        OutlinedTextField(
            value = username,
            onValueChange = { username = it },
            label = { Text("Username") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = fullName,
            onValueChange = { fullName = it },
            label = { Text("Full Name") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = phone,
            onValueChange = { phone = it },
            label = { Text("Phone") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 🟢 Nút tạo hoặc cập nhật
        Button(
            onClick = {
                val account = Account(
                    id = selectedAccount?.id ?: 0, // Nếu đang edit, giữ lại id
                    username = username.trim(),
                    password = password.trim(),
                    fullName = fullName.trim(),
                    email = email.trim(),
                    phone = phone.trim(),
                    role = "Customer"
                )

                if (account.username.isNotEmpty() && account.password.isNotEmpty()) {
                    scope.launch(Dispatchers.IO) {
                        if (selectedAccount != null) {
                            println("🟡 Updating account id=${account.id} username=${account.username}")
                            viewModel.updateAccount(account)
                        } else {
                            println("🟢 Creating new account: ${account.username}")
                            viewModel.createAccount(account)
                        }
                    }
                }

                // Reset form
                selectedAccount = null
                username = ""
                password = ""
                fullName = ""
                email = ""
                phone = ""
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (selectedAccount != null) "Update Account" else "Create Account")
        }

        Spacer(modifier = Modifier.height(32.dp))

        // 🧾 Danh sách tài khoản
        Text("Danh sách tài khoản:", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        if (accounts.isEmpty()) {
            Text("(Chưa có tài khoản nào)")
        } else {
            accounts.forEach { acc ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "👤 ${acc.username}", style = MaterialTheme.typography.bodyLarge)
                        Text(text = "📧 ${acc.email}", style = MaterialTheme.typography.bodySmall)
                        Text(text = "📱 ${acc.phone}", style = MaterialTheme.typography.bodySmall)
                    }

                    Row {
                        // ✏️ Edit
                        IconButton(onClick = {
                            selectedAccount = acc
                            username = acc.username
                            password = acc.password
                            fullName = acc.fullName
                            email = acc.email
                            phone = acc.phone
                        }) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit")
                        }

                        // 🗑️ Delete (có xác nhận)
                        var showDialog by remember { mutableStateOf(false) }

                        if (showDialog) {
                            AlertDialog(
                                onDismissRequest = { showDialog = false },
                                confirmButton = {
                                    TextButton(onClick = {
                                        scope.launch(Dispatchers.IO) {
                                            viewModel.deleteAccount(acc)
                                        }
                                        showDialog = false
                                    }) {
                                        Text("Xóa")
                                    }
                                },
                                dismissButton = {
                                    TextButton(onClick = { showDialog = false }) {
                                        Text("Hủy")
                                    }
                                },
                                title = { Text("Xác nhận xóa") },
                                text = { Text("Bạn có chắc muốn xóa tài khoản ${acc.username}?") }
                            )
                        }

                        IconButton(onClick = { showDialog = true }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete")
                        }

                    }
                }

                Divider(thickness = 0.5.dp)
            }
        }
    }
}
