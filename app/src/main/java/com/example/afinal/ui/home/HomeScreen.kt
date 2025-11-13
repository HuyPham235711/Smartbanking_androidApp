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
// ⭐️ 1. XÓA 2 IMPORT NÀY (NẾU CÓ):
// import kotlinx.coroutines.Dispatchers
// import kotlinx.coroutines.withContext
// ⭐️ 2. THÊM IMPORT NÀY:
import androidx.compose.runtime.collectAsState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    onLogout: () -> Unit // 1. Nhận hàm logout
) {
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


    // ⭐️ 3. THAY THẾ HOÀN TOÀN KHỐI `LaunchedEffect(Unit)` CŨ BẰNG CODE NÀY:

    // 🔹 Quan sát danh sách tài khoản từ Room (được cung cấp bởi repo)
    //    Chúng ta lấy repo từ `checkingVm` đã được khởi tạo
    val allAccounts by checkingVm.accountRepository.observeAllAccounts().collectAsState(initial = emptyList())

    // 🔹 Tự động chọn tài khoản đầu tiên KHI danh sách được nạp (hoặc thay đổi)
    LaunchedEffect(allAccounts) {
        // Chỉ tự động chọn nếu chưa có tài khoản nào được chọn
        if (selectedAccountId == null) {
            // Lấy tài khoản Customer đầu tiên (hoặc bất kỳ tài khoản nào nếu không có Customer)
            val firstAccount = allAccounts.firstOrNull { it.role.equals("Customer", ignoreCase = true) }
                ?: allAccounts.firstOrNull()

            selectedAccountId = firstAccount?.id

            if (firstAccount != null) {
                // Nạp dữ liệu vào ViewModel khi tài khoản đầu tiên xuất hiện
                checkingVm.loadAccount(firstAccount.id)
                println("✅ Auto-selected first account: ${firstAccount.fullName} (${firstAccount.id})")
            } else {
                // Sẽ hiển thị khi app mới mở và chưa kịp sync
                println("⚠️ No accounts found in database (yet)...")
            }
        }
    }
    // (Kết thúc khối thay thế)


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
                onLogout = onLogout, // 2. Truyền hàm logout xuống
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