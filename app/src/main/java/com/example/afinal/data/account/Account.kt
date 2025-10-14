package com.example.afinal.data.account

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val username: String,
    val password: String,
    val fullName: String,
    val email: String,
    val phone: String,
    val role: String = "Customer"
)
