package com.example.afinal.data.mortgage

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MortgageAccountDao {

    // ✅ Dùng REPLACE để tránh lỗi khi sync từ Firestore (document bị trùng ID)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: MortgageAccountEntity)

    @Query("SELECT * FROM mortgage_accounts WHERE ownerAccountId = :accountId")
    suspend fun getByOwner(accountId: String): List<MortgageAccountEntity>

    @Query("SELECT * FROM mortgage_accounts ORDER BY startDate DESC")
    suspend fun getAll(): List<MortgageAccountEntity>

    @Query("SELECT * FROM mortgage_accounts WHERE id = :id LIMIT 1")
    suspend fun getById(id: String): MortgageAccountEntity?

    @Delete
    suspend fun delete(account: MortgageAccountEntity)

    @Query("DELETE FROM mortgage_accounts")
    suspend fun clearAll()
}
