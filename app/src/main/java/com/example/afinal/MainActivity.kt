package com.example.afinal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.afinal.data.account.AccountRepository
import com.example.afinal.data.bill.BillPaymentRepository
import com.example.afinal.data.database.AppDatabase
import com.example.afinal.data.mortgage.MortgageRepository
import com.example.afinal.data.transaction.TransactionRepository
import com.example.afinal.ui.home.HomeScreen
import com.example.afinal.ui.mortgage.MortgageDetailScreen
import com.example.afinal.ui.officer.CreateAccountScreen
import com.example.afinal.ui.officer.OfficerHomeScreen
import com.example.afinal.ui.officer.mortgage.OfficerMortgageDetailScreen
import com.example.afinal.ui.theme.FinalTheme
import com.example.afinal.ui.transaction.TransactionHistoryScreen
import com.example.afinal.ui.transfer.TransferScreen
import com.example.afinal.ui.bill.BillPaymentScreen
import com.example.afinal.utils.SessionManager
import com.example.afinal.viewmodel.account.CheckingDetailViewModel
import com.example.afinal.viewmodel.account.CheckingDetailViewModelFactory
import com.example.afinal.viewmodel.mortgage.MortgageDetailViewModel
import com.example.afinal.viewmodel.mortgage.MortgageViewModel
import com.example.afinal.viewmodel.officer.OfficerMortgageDetailViewModel
import com.example.afinal.viewmodel.transaction.TransactionViewModel
import com.example.afinal.viewmodel.transfer.TransferViewModel
import com.example.afinal.viewmodel.bill.BillPaymentViewModel
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()

        sessionManager = SessionManager(applicationContext)

        val userRole = intent.getStringExtra("USER_ROLE") ?: "Customer"
        val accountId = intent.getStringExtra("ACCOUNT_ID")

        val startDestination =
            if (userRole.equals("Officer", ignoreCase = true)) "officer_home"
            else "home"

        val db = AppDatabase.getDatabase(applicationContext)

        val accountRepo = AccountRepository(db.accountDao())
        val transRepo = TransactionRepository(db.transactionDao())
        val billPaymentRepo = BillPaymentRepository(db.billPaymentDao())
        val mortgageRepo = MortgageRepository(
            accountDao = db.mortgageAccountDao(),
            scheduleDao = db.mortgageScheduleDao()
        )

        lifecycleScope.launch {
            mortgageRepo.syncMortgagesFromFirestoreOnce()
            mortgageRepo.syncSchedulesFromFirestoreOnce()
        }

        setContent {
            FinalTheme {

                val navController = rememberNavController()

                // ONLY CREATE CHECKING VIEWMODEL HERE (AS YOUR ORIGINAL DESIGN)
                val checkingVm: CheckingDetailViewModel = viewModel(
                    factory = CheckingDetailViewModelFactory(accountRepo, transRepo)
                )

                val transVm = TransactionViewModel(transRepo)
                val mortgageDetailVm = MortgageDetailViewModel(mortgageRepo)
                val officerMortgageDetailVm = OfficerMortgageDetailViewModel(mortgageRepo)

                val billPaymentVm = BillPaymentViewModel(
                    billPaymentRepository = billPaymentRepo,
                    accountRepository = accountRepo,
                    transactionRepository = transRepo
                )

                val transferVm = TransferViewModel(accountRepo, transRepo)

                NavHost(
                    navController = navController,
                    startDestination = startDestination
                ) {
                    composable("home") {
                        HomeScreen(
                            navController = navController,
                            onLogout = ::logout,
                            checkingVm = checkingVm,
                            loggedInAccountId = accountId
                        )
                    }

                    composable("officer_home") {
                        OfficerHomeScreen(
                            navController = navController,
                            onLogout = ::logout
                        )
                    }

                    composable("account_tab") {
                        CreateAccountScreen()
                    }

                    composable("transfer/{accId}") { backStackEntry ->
                        val accId = backStackEntry.arguments?.getString("accId") ?: return@composable
                        TransferScreen(
                            currentAccountId = accId,
                            viewModel = transferVm,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("bill_payment/{accId}") { backStackEntry ->
                        val accId = backStackEntry.arguments?.getString("accId") ?: return@composable
                        BillPaymentScreen(
                            currentAccountId = accId,
                            viewModel = billPaymentVm,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("mortgage_detail/{id}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id") ?: return@composable
                        mortgageDetailVm.loadMortgage(id)
                        MortgageDetailScreen(
                            viewModel = mortgageDetailVm,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable("officer_mortgage_detail/{id}") { backStackEntry ->
                        val id = backStackEntry.arguments?.getString("id") ?: return@composable
                        officerMortgageDetailVm.loadMortgage(id)
                        OfficerMortgageDetailScreen(
                            navController = navController,
                            viewModel = officerMortgageDetailVm
                        )
                    }

                    composable("transaction/{accId}") { backStackEntry ->
                        val accId = backStackEntry.arguments?.getString("accId") ?: return@composable
                        transVm.observeTransactions(accId)
                        TransactionHistoryScreen(
                            viewModel = transVm,
                            accountId = accId,
                            onClose = { navController.popBackStack() }
                        )
                    }
                }
            }
        }
    }

    private fun logout() {
        FirebaseAuth.getInstance().signOut()
        sessionManager.clearSession()
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onStop() {
        super.onStop()
        if (FirebaseAuth.getInstance().currentUser != null) {
            sessionManager.saveLastActiveTimestamp()
        }
    }

    override fun onStart() {
        super.onStart()
        if (sessionManager.isSessionExpired()) {
            FirebaseAuth.getInstance().signOut()
            sessionManager.clearSession()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}
