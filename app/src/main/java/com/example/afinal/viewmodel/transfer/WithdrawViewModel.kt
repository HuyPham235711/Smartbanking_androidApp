package com.example.afinal.viewmodel.transfer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.config.StripeConfig
import com.example.afinal.data.model.Transaction
import com.example.afinal.data.service.BankAccountInfo
import com.example.afinal.data.service.WithdrawService
import kotlinx.coroutines.launch

/**
 * ViewModel cho Withdraw (Rút tiền)
 * Member 3: Transaction & Payment - Week 3
 * MVVM Architecture
 */
class WithdrawViewModel : ViewModel() {

    private val withdrawService = WithdrawService()

    private val _withdrawResult = MutableLiveData<WithdrawResult>()
    val withdrawResult: LiveData<WithdrawResult> = _withdrawResult

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    /**
     * Xử lý rút tiền
     */
    fun withdraw(
        accountId: String,
        amount: Double,
        bankAccountInfo: BankAccountInfo,
        description: String? = null
    ) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                // Validate amount
                if (amount < StripeConfig.Limits.MIN_WITHDRAW_VND) {
                    _withdrawResult.value = WithdrawResult.Error(
                        "Số tiền tối thiểu là ${StripeConfig.Limits.MIN_WITHDRAW_VND} VND"
                    )
                    _isLoading.value = false
                    return@launch
                }

                if (amount > StripeConfig.Limits.MAX_WITHDRAW_VND) {
                    _withdrawResult.value = WithdrawResult.Error(
                        "Số tiền tối đa là ${StripeConfig.Limits.MAX_WITHDRAW_VND} VND"
                    )
                    _isLoading.value = false
                    return@launch
                }

                // Validate bank account info
                if (bankAccountInfo.bankName.isEmpty()) {
                    _withdrawResult.value = WithdrawResult.Error(
                        "Tên ngân hàng không được để trống"
                    )
                    _isLoading.value = false
                    return@launch
                }

                if (bankAccountInfo.accountNumber.isEmpty() ||
                    bankAccountInfo.accountNumber.length < 8) {
                    _withdrawResult.value = WithdrawResult.Error(
                        "Số tài khoản không hợp lệ"
                    )
                    _isLoading.value = false
                    return@launch
                }

                if (bankAccountInfo.accountHolder.isEmpty() ||
                    bankAccountInfo.accountHolder.length < 3) {
                    _withdrawResult.value = WithdrawResult.Error(
                        "Tên chủ tài khoản không hợp lệ"
                    )
                    _isLoading.value = false
                    return@launch
                }

                // Process withdraw
                val result = withdrawService.processWithdraw(
                    accountId = accountId,
                    amount = amount,
                    bankAccountInfo = bankAccountInfo,
                    description = description ?: "Rút tiền về ${bankAccountInfo.bankName}"
                )

                result.onSuccess { transaction ->
                    _withdrawResult.value = WithdrawResult.Success(
                        transaction,
                        "Rút tiền thành công! Tiền sẽ về tài khoản trong 1-2 ngày."
                    )
                }

                result.onFailure { error ->
                    _withdrawResult.value = WithdrawResult.Error(
                        error.message ?: "Rút tiền thất bại"
                    )
                }

            } catch (e: Exception) {
                _withdrawResult.value = WithdrawResult.Error(
                    e.message ?: "Có lỗi xảy ra khi rút tiền"
                )
            } finally {
                _isLoading.value = false
            }
        }
    }

    /**
     * Lấy danh sách ngân hàng Việt Nam
     */
    fun getVietnameseBanks() = withdrawService.getVietnameseBanks()

    /**
     * Reset withdraw result
     */
    fun resetResult() {
        _withdrawResult.value = WithdrawResult.Idle
    }
}

/**
 * Sealed class cho Withdraw Result
 */
sealed class WithdrawResult {
    object Idle : WithdrawResult()
    data class Success(val transaction: Transaction, val message: String) : WithdrawResult()
    data class Error(val message: String) : WithdrawResult()
}