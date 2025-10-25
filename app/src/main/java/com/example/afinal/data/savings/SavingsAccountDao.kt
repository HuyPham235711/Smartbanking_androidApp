package com.example.afinal.data.savings

import androidx.room.*

@Dao
interface SavingsAccountDao {

    @Query("SELECT * FROM savings_account WHERE ownerAccountId = :accountId")
    suspend fun getByAccountId(accountId: String): List<SavingsAccount>

    @Query("SELECT SUM(balance) FROM savings_account WHERE ownerAccountId = :accountId")
    suspend fun getTotalBalance(accountId: String): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: SavingsAccount)

    @Update
    suspend fun update(account: SavingsAccount)

    @Delete
    suspend fun delete(account: SavingsAccount)
}
