package com.example.afinal.data.transaction

import kotlinx.coroutines.flow.Flow

class TransactionRepository(
    private val dao: TransactionDao
) {
    suspend fun insert(transaction: TransactionEntity) {
        dao.insert(transaction)
    }

    fun getTransactions(accountId: Long): Flow<List<TransactionEntity>> {
        return dao.getTransactionsByAccount(accountId)
    }

    suspend fun clearAll() {
        dao.clearAll()
    }
}
