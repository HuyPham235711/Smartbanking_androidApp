package com.example.afinal.viewmodel.officer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.account.Account
import com.example.afinal.data.account.AccountRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OfficerViewModel(
    private val accountRepository: AccountRepository
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<Account>>(emptyList())
    val accounts: StateFlow<List<Account>> = _accounts

    fun observeAccounts() {
        viewModelScope.launch {
            accountRepository.observeAllAccounts().collect { list ->
                _accounts.value = list
            }
        }
    }

    init {
        // 🔁 Firestore → Room Sync
        viewModelScope.launch {
            accountRepository.listenRemoteChanges().collect { remoteAccounts ->
                remoteAccounts.forEach { acc ->
                    accountRepository.insertAccount(acc, isRemote = true)
                }
                _accounts.value = accountRepository.getAllAccounts()
            }
        }
    }

}
