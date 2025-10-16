package com.example.afinal.ui.transaction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.transaction.TransactionEntity
import com.example.afinal.data.transaction.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class TransactionViewModel(
    private val repository: TransactionRepository
) : ViewModel() {

    private val _transactions = MutableStateFlow<List<TransactionEntity>>(emptyList())
    val transactions: StateFlow<List<TransactionEntity>> = _transactions

    fun loadTransactions(accountId: Long) {
        viewModelScope.launch {
            repository.getTransactions(accountId).collect { list ->
                _transactions.value = list
            }
        }
    }

    fun addSampleTransaction() {
        viewModelScope.launch {
            val sample = TransactionEntity(
                accountId = 1L,
                amount = -150_000,
                type = "TRANSFER",
                description = "Chuyển khoản test",
                timestamp = System.currentTimeMillis()
            )
            repository.insert(sample)
        }
    }
}
