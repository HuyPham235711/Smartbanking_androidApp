package com.example.afinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.afinal.ui.mortgage.MortgageListScreen
import com.example.afinal.ui.mortgage.MortgageViewModel
import com.example.afinal.ui.theme.FinalTheme
import com.example.afinal.ui.transaction.TransactionHistoryScreen
import com.example.afinal.ui.transaction.TransactionViewModel
import com.example.afinal.viewmodel.ViewModelFactory

/**
 * MainActivity này chỉ dùng để test các màn hình riêng lẻ.
 * Nó không phải là cửa vào chính của ứng dụng.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModelFactory = ViewModelFactory(applicationContext)

        setContent {
            FinalTheme {
                // Bạn có thể test các màn hình khác bằng cách thay đổi code ở đây
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "mortgage_list") {

                    composable("mortgage_list") {
                        val mortgageViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[MortgageViewModel::class.java]
                        MortgageListScreen(
                            viewModel = mortgageViewModel,
                            onSelect = { /* Xử lý điều hướng test */ }
                        )
                    }

                    composable("transaction") {
                        val transactionViewModel = ViewModelProvider(this@MainActivity, viewModelFactory)[TransactionViewModel::class.java]
                        TransactionHistoryScreen(viewModel = transactionViewModel)
                    }
                }
            }
        }
    }
}