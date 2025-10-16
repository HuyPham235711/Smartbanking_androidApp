package com.example.afinal.data.mortgage

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "mortgage_schedules",
    foreignKeys = [
        ForeignKey(
            entity = MortgageAccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["mortgageId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class MortgageScheduleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val mortgageId: Long,          // Khóa ngoại -> MortgageAccountEntity.id
    val period: Int,               // Kỳ thứ mấy (1, 2, 3, ...)
    val dueDate: Long,             // Ngày đến hạn (epoch millis)
    val principalAmount: Long,     // Gốc phải trả kỳ này
    val interestAmount: Long,      // Lãi phải trả kỳ này
    val totalAmount: Long,         // Tổng = gốc + lãi
    val status: String = "PENDING" // PENDING / PAID
)
