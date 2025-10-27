package com.example.afinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.remember
import androidx.lifecycle.lifecycleScope
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.afinal.data.account.AccountRepository
import com.example.afinal.data.database.AppDatabase
import com.example.afinal.data.mortgage.MortgageRepository
import com.example.afinal.data.transaction.TransactionRepository
import com.example.afinal.ui.home.HomeScreen
import com.example.afinal.ui.mortgage.MortgageDetailScreen
import com.example.afinal.ui.officer.OfficerHomeScreen
import com.example.afinal.ui.officer.CreateAccountScreen
import com.example.afinal.ui.officer.mortgage.OfficerMortgageDetailScreen
import com.example.afinal.ui.theme.FinalTheme
import com.example.afinal.ui.transaction.TransactionHistoryScreen
import com.example.afinal.viewmodel.account.CheckingDetailViewModel
import com.example.afinal.viewmodel.mortgage.MortgageDetailViewModel
import com.example.afinal.viewmodel.mortgage.MortgageViewModel
import com.example.afinal.viewmodel.officer.OfficerMortgageDetailViewModel
import com.example.afinal.viewmodel.officer.OfficerMortgageViewModel
import com.example.afinal.viewmodel.transaction.TransactionViewModel
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        val db = AppDatabase.getDatabase(applicationContext)


        // Repository + ViewModels
        val accountRepo = AccountRepository(db.accountDao())
        val transRepo = TransactionRepository(db.transactionDao())
        val mortgageRepo = MortgageRepository(
            accountDao = db.mortgageAccountDao(),
            scheduleDao = db.mortgageScheduleDao()
        )
        lifecycleScope.launch {
            mortgageRepo.syncMortgagesFromFirestoreOnce()   // 🔹 Kéo danh sách mortgage xuống Room
            mortgageRepo.syncSchedulesFromFirestoreOnce()   // 🔹 Sau đó kéo schedules xuống Room
        }

//        lifecycleScope.launch {
//            mortgageRepo.clearAllData()
//        }


        val transVm = TransactionViewModel(repository = transRepo)
        val mortgageListVm = MortgageViewModel(mortgageRepo)
        val mortgageDetailVm = MortgageDetailViewModel(mortgageRepo)
        val officerMortgageDetailVm = OfficerMortgageDetailViewModel(
            repository = MortgageRepository(
                accountDao = db.mortgageAccountDao(),
                scheduleDao = db.mortgageScheduleDao()
            )
        )

        val checkingVm = CheckingDetailViewModel(accountRepo, transRepo)

        setContent {
            FinalTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {
                    // Màn hình chính (Customer tabs)
                    composable("home") {
                        HomeScreen(navController = navController)
                    }

                    // Officer view
                    composable("officer_home") {
                        OfficerHomeScreen(navController = navController)
                    }

                    // Officer – tạo tài khoản
                    composable("account_tab") {
                        CreateAccountScreen()
                    }

                    // Mortgage detail (được mở từ Customer hoặc Officer)
                    composable("mortgage_detail/{id}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id") ?: ""
                        mortgageDetailVm.loadMortgage(id)
                        MortgageDetailScreen(
                            viewModel = mortgageDetailVm,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // Officer xem chi tiết khoản vay
                    composable("officer_mortgage_detail/{id}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id") ?: ""
                        officerMortgageDetailVm.loadMortgage(id)
                        OfficerMortgageDetailScreen(
                            navController = navController,
                            viewModel = officerMortgageDetailVm
                        )
                    }


                    // Transaction history
                    composable("transaction/{accountId}") { backStackEntry ->
                        val accountId = backStackEntry.arguments?.getString("accountId") ?: return@composable
                        transVm.observeTransactions(accountId)
                        TransactionHistoryScreen(viewModel = transVm, accountId = accountId)
                    }
                }
            }
        }
    }
}
