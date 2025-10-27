package com.example.afinal.viewmodel.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.account.Account
import com.example.afinal.data.account.AccountRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AccountViewModel(private val repository: AccountRepository) : ViewModel() {

    // ✅ Dòng dữ liệu realtime từ Room (Flow → StateFlow)
    val accounts: StateFlow<List<Account>> = repository.observeAllAccounts()
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    // ✅ Tạo tài khoản (local + push Firestore)
    fun createAccount(account: Account) {
        viewModelScope.launch {
            repository.insertAccount(account)
            println("🟢 Created account ${account.username} (${account.id})")
        }
    }

    // ✅ Cập nhật tài khoản
    fun updateAccount(account: Account) {
        viewModelScope.launch {
            repository.updateAccount(account)
            println("🟡 Updated account ${account.username} (${account.id})")
        }
    }

    // ✅ Xoá tài khoản (local + Firestore)
    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
            println("🗑️ Deleted account ${account.username} (${account.id})")
        }
    }

    // ✅ Lắng nghe thay đổi từ Firestore → chèn vào Room nếu khác biệt
    init {
        viewModelScope.launch {
            repository.listenRemoteChanges()
                .distinctUntilChanged()
                .collect { remoteAccounts ->
                    remoteAccounts.forEach { acc ->
                        repository.insertAccount(acc, isRemote = true)
                    }
                }
        }
    }
}
