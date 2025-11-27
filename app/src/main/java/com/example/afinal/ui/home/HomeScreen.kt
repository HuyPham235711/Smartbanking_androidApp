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

@OptIn(ExperimentalMaterial3Api::class)
@Composable

fun HomeScreen(
    navController: NavController,
    onLogout: () -> Unit,
    checkingVm: CheckingDetailViewModel,
    loggedInAccountId: String?
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }

    val accountRepo = remember { AccountRepository(db.accountDao()) }
    val transRepo = remember { TransactionRepository(db.transactionDao()) }
    val savingRepo = remember { SavingRepository(db.savingsAccountDao()) }
    val mortgageRepo = remember {
        MortgageRepository(
            db.mortgageAccountDao(),
            db.mortgageScheduleDao()
        )
    }

    val savingVm = remember { SavingViewModel(savingRepo) }
    val mortgageVm = remember { MortgageViewModel(mortgageRepo) }

    var selectedTab by rememberSaveable { mutableStateOf("checking") }
    var selectedAccountId by rememberSaveable { mutableStateOf<String?>(null) }
    var showAddDialog by remember { mutableStateOf(false) }

    // ⭐ Load đúng account ID đăng nhập
    LaunchedEffect(loggedInAccountId) {
        if (loggedInAccountId != null) {
            selectedAccountId = loggedInAccountId
            checkingVm.loadAccount(loggedInAccountId)
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("SmartBanking - Customer") }) },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == "checking",
                    onClick = { selectedTab = "checking" },
                    icon = { Icon(Icons.Default.Person, null) },
                    label = { Text("Checking") }
                )
                NavigationBarItem(
                    selected = selectedTab == "saving",
                    onClick = { selectedTab = "saving" },
                    icon = { Icon(Icons.Default.AttachMoney, null) },
                    label = { Text("Savings") }
                )
                NavigationBarItem(
                    selected = selectedTab == "mortgage",
                    onClick = { selectedTab = "mortgage" },
                    icon = { Icon(Icons.Default.AccountBalance, null) },
                    label = { Text("Mortgage") }
                )
            }
        }
    ) { padding ->

        when (selectedTab) {

            "checking" -> CheckingDetailEntry(
                navController = navController,
                viewModel = checkingVm,
                loggedInAccountId = loggedInAccountId,
                onLogout = onLogout,
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

