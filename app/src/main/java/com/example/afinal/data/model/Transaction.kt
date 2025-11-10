package com.example.afinal.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import java.util.UUID

/**
 * Transaction Data Model
 * Member 3: Transaction & Payment Module - Week 1
 *
 * Mô tả: Model chứa thông tin chi tiết của một giao dịch
 * Sử dụng: Lưu trữ và quản lý transaction trong Firebase Firestore
 */
data class Transaction(
    @DocumentId
    val id: String = UUID.randomUUID().toString(),

    // Loại giao dịch (DEPOSIT, TRANSFER_INTERNAL, WITHDRAW, BILL_PAYMENT)
    val transactionType: TransactionType = TransactionType.DEPOSIT,

    // Số tiền giao dịch (VND)
    val amount: Double = 0.0,

    // Đơn vị tiền tệ
    val currency: String = "VND",

    // Tài khoản nguồn (người gửi/nạp tiền)
    val fromAccountId: String = "",

    // Tài khoản đích (người nhận) - null nếu là deposit/withdraw
    val toAccountId: String? = null,

    // Trạng thái giao dịch
    val status: TransactionStatus = TransactionStatus.PENDING,

    // Thời gian tạo giao dịch
    val timestamp: Timestamp = Timestamp.now(),

    // Mô tả giao dịch
    val description: String? = null,

    // Stripe Payment Intent ID (cho tracking)
    val stripePaymentIntentId: String? = null,

    // Phí giao dịch (nếu có)
    val fee: Double = 0.0,

    // Số dư sau khi giao dịch (cho tài khoản fromAccountId)
    val balanceAfter: Double? = null,

    // Thông tin bổ sung (metadata)
    val metadata: Map<String, String>? = null,

    // Lỗi message (nếu giao dịch thất bại)
    val errorMessage: String? = null,

    // IP Address của người thực hiện giao dịch (security)
    val ipAddress: String? = null,

    // Device info (security)
    val deviceInfo: String? = null
) {
    /**
     * Kiểm tra xem giao dịch có thành công không
     */
    fun isSuccessful(): Boolean {
        return status == TransactionStatus.COMPLETED
    }

    /**
     * Kiểm tra xem giao dịch có đang chờ xử lý không
     */
    fun isPending(): Boolean {
        return status == TransactionStatus.PENDING
    }

    /**
     * Kiểm tra xem giao dịch có thất bại không
     */
    fun isFailed(): Boolean {
        return status == TransactionStatus.FAILED || status == TransactionStatus.CANCELLED
    }

    /**
     * Format số tiền theo định dạng VND
     */
    fun getFormattedAmount(): String {
        return "%,.0f %s".format(amount, currency)
    }

    /**
     * Lấy thông tin tóm tắt giao dịch
     */
    fun getSummary(): String {
        return when (transactionType) {
            TransactionType.DEPOSIT -> "Nạp tiền: ${getFormattedAmount()}"
            TransactionType.WITHDRAW -> "Rút tiền: ${getFormattedAmount()}"
            TransactionType.TRANSFER_INTERNAL -> "Chuyển tiền: ${getFormattedAmount()}"
            TransactionType.TRANSFER_EXTERNAL -> "Chuyển khoản ngân hàng: ${getFormattedAmount()}"
            TransactionType.BILL_PAYMENT -> "Thanh toán hóa đơn: ${getFormattedAmount()}"
            TransactionType.REFUND -> "Hoàn tiền: ${getFormattedAmount()}"
        }
    }

    /**
     * Convert to Map for Firestore
     */
    fun toMap(): Map<String, Any?> {
        return mapOf(
            "id" to id,
            "transactionType" to transactionType.name,
            "amount" to amount,
            "currency" to currency,
            "fromAccountId" to fromAccountId,
            "toAccountId" to toAccountId,
            "status" to status.name,
            "timestamp" to timestamp,
            "description" to description,
            "stripePaymentIntentId" to stripePaymentIntentId,
            "fee" to fee,
            "balanceAfter" to balanceAfter,
            "metadata" to metadata,
            "errorMessage" to errorMessage,
            "ipAddress" to ipAddress,
            "deviceInfo" to deviceInfo
        )
    }

    companion object {
        /**
         * Create Transaction from Firestore Map
         */
        fun fromMap(map: Map<String, Any>): Transaction {
            return Transaction(
                id = map["id"] as? String ?: "",
                transactionType = TransactionType.valueOf(
                    map["transactionType"] as? String ?: "DEPOSIT"
                ),
                amount = (map["amount"] as? Number)?.toDouble() ?: 0.0,
                currency = map["currency"] as? String ?: "VND",
                fromAccountId = map["fromAccountId"] as? String ?: "",
                toAccountId = map["toAccountId"] as? String,
                status = TransactionStatus.valueOf(
                    map["status"] as? String ?: "PENDING"
                ),
                timestamp = map["timestamp"] as? Timestamp ?: Timestamp.now(),
                description = map["description"] as? String,
                stripePaymentIntentId = map["stripePaymentIntentId"] as? String,
                fee = (map["fee"] as? Number)?.toDouble() ?: 0.0,
                balanceAfter = (map["balanceAfter"] as? Number)?.toDouble(),
                metadata = map["metadata"] as? Map<String, String>,
                errorMessage = map["errorMessage"] as? String,
                ipAddress = map["ipAddress"] as? String,
                deviceInfo = map["deviceInfo"] as? String
            )
        }
    }
}

/**
 * Transaction Type Enum
 * Định nghĩa các loại giao dịch
 */
enum class TransactionType {
    DEPOSIT,              // Nạp tiền vào tài khoản
    WITHDRAW,             // Rút tiền từ tài khoản
    TRANSFER_INTERNAL,    // Chuyển tiền nội bộ (trong hệ thống)
    TRANSFER_EXTERNAL,    // Chuyển khoản ra ngân hàng
    BILL_PAYMENT,         // Thanh toán hóa đơn (điện, nước, internet)
    REFUND                // Hoàn tiền
}

/**
 * Transaction Status Enum
 * Định nghĩa trạng thái giao dịch
 */
enum class TransactionStatus {
    PENDING,              // Đang chờ xử lý
    PROCESSING,           // Đang xử lý
    COMPLETED,            // Hoàn thành
    FAILED,               // Thất bại
    CANCELLED,            // Đã hủy
    REFUNDED              // Đã hoàn tiền
}