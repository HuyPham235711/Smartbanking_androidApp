package com.example.afinal.data.service


import android.util.Log
import com.example.afinal.data.config.StripeConfig
import com.example.afinal.data.transaction.Transaction
import com.example.afinal.data.transaction.TransactionType
import com.example.afinal.data.transaction.TransactionStatus
import com.example.afinal.data.transaction.TransactionRepository
import com.google.firebase.Timestamp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.util.UUID

/**
 * Withdraw Service - Xử lý rút tiền
 * Member 3: Transaction & Payment Module - Week 3
 *
 * Flow:
 * 1. Validate số tiền và thông tin ngân hàng
 * 2. Kiểm tra số dư tài khoản (tích hợp với M2)
 * 3. Tạo Payout với Stripe
 * 4. Lưu transaction log
 * 5. Cập nhật số dư tài khoản
 */
class WithdrawService {

    private val transactionRepo = TransactionRepository()

    companion object {
        private const val TAG = "WithdrawService"
    }

    /**
     * Xử lý Withdraw (Rút tiền về ngân hàng)
     *
     * @param accountId ID tài khoản rút tiền
     * @param amount Số tiền rút (VND)
     * @param bankAccountInfo Thông tin tài khoản ngân hàng
     * @param description Mô tả
     * @return Result<Transaction>
     */
    suspend fun processWithdraw(
        accountId: String,
        amount: Double,
        bankAccountInfo: BankAccountInfo,
        description: String? = null
    ): Result<Transaction> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Processing withdraw: $amount VND from account $accountId")

                // 1. Validate amount
                if (amount < StripeConfig.Limits.MIN_WITHDRAW_VND) {
                    return@withContext Result.failure(
                        Exception("Số tiền tối thiểu là ${StripeConfig.Limits.MIN_WITHDRAW_VND} VND")
                    )
                }

                if (amount > StripeConfig.Limits.MAX_WITHDRAW_VND) {
                    return@withContext Result.failure(
                        Exception("Số tiền tối đa là ${StripeConfig.Limits.MAX_WITHDRAW_VND} VND")
                    )
                }

                // 2. Validate bank account info
                val validation = validateBankAccountInfo(bankAccountInfo)
                if (validation.isFailure) {
                    return@withContext validation
                }

                // 3. TODO Week 3: Kiểm tra số dư với M2
                // val balanceCheck = accountService.checkBalance(accountId, amount + fee)
                // if (!balanceCheck) return Result.failure(Exception("Số dư không đủ"))

                // 4. Tính phí
                val fee = StripeConfig.calculateFee(amount, "WITHDRAW")
                val totalAmount = amount + fee

                // 5. Tạo transaction
                val transaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    transactionType = TransactionType.WITHDRAW,
                    amount = amount,
                    currency = StripeConfig.Currency.VND.uppercase(),
                    fromAccountId = accountId,
                    toAccountId = null,
                    status = TransactionStatus.PROCESSING,
                    timestamp = Timestamp.now(),
                    description = description ?: "Rút tiền về ngân hàng ${bankAccountInfo.bankName}",
                    fee = fee,
                    metadata = mapOf(
                        "bankName" to bankAccountInfo.bankName,
                        "accountNumber" to bankAccountInfo.accountNumber,
                        "accountHolder" to bankAccountInfo.accountHolder
                    )
                )

                // 6. Lưu transaction
                val saveResult = transactionRepo.saveTransaction(transaction)
                if (saveResult.isFailure) {
                    return@withContext Result.failure(saveResult.exceptionOrNull()!!)
                }

                // 7. Tạo Payout với Stripe (Simulation)
                if (StripeConfig.TestMode.IS_TEST_MODE) {
                    // Simulate payout success
                    Log.d(TAG, "TEST MODE: Simulating payout success")

                    // Update transaction status
                    transactionRepo.updateTransactionStatus(
                        transaction.id,
                        TransactionStatus.COMPLETED
                    )

                    // TODO Week 3: Cập nhật số dư với M2
                    // accountService.deductBalance(accountId, totalAmount)

                    return@withContext Result.success(
                        transaction.copy(status = TransactionStatus.COMPLETED)
                    )
                } else {
                    // Production: Call Stripe Payout API
                    val payoutResult = createStripePayout(
                        amount = amount,
                        bankAccount = bankAccountInfo
                    )

                    if (payoutResult.isSuccess) {
                        transactionRepo.updateTransactionStatus(
                            transaction.id,
                            TransactionStatus.COMPLETED
                        )
                        Result.success(transaction.copy(status = TransactionStatus.COMPLETED))
                    } else {
                        transactionRepo.updateTransactionStatus(
                            transaction.id,
                            TransactionStatus.FAILED,
                            payoutResult.exceptionOrNull()?.message
                        )
                        Result.failure(payoutResult.exceptionOrNull()!!)
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error processing withdraw", e)
                Result.failure(Exception("Rút tiền thất bại: ${e.message}"))
            }
        }
    }

    /**
     * Validate thông tin tài khoản ngân hàng
     */
    private fun validateBankAccountInfo(info: BankAccountInfo): Result<Transaction> {
        return when {
            info.bankName.isEmpty() -> {
                Result.failure(Exception("Tên ngân hàng không được để trống"))
            }
            info.accountNumber.isEmpty() -> {
                Result.failure(Exception("Số tài khoản không được để trống"))
            }
            info.accountNumber.length < 8 -> {
                Result.failure(Exception("Số tài khoản không hợp lệ (tối thiểu 8 ký tự)"))
            }
            info.accountHolder.isEmpty() -> {
                Result.failure(Exception("Tên chủ tài khoản không được để trống"))
            }
            info.accountHolder.length < 3 -> {
                Result.failure(Exception("Tên chủ tài khoản không hợp lệ"))
            }
            else -> Result.success(Transaction()) // Dummy success
        }
    }

    /**
     * Tạo Stripe Payout (for production)
     */
    private suspend fun createStripePayout(
        amount: Double,
        bankAccount: BankAccountInfo
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Creating Stripe Payout")

                val amountInCents = amount.toInt()
                val url = URL("${StripeConfig.BASE_URL}payouts")
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer ${StripeConfig.SECRET_KEY}")
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.doOutput = true

                val requestBody = "amount=$amountInCents" +
                        "&currency=${StripeConfig.Currency.VND}" +
                        "&method=standard" +
                        "&description=Withdraw to ${bankAccount.bankName}"

                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(requestBody)
                writer.flush()
                writer.close()

                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val payoutId = jsonResponse.getString("id")

                    Log.d(TAG, "Payout created: $payoutId")
                    Result.success(payoutId)
                } else {
                    val error = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e(TAG, "Stripe Payout Error: $error")
                    Result.failure(Exception("Payout failed: $responseCode"))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error creating payout", e)
                Result.failure(Exception("Không thể tạo payout: ${e.message}"))
            }
        }
    }

    /**
     * Lấy danh sách ngân hàng Việt Nam phổ biến
     */
    fun getVietnameseBanks(): List<BankInfo> {
        return listOf(
            BankInfo("VCB", "Vietcombank", "970436"),
            BankInfo("TCB", "Techcombank", "970407"),
            BankInfo("BIDV", "BIDV", "970418"),
            BankInfo("VTB", "Vietinbank", "970415"),
            BankInfo("ACB", "ACB", "970416"),
            BankInfo("MB", "MB Bank", "970422"),
            BankInfo("VPB", "VPBank", "970432"),
            BankInfo("TPB", "TPBank", "970423"),
            BankInfo("STB", "Sacombank", "970403"),
            BankInfo("HDB", "HDBank", "970437"),
            BankInfo("VIB", "VIB", "970441"),
            BankInfo("SHB", "SHB", "970443"),
            BankInfo("EIB", "Eximbank", "970431"),
            BankInfo("MSB", "MSB", "970426"),
            BankInfo("OCB", "OCB", "970448"),
            BankInfo("SEA", "SeABank", "970440"),
            BankInfo("ABB", "ABBank", "970425"),
            BankInfo("VAB", "VietABank", "970427"),
            BankInfo("NAB", "NamABank", "970428"),
            BankInfo("PGB", "PGBank", "970430")
        )
    }
}

/**
 * Data class cho thông tin tài khoản ngân hàng
 */
data class BankAccountInfo(
    val bankName: String,
    val bankCode: String,
    val accountNumber: String,
    val accountHolder: String
)

/**
 * Data class cho thông tin ngân hàng
 */
data class BankInfo(
    val code: String,
    val name: String,
    val binCode: String
)