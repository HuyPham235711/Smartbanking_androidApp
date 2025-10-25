package com.example.afinal.ui.home

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.afinal.data.account.AccountRepository
import com.example.afinal.data.database.AppDatabase
import com.example.afinal.data.mortgage.MortgageRepository
import com.example.afinal.data.savings.SavingRepository
import com.example.afinal.data.transaction.TransactionRepository
import com.example.afinal.ui.customer.CheckingDetailEntry
import com.example.afinal.ui.mortgage.MortgageListEntry
import com.example.afinal.ui.savings.SavingEntry
import com.example.afinal.viewmodel.account.CheckingDetailViewModel
import com.example.afinal.viewmodel.mortgage.MortgageViewModel
import com.example.afinal.viewmodel.savings.SavingViewModel
import com.example.afinal.viewmodel.transaction.TransactionViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(navController: NavController) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }

    // Repository + ViewModels
    val accountRepo = remember { AccountRepository(db.accountDao()) }
    val transRepo = remember { TransactionRepository(db.transactionDao()) }
    val savingRepo = remember { SavingRepository(db.savingsAccountDao()) }
    val mortgageRepo = remember { MortgageRepository(db.mortgageAccountDao(), db.mortgageScheduleDao()) }

    val checkingVm = remember { CheckingDetailViewModel(accountRepo, transRepo) }
    val savingVm = remember { SavingViewModel(savingRepo) }
    val mortgageVm = remember { MortgageViewModel(mortgageRepo) }

    var selectedTab by rememberSaveable { mutableStateOf("checking") }
    var selectedAccountId by rememberSaveable { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // 🔹 Lấy account đầu tiên khi app mở
    LaunchedEffect(Unit) {
        val firstAccount = withContext(Dispatchers.IO) {
            db.accountDao().getAllAccounts().firstOrNull()
        }
        selectedAccountId = firstAccount?.id
        if (firstAccount != null) {
            checkingVm.loadAccount(firstAccount.id)
            println("✅ Auto-selected first account: ${firstAccount.fullName} (${firstAccount.id})")
        } else {
            println("⚠️ No accounts found in database.")
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("SmartBanking - Customer") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == "checking",
                    onClick = { selectedTab = "checking" },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Checking") },
                    label = { Text("Checking") }
                )
                NavigationBarItem(
                    selected = selectedTab == "saving",
                    onClick = { selectedTab = "saving" },
                    icon = { Icon(Icons.Default.AttachMoney, contentDescription = "Savings") },
                    label = { Text("Savings") }
                )
                NavigationBarItem(
                    selected = selectedTab == "mortgage",
                    onClick = { selectedTab = "mortgage" },
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Mortgage") },
                    label = { Text("Mortgage") }
                )
            }
        },
        // 🟢 FAB chỉ hiển thị ở tab Savings
        floatingActionButton = {
            if (selectedTab == "saving") {
                FloatingActionButton(onClick = { showAddDialog = true }) {
                    Icon(Icons.Default.AttachMoney, contentDescription = "Tạo sổ tiết kiệm")
                }
            }
        }
    ) { padding ->
        when (selectedTab) {
            "checking" -> CheckingDetailEntry(
                navController = navController,
                viewModel = checkingVm,
                onAccountSelected = { selectedAccountId = it },
                modifier = Modifier.padding(padding)
            )

            "saving" -> SavingEntry(
                navController = navController,
                viewModel = savingVm,
                accountId = selectedAccountId,
                modifier = Modifier.padding(padding),
                showAddDialog = showAddDialog,
                onDialogDismiss = { showAddDialog = false }
            )

            "mortgage" -> {
                LaunchedEffect(selectedAccountId) {
                    selectedAccountId?.let { mortgageVm.loadMortgagesForUser(it) }
                }

                MortgageListEntry(
                    navController = navController,
                    viewModel = mortgageVm,
                    currentAccountId = selectedAccountId,
                    modifier = Modifier.padding(padding)
                )
            }

        }
    }
}
