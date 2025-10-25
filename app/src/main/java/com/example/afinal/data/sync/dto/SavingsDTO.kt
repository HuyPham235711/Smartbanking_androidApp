package com.example.afinal.data.sync.dto

data class SavingsDTO(
    val id: String = "",
    val ownerAccountId: String = "",  // 🔁 đổi Int → String
    val balance: Double = 0.0,
    val interestRate: Double = 0.0,
    val termMonths: Int = 0,
    val openDate: String = "",
    val maturityDate: String = ""
)
