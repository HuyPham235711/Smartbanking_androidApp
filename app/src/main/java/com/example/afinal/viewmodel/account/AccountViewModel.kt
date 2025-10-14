package com.example.afinal.viewmodel.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.account.Account
import com.example.afinal.data.account.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel kết nối giữa UI và Repository.
 * Quản lý dữ liệu & lifecycle-safe coroutine.
 */
class AccountViewModel(private val repository: AccountRepository) : ViewModel() {

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts

    fun loadAccounts() {
        viewModelScope.launch {
            _accounts.value = repository.getAllAccounts()
        }
    }

    fun createAccount(account: Account) {
        viewModelScope.launch {
            repository.insertAccount(account)
            _accounts.value = repository.getAllAccounts()
            println("✅ Inserted account: $account")
        }
    }

    fun updateAccount(account: Account) {
        viewModelScope.launch {
            println("🟡 Updating account id=${account.id} username=${account.username}")
            repository.updateAccount(account)
            loadAccounts()
        }
    }

    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
            loadAccounts()
        }
    }


}
