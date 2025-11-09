package com.example.afinal.service

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
 * Stripe Payment Service - FIXED VERSION
 * Member 3: Transaction & Payment Module - Week 2
 *
 * FIXES:
 * - Mock Stripe API in Test Mode to avoid network hang
 * - Add proper timeout handling
 * - Simulate instant payment success
 * - Better error handling
 */
class StripePaymentService {

    private val transactionRepo = TransactionRepository()

    companion object {
        private const val TAG = "StripePaymentService"
    }

    /**
     * Process Deposit (Mock version for testing)
     */
    suspend fun processDeposit(
        accountId: String,
        amount: Double,
        description: String? = null
    ): Result<Transaction> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Processing deposit: $amount VND for account $accountId")

                // 1. Validate
                if (!StripeConfig.isConfigured()) {
                    return@withContext Result.failure(
                        Exception("Stripe chưa được cấu hình. Vui lòng thêm API keys.")
                    )
                }

                val validation = StripeConfig.validateAmount(amount, StripeConfig.Currency.VND)
                if (validation.isFailure) {
                    return@withContext Result.failure(validation.exceptionOrNull()!!)
                }

                // 2. Check if Test Mode (ALWAYS TRUE for testing)
                val isTestMode = true // Force test mode
                val paymentIntentId = if (isTestMode) {
                    // MOCK: Generate fake payment intent ID
                    Log.d(TAG, "TEST MODE: Skipping real Stripe API call")
                    "pi_test_${UUID.randomUUID().toString().take(24)}"
                } else {
                    // PRODUCTION: Call real Stripe API
                    val result = createPaymentIntentReal(
                        amount = amount,
                        currency = StripeConfig.Currency.VND,
                        description = description ?: "Nạp tiền vào tài khoản"
                    )

                    if (result.isFailure) {
                        return@withContext Result.failure(result.exceptionOrNull()!!)
                    }

                    result.getOrNull()
                }

                Log.d(TAG, "Payment Intent ID: $paymentIntentId")

                // 3. Create Transaction
                val transaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    transactionType = TransactionType.DEPOSIT,
                    amount = amount,
                    currency = StripeConfig.Currency.VND.uppercase(),
                    fromAccountId = accountId,
                    toAccountId = null,
                    status = TransactionStatus.PENDING,
                    timestamp = Timestamp.now(),
                    description = description,
                    stripePaymentIntentId = paymentIntentId,
                    fee = StripeConfig.calculateFee(amount, "DEPOSIT")
                )

                // 4. Save to Firebase
                val saveResult = transactionRepo.saveTransaction(transaction)

                if (saveResult.isFailure) {
                    return@withContext Result.failure(saveResult.exceptionOrNull()!!)
                }

                Log.d(TAG, "Transaction saved: ${transaction.id}")

                // 5. Simulate payment success in Test Mode
                if (StripeConfig.TestMode.IS_TEST_MODE) {
                    // Simulate processing delay (1 second)
                    kotlinx.coroutines.delay(1000)

                    // Auto complete transaction
                    transactionRepo.updateTransactionStatus(
                        transaction.id,
                        TransactionStatus.COMPLETED
                    )

                    Log.d(TAG, "TEST MODE: Deposit completed successfully")

                    return@withContext Result.success(
                        transaction.copy(status = TransactionStatus.COMPLETED)
                    )
                }

                // Production: Return pending transaction
                Result.success(transaction)

            } catch (e: Exception) {
                Log.e(TAG, "Error processing deposit", e)
                Result.failure(Exception("Nạp tiền thất bại: ${e.message}"))
            }
        }
    }

    /**
     * Process Internal Transfer (Mock version)
     */
    suspend fun processInternalTransfer(
        fromAccountId: String,
        toAccountId: String,
        amount: Double,
        description: String? = null
    ): Result<Transaction> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Processing internal transfer: $amount VND from $fromAccountId to $toAccountId")

                // 1. Validate
                if (fromAccountId == toAccountId) {
                    return@withContext Result.failure(
                        Exception("Không thể chuyển tiền cho chính mình")
                    )
                }

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

                // 2. Calculate fee
                val fee = StripeConfig.calculateFee(amount, "TRANSFER")

                // 3. Create Transaction
                val transaction = Transaction(
                    id = UUID.randomUUID().toString(),
                    transactionType = TransactionType.TRANSFER_INTERNAL,
                    amount = amount,
                    currency = StripeConfig.Currency.VND.uppercase(),
                    fromAccountId = fromAccountId,
                    toAccountId = toAccountId,
                    status = TransactionStatus.PROCESSING,
                    timestamp = Timestamp.now(),
                    description = description ?: "Chuyển tiền nội bộ",
                    fee = fee
                )

                // 4. Save transaction
                val saveResult = transactionRepo.saveTransaction(transaction)

                if (saveResult.isFailure) {
                    return@withContext Result.failure(saveResult.exceptionOrNull()!!)
                }

                // 5. Simulate transfer success (Test Mode)
                if (StripeConfig.TestMode.IS_TEST_MODE) {
                    kotlinx.coroutines.delay(1000)

                    transactionRepo.updateTransactionStatus(
                        transaction.id,
                        TransactionStatus.COMPLETED
                    )

                    Log.d(TAG, "TEST MODE: Transfer completed successfully")

                    return@withContext Result.success(
                        transaction.copy(status = TransactionStatus.COMPLETED)
                    )
                }

                // Production: Would call actual transfer logic here
                // TODO Week 3: Integrate with M2 Account Service

                Result.success(transaction)

            } catch (e: Exception) {
                Log.e(TAG, "Error processing internal transfer", e)
                Result.failure(Exception("Chuyển tiền thất bại: ${e.message}"))
            }
        }
    }

    /**
     * Create Payment Intent with Real Stripe API (Production only)
     * NOT USED in Test Mode
     */
    private suspend fun createPaymentIntentReal(
        amount: Double,
        currency: String,
        description: String
    ): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Creating Payment Intent with Stripe API")

                val amountInCents = amount.toInt()
                val url = URL("${StripeConfig.BASE_URL}${StripeConfig.Endpoints.PAYMENT_INTENTS}")
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "POST"
                connection.setRequestProperty("Authorization", "Bearer ${StripeConfig.SECRET_KEY}")
                connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded")
                connection.doOutput = true

                // Set aggressive timeouts
                connection.connectTimeout = 5000 // 5 seconds
                connection.readTimeout = 5000    // 5 seconds

                val requestBody = "amount=$amountInCents" +
                        "&currency=$currency" +
                        "&description=${java.net.URLEncoder.encode(description, "UTF-8")}" +
                        "&automatic_payment_methods[enabled]=true"

                // Send request
                val writer = OutputStreamWriter(connection.outputStream)
                writer.write(requestBody)
                writer.flush()
                writer.close()

                // Read response
                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val paymentIntentId = jsonResponse.getString("id")

                    Log.d(TAG, "Payment Intent created: $paymentIntentId")
                    Result.success(paymentIntentId)

                } else {
                    val errorResponse = connection.errorStream?.bufferedReader()?.use { it.readText() }
                    Log.e(TAG, "Stripe API Error: $errorResponse")
                    Result.failure(Exception("Stripe API Error: $responseCode"))
                }

            } catch (e: java.net.SocketTimeoutException) {
                Log.e(TAG, "Stripe API timeout", e)
                Result.failure(Exception("Kết nối Stripe timeout. Vui lòng thử lại."))
            } catch (e: Exception) {
                Log.e(TAG, "Error creating Payment Intent", e)
                Result.failure(Exception("Không thể tạo Payment Intent: ${e.message}"))
            }
        }
    }

    /**
     * Check Payment Intent Status (Mock in Test Mode)
     */
    suspend fun getPaymentIntentStatus(paymentIntentId: String): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Checking Payment Intent status: $paymentIntentId")

                if (StripeConfig.TestMode.IS_TEST_MODE) {
                    // Mock response
                    Log.d(TAG, "TEST MODE: Returning mock status")
                    return@withContext Result.success("succeeded")
                }

                // Production: Call real API
                val url = URL("${StripeConfig.BASE_URL}${StripeConfig.Endpoints.PAYMENT_INTENTS}/$paymentIntentId")
                val connection = url.openConnection() as HttpURLConnection

                connection.requestMethod = "GET"
                connection.setRequestProperty("Authorization", "Bearer ${StripeConfig.SECRET_KEY}")
                connection.connectTimeout = 5000
                connection.readTimeout = 5000

                val responseCode = connection.responseCode

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val response = connection.inputStream.bufferedReader().use { it.readText() }
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.getString("status")

                    Log.d(TAG, "Payment Intent status: $status")
                    Result.success(status)

                } else {
                    Result.failure(Exception("Cannot get payment status"))
                }

            } catch (e: Exception) {
                Log.e(TAG, "Error checking payment status", e)
                Result.failure(Exception("Không thể kiểm tra trạng thái: ${e.message}"))
            }
        }
    }

    /**
     * Process Withdraw (Week 3 - Not implemented yet)
     */
    suspend fun processWithdraw(
        accountId: String,
        amount: Double,
        bankAccountInfo: String,
        description: String? = null
    ): Result<Transaction> {
        return Result.failure(Exception("Chức năng rút tiền sẽ được phát triển trong Week 3"))
    }
}