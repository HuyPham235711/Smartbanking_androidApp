package com.example.afinal.data.interest

import androidx.room.*

@Dao
interface InterestRateDao {

    @Query("SELECT * FROM interest_rate ORDER BY termMonths")
    suspend fun getAll(): List<InterestRate>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(rate: InterestRate)

    @Update
    suspend fun update(rate: InterestRate)
}
