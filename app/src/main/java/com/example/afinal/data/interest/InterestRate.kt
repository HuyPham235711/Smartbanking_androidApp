package com.example.afinal.data.interest

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bảng lưu lãi suất theo kỳ hạn (để Officer chỉnh sửa)
 */
@Entity(tableName = "interest_rate")
data class InterestRate(
    @PrimaryKey val termMonths: Int, // Kỳ hạn (tháng)
    val rate: Double                  // Lãi suất %/năm
)
