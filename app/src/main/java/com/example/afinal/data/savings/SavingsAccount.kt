package com.example.afinal.data.savings

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "savings_account")
data class SavingsAccount(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val ownerAccountId: Int,  // ID người sở hữu (liên kết với Account.id)
    val balance: Double,      // Số dư hiện tại
    val interestRate: Double, // Lãi suất tại thời điểm mở sổ (%/năm)
    val termMonths: Int,      // Kỳ hạn (tháng)
    val openDate: String,     // Ngày mở sổ (yyyy-MM-dd)
    val maturityDate: String  // Ngày đáo hạn (yyyy-MM-dd)
)
