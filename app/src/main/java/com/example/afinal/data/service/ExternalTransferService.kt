package com.example.afinal.data.service

import android.util.Log
import com.example.afinal.data.config.StripeConfig
import com.example.afinal.data.transaction.Transaction
import com.example.afinal.data.transaction.TransactionType
import com.example.afinal.data.transaction.TransactionStatus
import com.example.afinal.data.model.RecipientBankInfo  // ← THÊM IMPORT NÀY
import com.example.afinal.data.transaction.TransactionRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.UUID

/**
 * External Transfer Service - Chuyển khoản liên ngân hàng
 * Member 3: Transaction & Payment Module - Week 4
 *
 * Features:
 * - Tra cứu tên chủ tài khoản
 * - Xử lý chuyển khoản liên ngân hàng
 * - Xác thực OTP
 */
class ExternalTransferService {

    private val transactionRepo = TransactionRepository()

    companion object {
        private const val TAG = "ExternalTransferService"
    }

    /**
     * Tra cứu tên chủ tài khoản
     * TODO Week 4: Tích hợp với API ngân hàng thực
     */
    suspend fun inquiryAccountName(
        bankCode: String,
        accountNumber: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Inquiry account: $bankCode - $accountNumber")

                // Simulate inquiry delay
                kotlinx.coroutines.delay(1500)

                // Mock response - trong production sẽ call API ngân hàng
                if (accountNumber.length < 8) {
                    return@withContext Result.failure(
                        Exception("Số tài khoản không hợp lệ")
                    )
                }

                // Generate mock account holder name
                val mockName = when (bankCode) {
                    "VCB" -> "NGUYEN VAN A"
                    "TCB" -> "TRAN THI B"
                    "BIDV" -> "LE VAN C"
                    else -> "MOCK ACCOUNT HOLDER"
                }

                Log.d(TAG, "Account inquiry successful: $mockName")
                Result.success(mockName)

            } catch (e: Exception) {
                Log.e(TAG, "Error inquiring account", e)
                Result.failure(Exception("Không thể tra cứu tài khoản: ${e.message}"))
            }
        }
    }

    /**
     * Xử lý chuyển khoản liên ngân hàng
     */
    suspend fun processExternalTransfer(
        fromAccountId: String,
        recipientInfo: RecipientBankInfo,
        amount: Double,
        description: String?,
        otpCode: String
    ): Result<Transaction> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Processing external transfer: $amount VND")

                // 1. Validate amount
                if (amount < StripeConfig.Limits.MIN_TRANSFER_VND) {
                    return@withContext Result.failure(
                        Exception("Số tiền tối thiểu là ${StripeConfig.Limits.MIN_TRANSFER_VND} VND")
                    )
                }

                if (amount > StripeConfig.Limits.MAX_TRANSFER_VND) {
                    return@withContext Result.failure(
                        Exception("Số tiền tối đa là ${StripeConfig.Limits.MAX_TRANSFER_VND} VND")
                    )
                }

                // 2. Validate recipient info
                if (!recipientInfo.isValid()) {
                    return@withContext Result.failure(
                        Exception("Thông tin người nhận không hợp lệ")
                    )
                }

                // 3. Validate OTP
                // TODO Week 4: Tích hợp với M1 OTP Service
                if (otpCode.length != 6) {
                    return@withContext Result.failure(
                        Exception("Mã OTP không hợp lệ")
                    )
                }

                // Simulate OTP verification
                val isOtpValid = verifyOTP(otpCode)
                if (!isOtpValid) {
                    return@withContext Result.failure(
                        Exception("Mã OTP không đúng")
                    )
                }

                // 4. Calculate fee
                val fee = StripeConfig.calculateFee(amount, "TRANSFER_EXTERNAL")

                // 5. Create transaction
                val transaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    transactionType = TransactionType.TRANSFER_EXTERNAL,
                    amount = amount,
                    currency = StripeConfig.Currency.VND.uppercase(),
                    fromAccountId = fromAccountId,
                    toAccountId = recipientInfo.accountNumber,
                    status = TransactionStatus.PROCESSING,
                    timestamp = Timestamp.now(),
                    description = description ?: "Chuyển khoản ${recipientInfo.bankName}",
                    fee = fee,
                    metadata = recipientInfo.toMap()
                )

                // 6. Save transaction
                val saveResult = transactionRepo.saveTransaction(transaction)
                if (saveResult.isFailure) {
                    return@withContext Result.failure(saveResult.exceptionOrNull()!!)
                }

                // 7. Process transfer (simulation)
                // TODO Week 4: Tích hợp API ngân hàng
                kotlinx.coroutines.delay(2000) // Simulate processing

                // 8. Update status to completed
                transactionRepo.updateTransactionStatus(
                    transaction.id,
                    TransactionStatus.COMPLETED
                )

                Log.d(TAG, "External transfer completed: ${transaction.id}")
                Result.success(transaction.copy(status = TransactionStatus.COMPLETED))

            } catch (e: Exception) {
                Log.e(TAG, "Error processing external transfer", e)
                Result.failure(Exception("Chuyển khoản thất bại: ${e.message}"))
            }
        }
    }

    /**
     * Kiểm tra trạng thái giao dịch
     */
    suspend fun checkTransferStatus(transactionId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Checking transfer status: $transactionId")

                val result = transactionRepo.getTransactionById(transactionId)

                result.onSuccess { transaction ->
                    return@withContext Result.success(transaction.status.name)
                }

                result.onFailure { error ->
                    return@withContext Result.failure(error)
                }

                Result.failure(Exception("Unknown error"))

            } catch (e: Exception) {
                Log.e(TAG, "Error checking status", e)
                Result.failure(Exception("Không thể kiểm tra trạng thái: ${e.message}"))
            }
        }
    }

    /**
     * Verify OTP code
     * TODO Week 4: Replace with M1 OTP Service
     */
    private fun verifyOTP(otpCode: String): Boolean {
        // Simulation: Accept any 6-digit OTP in test mode
        if (StripeConfig.TestMode.IS_TEST_MODE) {
            return otpCode.length == 6 && otpCode.all { it.isDigit() }
        }

        // In production, call M1 OTP verification service
        return true
    }
}
