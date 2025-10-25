package com.example.afinal.data.sync.dto

data class TransactionDTO(
    val id: String = "",
    val accountId: String = "",
    val amount: Double = 0.0,
    val currency: String = "VND",
    val type: String = "",
    val description: String? = null,
    val timestamp: Long = 0L
)

