package com.example.afinal.data.mortgage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MortgageAccountDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(account: MortgageAccountEntity): Long

    @Query("SELECT * FROM mortgage_accounts ORDER BY id DESC")
    suspend fun getAll(): List<MortgageAccountEntity>
}
