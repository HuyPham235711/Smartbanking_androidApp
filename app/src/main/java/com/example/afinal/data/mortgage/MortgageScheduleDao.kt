package com.example.afinal.data.mortgage

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

@Dao
interface MortgageScheduleDao {

    // ✅ Dùng REPLACE để tránh lỗi trùng khi sync từ Firestore
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(schedules: List<MortgageScheduleEntity>)

    @Query("SELECT * FROM mortgage_schedules WHERE mortgageId = :mortgageId ORDER BY period ASC")
    suspend fun getByMortgage(mortgageId: String): List<MortgageScheduleEntity>

    @Query("UPDATE mortgage_schedules SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: String, status: String)

    @Query("SELECT * FROM mortgage_schedules WHERE id = :scheduleId LIMIT 1")
    suspend fun getScheduleById(scheduleId: String): MortgageScheduleEntity?

    @Query("DELETE FROM mortgage_schedules")
    suspend fun clearAll()
}
