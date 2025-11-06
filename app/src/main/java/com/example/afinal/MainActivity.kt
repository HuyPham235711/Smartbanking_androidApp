package com.example.afinal

import android.content.Intent // 1. Import
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
import com.example.afinal.utils.SessionManager // 2. Import
import com.example.afinal.viewmodel.account.CheckingDetailViewModel
import com.example.afinal.viewmodel.mortgage.MortgageDetailViewModel
import com.example.afinal.viewmodel.mortgage.MortgageViewModel
import com.example.afinal.viewmodel.officer.OfficerMortgageDetailViewModel
import com.example.afinal.viewmodel.officer.OfficerMortgageViewModel
import com.example.afinal.viewmodel.transaction.TransactionViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth // 3. Import
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {

    // 4. Khai báo SessionManager
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        // 5. Khởi tạo SessionManager
        sessionManager = SessionManager(applicationContext)

        val db = AppDatabase.getDatabase(applicationContext)

        // ... (Toàn bộ code repository và viewmodel của bạn giữ nguyên)
        val accountRepo = AccountRepository(db.accountDao())
        val transRepo = TransactionRepository(db.transactionDao())
        val mortgageRepo = MortgageRepository(
            accountDao = db.mortgageAccountDao(),
            scheduleDao = db.mortgageScheduleDao()
        )
        lifecycleScope.launch {
            mortgageRepo.syncMortgagesFromFirestoreOnce()
            mortgageRepo.syncSchedulesFromFirestoreOnce()
        }
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
                // ... (Toàn bộ code setContent giữ nguyên)
                val navController = rememberNavController()

                NavHost(
                    navController = navController,
                    startDestination = "officer_home"
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

    /**
     * 6. Được gọi khi app bị đưa vào background.
     * Lưu lại thời điểm này.
     */
    override fun onStop() {
        super.onStop()
        // Chỉ lưu nếu người dùng đã đăng nhập
        if (FirebaseAuth.getInstance().currentUser != null) {
            sessionManager.saveLastActiveTimestamp()
        }
    }

    /**
     * 7. Được gọi khi app được mở lại.
     * Kiểm tra xem session có hết hạn không.
     */
    override fun onStart() {
        super.onStart()
        if (sessionManager.isSessionExpired()) {
            // Đăng xuất khỏi Firebase
            FirebaseAuth.getInstance().signOut()

            // Xóa timestamp
            sessionManager.clearSession()

            // Quay về màn hình Login
            val intent = Intent(this, LoginActivity::class.java)
            // Xóa hết các activity cũ trên stack
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish() // Đóng MainActivity này lại
        }
    }
}