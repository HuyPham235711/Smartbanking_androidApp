package com.example.afinal.data.mortgage

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.UUID

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
    @PrimaryKey val id: String = UUID.randomUUID().toString(), // 🔄 UUID
    val mortgageId: String, // 🔄 thay Long → String
    val period: Int,
    val dueDate: Long,
    val principalAmount: Double,
    val interestAmount: Double,
    val totalAmount: Double,
    val status: String = "PENDING"
)

