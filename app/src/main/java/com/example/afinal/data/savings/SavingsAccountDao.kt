package com.example.afinal.data.savings

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import androidx.room.Delete

@Dao
interface SavingsAccountDao {

    @Query("SELECT * FROM savings_account WHERE ownerAccountId = :clientId")
    suspend fun getByClient(clientId: Int): List<SavingsAccount>

    @Query("SELECT SUM(balance) FROM savings_account WHERE ownerAccountId = :clientId")
    suspend fun getTotalBalance(clientId: Int): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: SavingsAccount)

    @Update
    suspend fun update(account: SavingsAccount)

    @Delete
    suspend fun delete(account: SavingsAccount)
}
