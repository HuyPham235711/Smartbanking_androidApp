package com.example.afinal.data.sync.dto

data class ScheduleDTO(
    val id: String = "",
    val mortgageId: String = "",
    val period: Int = 0,
    val dueDate: Long = 0L,
    val principalAmount: Double = 0.0,
    val interestAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val status: String = "PENDING"
)
