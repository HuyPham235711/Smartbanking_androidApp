package com.example.afinal.data.config

/**
 * QUAN TRỌNG:
 * 1. Thay YOUR_KEY_HERE bằng keys từ https://dashboard.stripe.com/test/apikeys
 * 2. KHÔNG commit Secret Key lên Git
 * 3. Sử dụng Test Mode keys (có prefix pk_test_ và sk_test_)
 */
object StripeConfig {

    // ========== STRIPE API KEYS ==========
    // TODO: Thay đổi keys này từ Stripe Dashboard
    const val PUBLISHABLE_KEY = "pk_test_51SFGxW0zNxlwMyeXWA2mapdXMobGkONBk6cx5plO1sNjaidfwqJ7npfOTuFPsVKq4YTkbEOiSMW5Nxxlz817sDXa00onno0T85"
    const val SECRET_KEY = "sk_test_51SFGxW0zNxlwMyeXs6Q2Qh6L0dZbuEyeOFJFwtWva52I7pXyFz6Hgq4q4IgVRBAZed2yKcTpAxqX6yCzYpgARgdR00hKO7jwjB"

    // ========== API ENDPOINTS ==========
    const val BASE_URL = "https://api.stripe.com/v1/"

    object Endpoints {
        const val PAYMENT_INTENTS = "payment_intents"
        const val PAYMENT_METHODS = "payment_methods"
        const val CUSTOMERS = "customers"
        const val CHARGES = "charges"
    }

    // ========== CURRENCY SETTINGS ==========
    object Currency {
        const val VND = "vnd"
        const val USD = "usd"
    }

    // ========== TRANSACTION LIMITS ==========
    object Limits {
        // Deposit limits (VND)
        const val MIN_DEPOSIT_VND = 5000.0
        const val MAX_DEPOSIT_VND = 50000000.0  // 50 triệu

        // Transfer limits (VND)
        const val MIN_TRANSFER_VND = 1000.0
        const val MAX_TRANSFER_VND = 100000000.0  // 100 triệu

        // Withdraw limits (VND)
        const val MIN_WITHDRAW_VND = 50000.0
        const val MAX_WITHDRAW_VND = 20000000.0  // 20 triệu

        // Daily limits
        const val MAX_DAILY_TRANSACTIONS = 20
        const val MAX_DAILY_AMOUNT_VND = 200000000.0  // 200 triệu
    }

    // ========== TRANSACTION FEES ==========
    object Fees {
        // Phí giao dịch (%)
        const val DEPOSIT_FEE_PERCENT = 0.0  // Miễn phí nạp tiền
        const val TRANSFER_FEE_PERCENT = 0.1  // 0.1% phí chuyển khoản
        const val WITHDRAW_FEE_PERCENT = 0.5  // 0.5% phí rút tiền

        // Phí cố định (VND)
        const val TRANSFER_FIXED_FEE = 1000.0
        const val WITHDRAW_FIXED_FEE = 5000.0
    }

    // ========== TIMEOUT SETTINGS ==========
    object Timeout {
        const val CONNECT_TIMEOUT_SECONDS = 30L
        const val READ_TIMEOUT_SECONDS = 30L
        const val WRITE_TIMEOUT_SECONDS = 30L
    }

    // ========== TEST MODE ==========
    object TestMode {
        const val IS_TEST_MODE = true  // Set false khi production

        // Test cards
        const val TEST_CARD_SUCCESS = "4242424242424242"
        const val TEST_CARD_DECLINE = "4000000000000002"
        const val TEST_CARD_INSUFFICIENT = "4000000000009995"
        const val TEST_CARD_3D_SECURE = "4000002500003155"
    }

    // ========== VALIDATION FUNCTIONS ==========

    /**
     * Kiểm tra xem Stripe đã được cấu hình chưa
     */
    fun isConfigured(): Boolean {
        return PUBLISHABLE_KEY != "pk_test_YOUR_PUBLISHABLE_KEY_HERE" &&
                SECRET_KEY != "sk_test_YOUR_SECRET_KEY_HERE" &&
                PUBLISHABLE_KEY.isNotEmpty() &&
                SECRET_KEY.isNotEmpty()
    }

    /**
     * Validate số tiền giao dịch
     */
    fun validateAmount(amount: Double, currency: String): Result<Boolean> {
        return when {
            amount <= 0 -> {
                Result.failure(Exception("Số tiền phải lớn hơn 0"))
            }
            currency.uppercase() == Currency.VND && amount < Limits.MIN_DEPOSIT_VND -> {
                Result.failure(Exception("Số tiền tối thiểu là ${Limits.MIN_DEPOSIT_VND} VND"))
            }
            currency.uppercase() == Currency.VND && amount > Limits.MAX_DEPOSIT_VND -> {
                Result.failure(Exception("Số tiền tối đa là ${Limits.MAX_DEPOSIT_VND} VND"))
            }
            else -> Result.success(true)
        }
    }

    /**
     * Tính phí giao dịch
     */
    fun calculateFee(amount: Double, transactionType: String): Double {
        return when (transactionType.uppercase()) {
            "DEPOSIT" -> {
                // Miễn phí nạp tiền
                0.0
            }
            "TRANSFER", "TRANSFER_INTERNAL" -> {
                // Phí % + phí cố định
                val percentFee = amount * (Fees.TRANSFER_FEE_PERCENT / 100)
                percentFee + Fees.TRANSFER_FIXED_FEE
            }
            "WITHDRAW" -> {
                // Phí % + phí cố định
                val percentFee = amount * (Fees.WITHDRAW_FEE_PERCENT / 100)
                percentFee + Fees.WITHDRAW_FIXED_FEE
            }
            else -> 0.0
        }
    }

    /**
     * Format số tiền theo currency
     */
    fun formatAmount(amount: Double, currency: String): String {
        return when (currency.uppercase()) {
            Currency.VND -> "%,.0f VND".format(amount)
            Currency.USD -> "$%.2f".format(amount)
            else -> "%,.2f %s".format(amount, currency)
        }
    }

    /**
     * Kiểm tra xem có phải test card không
     */
    fun isTestCard(cardNumber: String): Boolean {
        val cleanCard = cardNumber.replace(" ", "")
        return TestMode.IS_TEST_MODE && (
                cleanCard == TestMode.TEST_CARD_SUCCESS ||
                        cleanCard == TestMode.TEST_CARD_DECLINE ||
                        cleanCard == TestMode.TEST_CARD_INSUFFICIENT ||
                        cleanCard == TestMode.TEST_CARD_3D_SECURE
                )
    }
}