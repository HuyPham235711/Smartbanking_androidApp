package com.example.afinal.data.account

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * DAO (Data Access Object) — cung cấp các hàm CRUD cho bảng Account
 */
@Dao
interface AccountDao {

    @Query("SELECT * FROM accounts WHERE email = :email LIMIT 1")
    suspend fun getAccountByEmail(email: String): Account?

    @Query("SELECT * FROM accounts")
    suspend fun getAllAccounts(): List<Account>


    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: String): Account?   // ✅ đổi Int → String

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account)

    @Update
    suspend fun updateAccount(account: Account)

    @Delete
    suspend fun deleteAccount(account: Account)

    @Query("SELECT * FROM accounts")
    suspend fun getAll(): List<Account>

    @Query("SELECT id FROM accounts ORDER BY id LIMIT 1")
    suspend fun getFirstAccountId(): String?           // ✅ đổi Int → String

    @Query("SELECT * FROM accounts")
    fun observeAll(): Flow<List<Account>>
}

