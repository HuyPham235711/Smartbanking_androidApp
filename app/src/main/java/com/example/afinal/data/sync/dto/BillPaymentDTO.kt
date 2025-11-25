package com.example.afinal.data.sync.dto

data class BillPaymentDTO(
    val id: String = "",
    val accountId: String = "",
    val billType: String = "",
    val serviceProvider: String = "",
    val customerCode: String = "",
    val amount: Double = 0.0,
    val currency: String = "VND",
    val status: String = "COMPLETED",
    val timestamp: Long = 0L,
    val description: String? = null,
    val billPeriod: String? = null
)

