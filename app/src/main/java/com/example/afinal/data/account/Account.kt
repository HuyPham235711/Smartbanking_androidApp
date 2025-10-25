package com.example.afinal.data.account

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),   // ✅ giá trị mặc định
    val username: String = "",
    val password: String = "",
    val fullName: String = "",
    val email: String = "",
    val phone: String = "",
    val role: String = "Customer",
    val balance: Double = 0.0
)


