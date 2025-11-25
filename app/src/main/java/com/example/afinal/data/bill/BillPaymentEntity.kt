package com.example.afinal.data.bill

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

/**
 * Entity lưu lịch sử thanh toán hóa đơn
 */
@Entity(tableName = "bill_payments")
data class BillPaymentEntity(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val accountId: String,              // Tài khoản thanh toán
    val billType: String,               // "ELECTRIC", "WATER", "PHONE_TOPUP"
    val serviceProvider: String,        // Nhà cung cấp dịch vụ
    val customerCode: String,           // Mã khách hàng/số điện thoại
    val amount: Double,                 // Số tiền thanh toán
    val currency: String = "VND",
    val status: String = "COMPLETED",   // "PENDING", "COMPLETED", "FAILED"
    val timestamp: Long,
    val description: String? = null,
    val billPeriod: String? = null      // Kỳ hóa đơn (tháng/năm)
)

/**
 * Data class cho thông tin hóa đơn
 */
data class BillInfo(
    val customerCode: String,
    val customerName: String,
    val address: String? = null,
    val amount: Double,
    val dueDate: Long? = null,
    val period: String? = null,
    val serviceProvider: String
)

/**
 * Enum cho loại hóa đơn
 */
enum class BillType(val displayName: String) {
    ELECTRIC("Tiền điện"),
    WATER("Tiền nước"),
    PHONE_TOPUP("Nạp tiền điện thoại")
}

/**
 * Data class cho nhà cung cấp dịch vụ
 */
data class ServiceProvider(
    val id: String,
    val name: String,
    val billType: BillType,
    val icon: Int? = null
)
