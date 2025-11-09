package com.example.afinal.data.model

/**
 * Data class cho thông tin người nhận chuyển khoản liên ngân hàng
 * Member 3: Transaction & Payment - Week 4
 */
data class RecipientBankInfo(
    val bankName: String,
    val bankCode: String,
    val accountNumber: String,
    val accountHolder: String
) {
    fun isValid(): Boolean {
        return bankName.isNotEmpty() &&
                bankCode.isNotEmpty() &&
                accountNumber.isNotEmpty() &&
                accountNumber.length >= 8 &&
                accountHolder.isNotEmpty() &&
                accountHolder.length >= 3
    }

    fun toDisplayString(): String {
        return "$bankName - $accountNumber - $accountHolder"
    }

    fun toMap(): Map<String, String> {
        return mapOf(
            "bankName" to bankName,
            "bankCode" to bankCode,
            "accountNumber" to accountNumber,
            "accountHolder" to accountHolder
        )
    }

    companion object {
        fun fromMap(map: Map<String, String>): RecipientBankInfo {
            return RecipientBankInfo(
                bankName = map["bankName"] ?: "",
                bankCode = map["bankCode"] ?: "",
                accountNumber = map["accountNumber"] ?: "",
                accountHolder = map["accountHolder"] ?: ""
            )
        }
    }
}