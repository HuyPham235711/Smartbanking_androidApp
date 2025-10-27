package com.example.afinal.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.afinal.data.auth.AuthRepository
import com.example.afinal.data.database.AppDatabase
import com.example.afinal.data.mortgage.MortgageRepository
import com.example.afinal.data.transaction.TransactionRepository
import com.example.afinal.ui.mortgage.MortgageViewModel
import com.example.afinal.ui.transaction.TransactionViewModel
import com.example.afinal.viewmodel.auth.LoginViewModel
import com.example.afinal.viewmodel.auth.PhoneAuthViewModel
import com.example.afinal.viewmodel.auth.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.example.afinal.viewmodel.officer.EkycViewModel

/**
 * Lớp "nhà máy" chịu trách nhiệm tạo ra tất cả các ViewModel trong ứng dụng.
 * Đây là giải pháp thay thế cho Hilt để cung cấp dependency thủ công.
 */
class ViewModelFactory(private val context: Context) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        val db = AppDatabase.getDatabase(context)
        val auth = FirebaseAuth.getInstance()

        return when {
            modelClass.isAssignableFrom(LoginViewModel::class.java) -> {
                val repo = AuthRepository(auth)
                LoginViewModel(repo) as T
            }
            modelClass.isAssignableFrom(RegisterViewModel::class.java) -> {
                val repo = AuthRepository(auth)
                RegisterViewModel(repo) as T
            }
            modelClass.isAssignableFrom(PhoneAuthViewModel::class.java) -> {
                val repo = AuthRepository(auth)
                PhoneAuthViewModel(repo) as T
            }
            modelClass.isAssignableFrom(MortgageViewModel::class.java) -> {
                val repo = MortgageRepository(db.mortgageAccountDao(), db.mortgageScheduleDao())
                MortgageViewModel(repo) as T
            }
            modelClass.isAssignableFrom(TransactionViewModel::class.java) -> {
                val repo = TransactionRepository(db.transactionDao())
                TransactionViewModel(repo) as T
            }
            modelClass.isAssignableFrom(EkycViewModel::class.java) -> {
                EkycViewModel() as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
    }
}