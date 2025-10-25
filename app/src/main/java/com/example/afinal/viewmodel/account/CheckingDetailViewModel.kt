package com.example.afinal.viewmodel.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import com.example.afinal.data.account.AccountRepository
import com.example.afinal.data.account.Account
import com.example.afinal.data.transaction.TransactionEntity
import com.example.afinal.data.transaction.TransactionRepository

class CheckingDetailViewModel(
    val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _account = MutableStateFlow<Account?>(null)
    val account: StateFlow<Account?> = _account

    fun loadAccount(id: String) {
        viewModelScope.launch {
            _account.value = accountRepository.getAccountById(id)
        }
    }

    fun updateBalance(accountId: String, newBalance: Double) {
        viewModelScope.launch {
            val acc = accountRepository.getAccountById(accountId)
            if (acc != null) {
                accountRepository.updateAccount(acc.copy(balance = newBalance))
                _account.value = acc.copy(balance = newBalance)
            }
        }
    }

    fun recordTransaction(
        accountId: String,
        type: String,
        amount: Double,
        description: String? = null
    ) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            val signedAmount = if (type == "WITHDRAW") -amount else amount
            val transaction = TransactionEntity(
                accountId = accountId,
                amount = signedAmount,
                currency = "VND",
                type = type, // "WITHDRAW", "DEPOSIT", "TRANSFER", ...
                description = description ?: when (type) {
                    "WITHDRAW" -> "Rút tiền"
                    "DEPOSIT" -> "Nạp tiền"
                    "TRANSFER" -> "Chuyển khoản"
                    else -> "Giao dịch"
                },
                timestamp = now
            )

            transactionRepository.insert(transaction)
        }
    }

    init {
        viewModelScope.launch {
            transactionRepository.listenRemoteChanges().collect { remoteTransactions ->
                val existingIds = mutableSetOf<String>()
                remoteTransactions.forEach { txn ->
                    if (!existingIds.contains(txn.id)) {
                        existingIds.add(txn.id)
                        transactionRepository.insert(txn, isRemote = true)
                    }
                }
            }
        }
    }




}
