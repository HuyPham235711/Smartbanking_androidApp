package com.example.afinal.viewmodel.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.afinal.data.account.AccountRepository
// 1. THÊM IMPORT
import com.example.afinal.data.auth.AuthRepository

class AccountViewModelFactory(
    private val repository: AccountRepository,
    private val authRepository: AuthRepository // 2. NHẬN AUTH REPO
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // 3. TRUYỀN AUTH REPO VÀO VIEWMODEL
            return AccountViewModel(repository, authRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}