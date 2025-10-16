package com.example.afinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.afinal.ui.theme.FinalTheme
import com.example.afinal.data.database.AppDatabase
import com.example.afinal.data.transaction.TransactionRepository
import com.example.afinal.ui.transaction.TransactionHistoryScreen
import com.example.afinal.ui.transaction.TransactionViewModel
import com.example.afinal.data.mortgage.MortgageRepository
import com.example.afinal.ui.mortgage.MortgageListScreen
import com.example.afinal.ui.mortgage.MortgageScheduleScreen
import com.example.afinal.ui.mortgage.MortgageViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // === Khởi tạo Database và Repository ===
        val db = AppDatabase.getDatabase(applicationContext)

        // Transaction (Task A)
        val transRepo = TransactionRepository(db.transactionDao())
        val transVm = TransactionViewModel(transRepo).apply { loadTransactions(1L) }

        // Mortgage (Task B)
        val mortgageRepo = MortgageRepository(
            accountDao = db.mortgageAccountDao(),
            scheduleDao = db.mortgageScheduleDao()
        )
        val mortgageVm = MortgageViewModel(mortgageRepo)

        // === Giao diện chính ===
        setContent {
            FinalTheme {
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "mortgage_list" // ⚠️ đổi sang "transaction" nếu test Task A
                ) {
                    // Task B
                    composable("mortgage_list") {
                        MortgageListScreen(
                            viewModel = mortgageVm,
                            onSelect = { id ->
                                navController.navigate("schedule/$id")
                            }
                        )
                    }
                    composable("schedule/{id}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id")?.toLongOrNull() ?: 0L
                        MortgageScheduleScreen(
                            viewModel = mortgageVm,
                            mortgageId = id,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    // Task A (nếu cần xem lại)
                    composable("transaction") {
                        TransactionHistoryScreen(viewModel = transVm)
                    }
                }
            }
        }
    }
}
