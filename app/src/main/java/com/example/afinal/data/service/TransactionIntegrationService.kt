package com.example.afinal.data.service



import android.util.Log
import com.example.afinal.data.transaction.Transaction
import com.example.afinal.data.transaction.TransactionType
import com.example.afinal.data.transaction.TransactionStatus
import com.example.afinal.data.transaction.TransactionRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Transaction Integration Service
 * Member 3: Transaction & Payment Module - Week 5
 *
 * Chức năng:
 * - Tích hợp với M2 (Account Management) để cập nhật số dư
 * - Tích hợp với M4 (Utilities) để xử lý thanh toán hóa đơn
 * - Đảm bảo tính ACID của giao dịch
 * - Xử lý rollback khi giao dịch thất bại
 */
class TransactionIntegrationService {

    private val transactionRepo = TransactionRepository()

    companion object {
        private const val TAG = "TransactionIntegration"
    }

    /**
     * Xử lý giao dịch với tích hợp đầy đủ
     * Đảm bảo tính ACID (Atomicity, Consistency, Isolation, Durability)
     *
     * @param transaction Transaction object
     * @param accountService Service từ M2 (Account Management)
     * @return Result<Transaction>
     */
    suspend fun processTransactionWithAccountUpdate(
        transaction: Transaction,
        accountService: Any? = null // TODO Week 5: Replace with actual AccountService from M2
    ): Result<Transaction> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Processing transaction with account update: ${transaction.id}")

                // Step 1: Validate transaction
                if (!validateTransaction(transaction)) {
                    return@withContext Result.failure(
                        Exception("Transaction validation failed")
                    )
                }

                // Step 2: Check account balance (M2 Integration)
                // TODO Week 5: Integrate with M2
                // val hasBalance = accountService.checkBalance(
                //     accountId = transaction.fromAccountId,
                //     amount = transaction.amount + transaction.fee
                // )
                // if (!hasBalance) {
                //     return@withContext Result.failure(Exception("Insufficient balance"))
                // }

                // Step 3: Update transaction status to PROCESSING
                transactionRepo.updateTransactionStatus(
                    transaction.id,
                    TransactionStatus.PROCESSING
                )

                // Step 4: Deduct balance from source account (M2)
                // TODO Week 5: Integrate with M2
                // val deductResult = accountService.deductBalance(
                //     accountId = transaction.fromAccountId,
                //     amount = transaction.amount + transaction.fee
                // )
                // if (deductResult.isFailure) {
                //     rollbackTransaction(transaction)
                //     return@withContext Result.failure(Exception("Failed to deduct balance"))
                // }

                // Step 5: Add balance to destination account (if internal transfer)
                if (transaction.transactionType == TransactionType.TRANSFER_INTERNAL) {
                    // TODO Week 5: Integrate with M2
                    // val addResult = accountService.addBalance(
                    //     accountId = transaction.toAccountId!!,
                    //     amount = transaction.amount
                    // )
                    // if (addResult.isFailure) {
                    //     rollbackTransaction(transaction)
                    //     return@withContext Result.failure(Exception("Failed to add balance"))
                    // }
                }

                // Step 6: Update balance after transaction
                // TODO Week 5: Get updated balance from M2
                // val newBalance = accountService.getBalance(transaction.fromAccountId)
                // transactionRepo.updateBalanceAfter(transaction.id, newBalance)

                // Step 7: Update transaction status to COMPLETED
                transactionRepo.updateTransactionStatus(
                    transaction.id,
                    TransactionStatus.COMPLETED
                )

                Log.d(TAG, "Transaction completed successfully: ${transaction.id}")
                Result.success(transaction.copy(status = TransactionStatus.COMPLETED))

            } catch (e: Exception) {
                Log.e(TAG, "Error processing transaction", e)
                rollbackTransaction(transaction)
                Result.failure(Exception("Transaction failed: ${e.message}"))
            }
        }
    }

    /**
     * Xử lý thanh toán hóa đơn (tích hợp với M4)
     *
     * @param transaction Transaction object
     * @param billPaymentService Service từ M4 (Utilities)
     * @return Result<Transaction>
     */
    suspend fun processBillPayment(
        transaction: Transaction,
        billPaymentService: Any? = null // TODO Week 5: Replace with actual BillPaymentService from M4
    ): Result<Transaction> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Processing bill payment: ${transaction.id}")

                // Step 1: Validate transaction
                if (transaction.transactionType != TransactionType.BILL_PAYMENT) {
                    return@withContext Result.failure(
                        Exception("Invalid transaction type")
                    )
                }

                // Step 2: Check balance (M2)
                // TODO Week 5: Integrate with M2
                // val hasBalance = accountService.checkBalance(...)

                // Step 3: Process payment with utility service (M4)
                // TODO Week 5: Integrate with M4
                // val paymentResult = billPaymentService.processPayment(
                //     billType = transaction.metadata["billType"],
                //     billNumber = transaction.metadata["billNumber"],
                //     amount = transaction.amount
                // )
                // if (paymentResult.isFailure) {
                //     return@withContext Result.failure(Exception("Bill payment failed"))
                // }

                // Step 4: Deduct balance (M2)
                // TODO Week 5: Integrate with M2
                // accountService.deductBalance(...)

                // Step 5: Update transaction status
                transactionRepo.updateTransactionStatus(
                    transaction.id,
                    TransactionStatus.COMPLETED
                )

                Log.d(TAG, "Bill payment completed: ${transaction.id}")
                Result.success(transaction.copy(status = TransactionStatus.COMPLETED))

            } catch (e: Exception) {
                Log.e(TAG, "Error processing bill payment", e)
                rollbackTransaction(transaction)
                Result.failure(Exception("Bill payment failed: ${e.message}"))
            }
        }
    }

    /**
     * Rollback transaction khi có lỗi
     */
    private suspend fun rollbackTransaction(transaction: Transaction) {
        try {
            Log.d(TAG, "Rolling back transaction: ${transaction.id}")

            // Update status to FAILED
            transactionRepo.updateTransactionStatus(
                transaction.id,
                TransactionStatus.FAILED,
                "Transaction rolled back due to error"
            )

            // TODO Week 5: Rollback balance changes with M2
            // if (transaction.balanceAfter != null) {
            //     accountService.restoreBalance(
            //         accountId = transaction.fromAccountId,
            //         amount = transaction.amount + transaction.fee
            //     )
            // }

            Log.d(TAG, "Transaction rolled back successfully")

        } catch (e: Exception) {
            Log.e(TAG, "Error rolling back transaction", e)
        }
    }

    /**
     * Validate transaction trước khi xử lý
     */
    private fun validateTransaction(transaction: Transaction): Boolean {
        return when {
            transaction.amount <= 0 -> {
                Log.e(TAG, "Invalid amount: ${transaction.amount}")
                false
            }
            transaction.fromAccountId.isEmpty() -> {
                Log.e(TAG, "Missing fromAccountId")
                false
            }
            transaction.transactionType == TransactionType.TRANSFER_INTERNAL &&
                    transaction.toAccountId.isNullOrEmpty() -> {
                Log.e(TAG, "Missing toAccountId for internal transfer")
                false
            }
            else -> true
        }
    }

    /**
     * Kiểm tra tính nhất quán của dữ liệu sau giao dịch
     * Quan trọng cho Week 6 SIT Testing
     */
    suspend fun verifyDataConsistency(transactionId: String): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Verifying data consistency for transaction: $transactionId")

                // Step 1: Get transaction
                val txnResult = transactionRepo.getTransactionById(transactionId)
                if (txnResult.isFailure) {
                    return@withContext Result.failure(
                        Exception("Transaction not found")
                    )
                }

                val transaction = txnResult.getOrNull()!!

                // Step 2: Verify transaction status
                if (transaction.status == TransactionStatus.COMPLETED) {
                    // TODO Week 5: Verify with M2 that balance was updated
                    // val currentBalance = accountService.getBalance(transaction.fromAccountId)
                    // val expectedBalance = transaction.balanceAfter
                    // if (currentBalance != expectedBalance) {
                    //     return@withContext Result.failure(
                    //         Exception("Balance mismatch")
                    //     )
                    // }
                }

                // Step 3: Verify transaction log exists
                val historyResult = transactionRepo.getTransactionHistory(
                    transaction.fromAccountId,
                    limit = 1
                )

                if (historyResult.isFailure) {
                    return@withContext Result.failure(
                        Exception("Transaction log not found")
                    )
                }

                Log.d(TAG, "Data consistency verified")
                Result.success(true)

            } catch (e: Exception) {
                Log.e(TAG, "Error verifying data consistency", e)
                Result.failure(Exception("Verification failed: ${e.message}"))
            }
        }
    }

    /**
     * Tính toán và cập nhật số dư sau giao dịch
     * Được gọi bởi M2 sau khi cập nhật balance
     */
    suspend fun updateBalanceAfterTransaction(
        transactionId: String,
        newBalance: Double
    ): Result<Boolean> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Updating balance after transaction: $transactionId -> $newBalance")

                val result = transactionRepo.updateBalanceAfter(transactionId, newBalance)

                if (result.isSuccess) {
                    Log.d(TAG, "Balance updated successfully")
                } else {
                    Log.e(TAG, "Failed to update balance")
                }

                result

            } catch (e: Exception) {
                Log.e(TAG, "Error updating balance", e)
                Result.failure(Exception("Failed to update balance: ${e.message}"))
            }
        }
    }

    /**
     * Lấy tổng số tiền giao dịch trong ngày
     * Dùng để kiểm tra limit (Week 6)
     */
    suspend fun getDailyTransactionSummary(accountId: String): Result<DailyTransactionSummary> {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Getting daily transaction summary for: $accountId")

                val countResult = transactionRepo.getDailyTransactionCount(accountId)
                val amountResult = transactionRepo.getDailyTransactionAmount(accountId)

                if (countResult.isFailure || amountResult.isFailure) {
                    return@withContext Result.failure(
                        Exception("Failed to get transaction summary")
                    )
                }

                val summary = DailyTransactionSummary(
                    accountId = accountId,
                    transactionCount = countResult.getOrNull() ?: 0,
                    totalAmount = amountResult.getOrNull() ?: 0.0
                )

                Log.d(TAG, "Daily summary: $summary")
                Result.success(summary)

            } catch (e: Exception) {
                Log.e(TAG, "Error getting daily summary", e)
                Result.failure(Exception("Failed to get summary: ${e.message}"))
            }
        }
    }
}

/**
 * Data class cho Daily Transaction Summary
 */
data class DailyTransactionSummary(
    val accountId: String,
    val transactionCount: Int,
    val totalAmount: Double
)