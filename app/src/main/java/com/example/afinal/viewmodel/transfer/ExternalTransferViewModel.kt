package com.example.afinal.viewmodel.transfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.config.StripeConfig
import com.example.afinal.data.model.Transaction
import com.example.afinal.data.model.RecipientBankInfo  // ← THÊM DÒNG NÀY
import com.example.afinal.data.service.ExternalTransferService
import kotlinx.coroutines.launch

/**
 * ViewModel cho External Transfer
 * Member 3: Transaction & Payment - Week 4
 * MVVM Architecture với OTP Integration
 */
class ExternalTransferViewModel : ViewModel() {

    private val transferService = ExternalTransferService()

    private val _transferResult = MutableLiveData<ExternalTransferResult>()
    val transferResult: LiveData<ExternalTransferResult> = _transferResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Tra cứu tên chủ tài khoản
     */
    fun inquiryAccountName(bankCode: String, accountNumber: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val result = transferService.inquiryAccountName(bankCode, accountNumber)

                result.onSuccess { accountName ->
                    _transferResult.value = ExternalTransferResult.AccountInquiry(accountName)
                }

                result.onFailure { error ->
                    _transferResult.value = ExternalTransferResult.Error(
                        error.message ?: "Không thể tra cứu tài khoản"
                    )
                }

            } catch (e: Exception) {
                _transferResult.value = ExternalTransferResult.Error(
                    e.message ?: "Có lỗi xảy ra"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Xử lý chuyển khoản liên ngân hàng
     */
    fun transferExternal(
        fromAccountId: String,
        recipientInfo: RecipientBankInfo,
        amount: Double,
        otpCode: String,
        description: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Validate amount
                if (amount < StripeConfig.Limits.MIN_TRANSFER_VND) {
                    _transferResult.value = ExternalTransferResult.Error(
                        "Số tiền tối thiểu là ${StripeConfig.Limits.MIN_TRANSFER_VND} VND"
                    )
                    return@launch
                }

                if (amount > StripeConfig.Limits.MAX_TRANSFER_VND) {
                    _transferResult.value = ExternalTransferResult.Error(
                        "Số tiền tối đa là ${StripeConfig.Limits.MAX_TRANSFER_VND} VND"
                    )
                    return@launch
                }

                // Validate OTP
                if (otpCode.length != 6) {
                    _transferResult.value = ExternalTransferResult.Error(
                        "Mã OTP không hợp lệ"
                    )
                    return@launch
                }

                // Process transfer
                val result = transferService.processExternalTransfer(
                    fromAccountId = fromAccountId,
                    recipientInfo = recipientInfo,
                    amount = amount,
                    description = description,
                    otpCode = otpCode
                )

                result.onSuccess { transaction ->
                    _transferResult.value = ExternalTransferResult.Success(
                        transaction,
                        "Chuyển khoản thành công"
                    )
                }

                result.onFailure { error ->
                    _transferResult.value = ExternalTransferResult.Error(
                        error.message ?: "Chuyển khoản thất bại"
                    )
                }

            } catch (e: Exception) {
                _transferResult.value = ExternalTransferResult.Error(
                    e.message ?: "Có lỗi xảy ra"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Kiểm tra trạng thái giao dịch
     */
    fun checkTransferStatus(transactionId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                val result = transferService.checkTransferStatus(transactionId)

                result.onSuccess { status ->
                    _transferResult.value = ExternalTransferResult.StatusCheck(status)
                }

                result.onFailure { error ->
                    _transferResult.value = ExternalTransferResult.Error(
                        error.message ?: "Không thể kiểm tra trạng thái"
                    )
                }

            } catch (e: Exception) {
                _transferResult.value = ExternalTransferResult.Error(
                    e.message ?: "Có lỗi xảy ra"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Simulate OTP sent (for testing without M1 integration)
     * TODO Week 4: Replace with real M1 OTP Service
     */
    fun simulateOtpSent() {
        _transferResult.value = ExternalTransferResult.OtpSent
    }

    /**
     * Reset result
     */
    fun resetResult() {
        _transferResult.value = ExternalTransferResult.Idle
    }
}

/**
 * Sealed class cho External Transfer Result
 */
sealed class ExternalTransferResult {
    object Idle : ExternalTransferResult()
    data class Success(val transaction: Transaction, val message: String) : ExternalTransferResult()
    data class Error(val message: String) : ExternalTransferResult()
    data class AccountInquiry(val accountName: String) : ExternalTransferResult()
    object OtpSent : ExternalTransferResult()
    data class StatusCheck(val status: String) : ExternalTransferResult()
}