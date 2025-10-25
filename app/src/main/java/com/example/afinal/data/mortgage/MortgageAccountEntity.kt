package com.example.afinal.data.mortgage

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "mortgage_accounts")
data class MortgageAccountEntity(
    @PrimaryKey val id: String = UUID.randomUUID().toString(), // 🔄 UUID
    val accountName: String,
    val principal: Double,
    val annualInterestRate: Double,
    val termMonths: Int,
    val startDate: Long,
    val status: String = "ACTIVE",
    val remainingBalance: Double,
    val ownerAccountId: String
)

