package com.example.afinal.data.sync.dto

data class MortgageDTO(
    val id: String = "",
    val accountName: String = "",
    val principal: Double = 0.0,
    val annualInterestRate: Double = 0.0,
    val termMonths: Int = 0,
    val startDate: Long = 0L,
    val status: String = "ACTIVE",
    val remainingBalance: Double = 0.0,
    val ownerAccountId: String
)
