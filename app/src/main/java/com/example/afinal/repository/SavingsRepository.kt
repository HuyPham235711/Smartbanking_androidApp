package com.example.afinal.repository

import com.example.afinal.data.savings.SavingsAccount
import com.example.afinal.data.savings.SavingsAccountDao

class SavingsRepository(private val dao: SavingsAccountDao) {

    suspend fun getAccountsByClient(clientId: Int): List<SavingsAccount> =
        dao.getByClient(clientId)

    suspend fun getTotalBalance(clientId: Int): Double =
        dao.getTotalBalance(clientId) ?: 0.0

    suspend fun insert(account: SavingsAccount) =
        dao.insert(account)

    suspend fun update(account: SavingsAccount) =
        dao.update(account)

    suspend fun delete(account: SavingsAccount) =
        dao.delete(account)
}
