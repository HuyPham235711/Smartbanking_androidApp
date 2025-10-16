package com.example.afinal.data.transaction

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "transactions")
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val accountId: Long,
    val amount: Long,            // đơn vị: VND
    val currency: String = "VND",
    val type: String,            // TRANSFER, DEPOSIT, WITHDRAW, MORTGAGE_PAYMENT
    val description: String? = null,
    val timestamp: Long          // epoch millis
)
