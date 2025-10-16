package com.example.afinal.data.mortgage

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "mortgage_accounts")
data class MortgageAccountEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val accountName: String,          // Tên khoản vay, ví dụ "Vay mua nhà"
    val principal: Long,              // Số tiền vay ban đầu (VND)
    val annualInterestRate: Double,   // Lãi suất (%/năm)
    val termMonths: Int,              // Tổng số kỳ (tháng)
    val startDate: Long,              // Ngày bắt đầu (epoch millis)
    val status: String = "ACTIVE"     // ACTIVE, PAID_OFF, OVERDUE, v.v.
)
