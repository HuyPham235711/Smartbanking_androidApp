package com.example.afinal.data.account

/**
 * Repository chịu trách nhiệm gọi DAO và xử lý logic trung gian.
 * UI (ViewModel) chỉ làm việc với Repository, không gọi DAO trực tiếp.
 */
class AccountRepository(private val accountDao: AccountDao) {

    suspend fun getAllAccounts() = accountDao.getAllAccounts()

    suspend fun getAccountById(id: Int) = accountDao.getAccountById(id)

    suspend fun insertAccount(account: Account) = accountDao.insertAccount(account)

    suspend fun updateAccount(account: Account) = accountDao.updateAccount(account)

    suspend fun deleteAccount(account: Account) = accountDao.deleteAccount(account)
}
