package com.example.afinal.data.savings

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "savings_account")
data class SavingsAccount(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),   // ✅ Dùng UUID thay vì Int tự tăng
    val ownerAccountId: String,   // id của Account (chuỗi UUID)
    val balance: Double,
    val interestRate: Double,
    val termMonths: Int,
    val openDate: String,
    val maturityDate: String
)
