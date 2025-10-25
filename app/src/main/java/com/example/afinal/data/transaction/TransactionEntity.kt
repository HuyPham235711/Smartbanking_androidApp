package com.example.afinal.data.transaction

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val accountId: String,
    val amount: Double,
    val currency: String,
    val type: String,
    val description: String?,
    val timestamp: Long
)

