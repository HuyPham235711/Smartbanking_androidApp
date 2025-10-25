package com.example.afinal.data.sync.dto

data class InterestDTO(
    val termMonths: Int = 0,
    val rate: Double = 0.0,
    val updatedAt: Long = System.currentTimeMillis()
)
