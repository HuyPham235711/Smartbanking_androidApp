package com.example.afinal.viewmodel.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.transaction.TransactionEntity
import com.example.afinal.data.transaction.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = _transactions

    /** 🔁 Theo dõi realtime danh sách giao dịch theo accountId */
    fun observeTransactions(accountId: String) {
        viewModelScope.launch {
            repository.getTransactions(accountId).collectLatest { list ->
                _transactions.value = list
            }
        }
    }

    /** ➕ Thêm giao dịch mới */
    fun addTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            repository.insert(transaction)
            // Không cần reload vì Flow tự cập nhật
        }
    }

    /** 🧹 Xóa toàn bộ (dành cho test) */
    fun clearAll() {
        viewModelScope.launch {
            repository.clearAll()
        }
    }

    init {
        viewModelScope.launch {
            repository.listenRemoteChanges().collect { remoteTransactions ->
                remoteTransactions.forEach { txn ->
                    repository.insert(txn, isRemote = true)
                }
            }
        }
    }

}
