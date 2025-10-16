package com.example.afinal.data.mortgage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MortgageScheduleDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<MortgageScheduleEntity>)

    @Query("SELECT * FROM mortgage_schedules WHERE mortgageId = :mortgageId ORDER BY period ASC")
    suspend fun getByMortgage(mortgageId: Long): List<MortgageScheduleEntity>
}
