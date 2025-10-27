package com.example.afinal.data.sync.dto

data class AccountDTO(
    val id: String = "0",
    val username: String = "",
    val password: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "Customer",
    val balance: Double = 0.0
)
