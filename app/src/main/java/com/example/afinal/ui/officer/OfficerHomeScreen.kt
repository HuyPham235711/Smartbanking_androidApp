package com.example.afinal.ui.officer

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AttachMoney
import androidx.compose.material.icons.filled.Person
// 1. IMPORT ICON ĐĂNG XUẤT
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavController
import com.example.afinal.data.database.AppDatabase
import com.example.afinal.data.mortgage.MortgageRepository
import com.example.afinal.ui.officer.CreateAccountScreen
import com.example.afinal.ui.officer.mortgage.OfficerMortgageScreen
import com.example.afinal.ui.officer.OfficerInterestScreen
import com.example.afinal.viewmodel.officer.OfficerMortgageViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OfficerHomeScreen(
    navController: NavController,
    onLogout: () -> Unit // 2. NHẬN HÀM LOGOUT
) {
    val context = LocalContext.current
    val db = remember { AppDatabase.getDatabase(context) }
    val mortgageVm = remember {
        OfficerMortgageViewModel(
            MortgageRepository(
                db.mortgageAccountDao(),
                db.mortgageScheduleDao()
            )
        )
    }

    var selectedTab by rememberSaveable { mutableStateOf("account_tab") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Officer Dashboard") },
                // 3. THÊM NÚT ACTION VÀO TOP BAR
                actions = {
                    IconButton(onClick = onLogout) {
                        Icon(
                            Icons.AutoMirrored.Filled.Logout,
                            contentDescription = "Đăng xuất"
                        )
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedTab == "account_tab",
                    onClick = { selectedTab = "account_tab" },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Accounts") },
                    label = { Text("Accounts") }
                )
                NavigationBarItem(
                    selected = selectedTab == "saving_tab",
                    onClick = { selectedTab = "saving_tab" },
                    icon = { Icon(Icons.Default.AttachMoney, contentDescription = "Savings") },
                    label = { Text("Savings") }
                )
                NavigationBarItem(
                    selected = selectedTab == "mortgage_tab",
                    onClick = { selectedTab = "mortgage_tab" },
                    icon = { Icon(Icons.Default.AccountBalance, contentDescription = "Mortgage") },
                    label = { Text("Mortgage") }
                )
            }
        },
        floatingActionButton = {
            if (selectedTab == "mortgage_tab") {
                FloatingActionButton(onClick = { mortgageVm.openDialog() }) {
                    Icon(Icons.Default.Add, contentDescription = "Thêm khoản vay")
                }
            }

        }
    ) { innerPadding ->
        when (selectedTab) {
            "account_tab" -> {
                // 🟢 Truyền padding đúng cú pháp
                Surface(modifier = Modifier.padding(innerPadding)) {
                    CreateAccountScreen()
                }
            }

            "saving_tab" -> {
                Surface(modifier = Modifier.padding(innerPadding)) {
                    OfficerInterestScreen()
                }
            }

            "mortgage_tab" -> {
                OfficerMortgageScreen(
                    navController = navController,
                    viewModel = mortgageVm,
                    modifier = Modifier.padding(innerPadding)
                )
            }
        }
    }
}