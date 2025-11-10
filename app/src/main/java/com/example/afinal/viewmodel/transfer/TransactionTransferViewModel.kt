package com.example.afinal.viewmodel.transfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.model.Transaction
import com.example.afinal.data.repository.TransactionTransferRepository
import com.example.afinal.data.service.StripePaymentService
import kotlinx.coroutines.launch
import kotlin.onFailure
import kotlin.onSuccess

/**
 * ViewModel xử lý Transaction UI Logic
 * Member 3: Transaction & Payment - Week 2
 * Áp dụng MVVM Architecture
 */
class TransactionTransferViewModel : ViewModel() {

    private val stripeService = StripePaymentService()
    private val transactionRepo = TransactionTransferRepository()

    // LiveData cho UI
    private val _transactionStatus = MutableLiveData<TransactionResult>()
    val transactionStatus: LiveData<TransactionResult> = _transactionStatus

    private val _transactionHistory = MutableLiveData<List<Transaction>>()
    val transactionHistory: LiveData<List<Transaction>> = _transactionHistory

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Xử lý Deposit (Nạp tiền)
     */
    fun deposit(accountId: String, amount: Double) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Validate amount
                if (amount < 5000) {
                    _transactionStatus.value = TransactionResult.Error(
                        "Số tiền tối thiểu là 5,000 VND"
                    )
                    return@launch
                }

                // Process deposit
                val result = stripeService.processDeposit(
                    accountId = accountId,
                    amount = amount,
                    description = "Nạp tiền vào tài khoản"
                )

                result.onSuccess { transaction ->
                    _transactionStatus.value = TransactionResult.Success(
                        transaction,
                        "Nạp tiền thành công"
                    )
                    // Refresh transaction history
                    loadTransactionHistory(accountId)
                }

                result.onFailure { error ->
                    _transactionStatus.value = TransactionResult.Error(
                        error.message ?: "Có lỗi xảy ra"
                    )
                }
            } catch (e: Exception) {
                _transactionStatus.value = TransactionResult.Error(
                    e.message ?: "Có lỗi xảy ra"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Xử lý Internal Transfer (Chuyển tiền nội bộ)
     */
    fun transferInternal(
        fromAccountId: String,
        toAccountId: String,
        amount: Double
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Validate
                if (fromAccountId == toAccountId) {
                    _transactionStatus.value = TransactionResult.Error(
                        "Không thể chuyển tiền cho chính mình"
                    )
                    return@launch
                }

                if (amount < 1000) {
                    _transactionStatus.value = TransactionResult.Error(
                        "Số tiền tối thiểu là 1,000 VND"
                    )
                    return@launch
                }

                // Process transfer
                val result = stripeService.processInternalTransfer(
                    fromAccountId = fromAccountId,
                    toAccountId = toAccountId,
                    amount = amount,
                    description = "Chuyển tiền nội bộ"
                )

                result.onSuccess { transaction ->
                    _transactionStatus.value = TransactionResult.Success(
                        transaction,
                        "Chuyển tiền thành công"
                    )
                    loadTransactionHistory(fromAccountId)
                }

                result.onFailure { error ->
                    _transactionStatus.value = TransactionResult.Error(
                        error.message ?: "Chuyển tiền thất bại"
                    )
                }
            } catch (e: Exception) {
                _transactionStatus.value = TransactionResult.Error(
                    e.message ?: "Có lỗi xảy ra"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Load lịch sử giao dịch
     */
    fun loadTransactionHistory(accountId: String, limit: Int = 50) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val result = transactionRepo.getTransactionHistory(accountId, limit)

                result.onSuccess { transactions ->
                    _transactionHistory.value = transactions
                }

                result.onFailure { error ->
                    _transactionStatus.value = TransactionResult.Error(
                        "Không thể tải lịch sử: ${error.message}"
                    )
                }
            } catch (e: Exception) {
                _transactionStatus.value = TransactionResult.Error(
                    "Có lỗi xảy ra khi tải lịch sử"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Kiểm tra trạng thái Payment Intent
     */
    fun checkPaymentStatus(paymentIntentId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val result = stripeService.getPaymentIntentStatus(paymentIntentId)

                result.onSuccess { status ->
                    _transactionStatus.value = TransactionResult.PaymentStatus(status)
                }

                result.onFailure { error ->
                    _transactionStatus.value = TransactionResult.Error(
                        error.message ?: "Không thể kiểm tra trạng thái"
                    )
                }
            } catch (e: Exception) {
                _transactionStatus.value = TransactionResult.Error(
                    "Có lỗi xảy ra"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Reset transaction status
     */
    fun resetStatus() {
        _transactionStatus.value = TransactionResult.Idle
    }
}

/**
 * Sealed class cho Transaction Result
 */
sealed class TransactionResult {
    object Idle : TransactionResult()
    data class Success(val transaction: Transaction, val message: String) : TransactionResult()
    data class Error(val message: String) : TransactionResult()
    data class PaymentStatus(val status: String) : TransactionResult()
}