package com.example.afinal.data.repository

import android.util.Log
import com.example.afinal.data.model.Transaction
import com.example.afinal.data.model.TransactionType
import com.example.afinal.data.model.TransactionStatus
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.tasks.await
import java.util.Calendar

/**
 * Transaction Repository
 * Member 3: Transaction & Payment Module - Week 2
 *
 * Chức năng:
 * - Lưu transaction vào Firestore
 * - Lấy lịch sử giao dịch
 * - Cập nhật trạng thái transaction
 * - Query transactions theo filter
 */
class TransactionTransferRepository {

    private val db = FirebaseFirestore.getInstance()
    private val transactionsCollection = db.collection("transactions")

    companion object {
        private const val TAG = "TransactionTransferRepository"
    }

    /**
     * Lưu transaction vào Firestore
     */
    suspend fun saveTransaction(transaction: Transaction): Result<String> {
        return try {
            Log.d(TAG, "Saving transaction: ${transaction.id}")

            // Lưu vào Firestore
            transactionsCollection
                .document(transaction.id)
                .set(transaction.toMap())
                .await()

            Log.d(TAG, "Transaction saved successfully: ${transaction.id}")
            Result.success(transaction.id)

        } catch (e: Exception) {
            Log.e(TAG, "Error saving transaction", e)
            Result.failure(Exception("Không thể lưu giao dịch: ${e.message}"))
        }
    }

    /**
     * Lấy transaction theo ID
     */
    suspend fun getTransactionById(transactionId: String): Result<Transaction> {
        return try {
            Log.d(TAG, "Getting transaction: $transactionId")

            val snapshot = transactionsCollection
                .document(transactionId)
                .get()
                .await()

            if (!snapshot.exists()) {
                return Result.failure(Exception("Không tìm thấy giao dịch"))
            }

            val transaction = Transaction.fromMap(snapshot.data!!)
            Log.d(TAG, "Transaction found: ${transaction.id}")

            Result.success(transaction)

        } catch (e: Exception) {
            Log.e(TAG, "Error getting transaction", e)
            Result.failure(Exception("Không thể lấy thông tin giao dịch: ${e.message}"))
        }
    }

    /**
     * Lấy lịch sử giao dịch theo account ID
     */
    suspend fun getTransactionHistory(
        accountId: String,
        limit: Int = 50
    ): Result<List<Transaction>> {
        return try {
            Log.d(TAG, "Getting transaction history for account: $accountId")

            // Query transactions where fromAccountId = accountId OR toAccountId = accountId
            val fromSnapshot = transactionsCollection
                .whereEqualTo("fromAccountId", accountId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val toSnapshot = transactionsCollection
                .whereEqualTo("toAccountId", accountId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            // Combine và sort
            val transactions = mutableListOf<Transaction>()

            fromSnapshot.documents.forEach { doc ->
                transactions.add(Transaction.fromMap(doc.data!!))
            }

            toSnapshot.documents.forEach { doc ->
                val txn = Transaction.fromMap(doc.data!!)
                // Tránh duplicate
                if (!transactions.any { it.id == txn.id }) {
                    transactions.add(txn)
                }
            }

            // Sort by timestamp descending
            transactions.sortByDescending { it.timestamp.seconds }

            Log.d(TAG, "Found ${transactions.size} transactions")
            Result.success(transactions.take(limit))

        } catch (e: Exception) {
            Log.e(TAG, "Error getting transaction history", e)
            Result.failure(Exception("Không thể lấy lịch sử giao dịch: ${e.message}"))
        }
    }

    /**
     * Cập nhật trạng thái transaction
     */
    suspend fun updateTransactionStatus(
        transactionId: String,
        status: TransactionStatus,
        errorMessage: String? = null
    ): Result<Boolean> {
        return try {
            Log.d(TAG, "Updating transaction status: $transactionId -> $status")

            val updates = mutableMapOf<String, Any>(
                "status" to status.name
            )

            if (errorMessage != null) {
                updates["errorMessage"] = errorMessage
            }

            transactionsCollection
                .document(transactionId)
                .update(updates)
                .await()

            Log.d(TAG, "Transaction status updated successfully")
            Result.success(true)

        } catch (e: Exception) {
            Log.e(TAG, "Error updating transaction status", e)
            Result.failure(Exception("Không thể cập nhật trạng thái: ${e.message}"))
        }
    }

    /**
     * Cập nhật balance sau giao dịch
     */
    suspend fun updateBalanceAfter(
        transactionId: String,
        balanceAfter: Double
    ): Result<Boolean> {
        return try {
            Log.d(TAG, "Updating balance after for transaction: $transactionId")

            transactionsCollection
                .document(transactionId)
                .update("balanceAfter", balanceAfter)
                .await()

            Result.success(true)

        } catch (e: Exception) {
            Log.e(TAG, "Error updating balance", e)
            Result.failure(Exception("Không thể cập nhật số dư: ${e.message}"))
        }
    }

    /**
     * Lấy transactions theo type
     */
    suspend fun getTransactionsByType(
        accountId: String,
        type: TransactionType,
        limit: Int = 20
    ): Result<List<Transaction>> {
        return try {
            Log.d(TAG, "Getting transactions by type: $type")

            val snapshot = transactionsCollection
                .whereEqualTo("fromAccountId", accountId)
                .whereEqualTo("transactionType", type.name)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val transactions = snapshot.documents.map { doc ->
                Transaction.fromMap(doc.data!!)
            }

            Log.d(TAG, "Found ${transactions.size} transactions")
            Result.success(transactions)

        } catch (e: Exception) {
            Log.e(TAG, "Error getting transactions by type", e)
            Result.failure(Exception("Không thể lấy giao dịch: ${e.message}"))
        }
    }

    /**
     * Lấy transactions theo status
     */
    suspend fun getTransactionsByStatus(
        accountId: String,
        status: TransactionStatus,
        limit: Int = 20
    ): Result<List<Transaction>> {
        return try {
            Log.d(TAG, "Getting transactions by status: $status")

            val snapshot = transactionsCollection
                .whereEqualTo("fromAccountId", accountId)
                .whereEqualTo("status", status.name)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(limit.toLong())
                .get()
                .await()

            val transactions = snapshot.documents.map { doc ->
                Transaction.fromMap(doc.data!!)
            }

            Log.d(TAG, "Found ${transactions.size} transactions")
            Result.success(transactions)

        } catch (e: Exception) {
            Log.e(TAG, "Error getting transactions by status", e)
            Result.failure(Exception("Không thể lấy giao dịch: ${e.message}"))
        }
    }

    /**
     * Lấy transactions trong khoảng thời gian
     */
    suspend fun getTransactionsByDateRange(
        accountId: String,
        startDate: Timestamp,
        endDate: Timestamp
    ): Result<List<Transaction>> {
        return try {
            Log.d(TAG, "Getting transactions by date range")

            val snapshot = transactionsCollection
                .whereEqualTo("fromAccountId", accountId)
                .whereGreaterThanOrEqualTo("timestamp", startDate)
                .whereLessThanOrEqualTo("timestamp", endDate)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()

            val transactions = snapshot.documents.map { doc ->
                Transaction.fromMap(doc.data!!)
            }

            Log.d(TAG, "Found ${transactions.size} transactions in date range")
            Result.success(transactions)

        } catch (e: Exception) {
            Log.e(TAG, "Error getting transactions by date range", e)
            Result.failure(Exception("Không thể lấy giao dịch: ${e.message}"))
        }
    }

    /**
     * Tính tổng số tiền giao dịch trong ngày
     */
    suspend fun getDailyTransactionAmount(accountId: String): Result<Double> {
        return try {
            // Get start of today
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = Timestamp(calendar.time)

            val snapshot = transactionsCollection
                .whereEqualTo("fromAccountId", accountId)
                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .get()
                .await()

            val totalAmount = snapshot.documents
                .map { Transaction.fromMap(it.data!!) }
                .filter { it.status == TransactionStatus.COMPLETED }
                .sumOf { it.amount }

            Log.d(TAG, "Daily transaction amount: $totalAmount")
            Result.success(totalAmount)

        } catch (e: Exception) {
            Log.e(TAG, "Error calculating daily amount", e)
            Result.failure(Exception("Không thể tính tổng giao dịch: ${e.message}"))
        }
    }

    /**
     * Đếm số lượng giao dịch trong ngày
     */
    suspend fun getDailyTransactionCount(accountId: String): Result<Int> {
        return try {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val startOfDay = Timestamp(calendar.time)

            val snapshot = transactionsCollection
                .whereEqualTo("fromAccountId", accountId)
                .whereGreaterThanOrEqualTo("timestamp", startOfDay)
                .get()
                .await()

            val count = snapshot.size()
            Log.d(TAG, "Daily transaction count: $count")

            Result.success(count)

        } catch (e: Exception) {
            Log.e(TAG, "Error counting daily transactions", e)
            Result.failure(Exception("Không thể đếm giao dịch: ${e.message}"))
        }
    }

    /**
     * Xóa transaction (chỉ dùng cho testing)
     */
    suspend fun deleteTransaction(transactionId: String): Result<Boolean> {
        return try {
            Log.d(TAG, "Deleting transaction: $transactionId")

            transactionsCollection
                .document(transactionId)
                .delete()
                .await()

            Log.d(TAG, "Transaction deleted successfully")
            Result.success(true)

        } catch (e: Exception) {
            Log.e(TAG, "Error deleting transaction", e)
            Result.failure(Exception("Không thể xóa giao dịch: ${e.message}"))
        }
    }
}