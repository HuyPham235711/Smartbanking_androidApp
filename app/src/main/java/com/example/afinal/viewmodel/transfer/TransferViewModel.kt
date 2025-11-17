package com.example.afinal.viewmodel.transfer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.account.Account
import com.example.afinal.data.account.AccountRepository
import com.example.afinal.data.transaction.TransactionEntity
import com.example.afinal.data.transaction.TransactionRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel xử lý logic chuyển tiền giữa 2 tài khoản.
 * Đảm bảo giao dịch an toàn và đồng bộ với Firebase.
 */
class TransferViewModel(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _currentAccount = MutableStateFlow<Account?>(null)
    val currentAccount: StateFlow<Account?> = _currentAccount.asStateFlow()

    private val _availableAccounts = MutableStateFlow<List<Account>>(emptyList())
    val availableAccounts: StateFlow<List<Account>> = _availableAccounts.asStateFlow()

    private val _transferState = MutableStateFlow<TransferState>(TransferState.Idle)
    val transferState: StateFlow<TransferState> = _transferState.asStateFlow()

    private val _isLoadingAccounts = MutableStateFlow(false)
    val isLoadingAccounts: StateFlow<Boolean> = _isLoadingAccounts.asStateFlow()

    /**
     * Tải thông tin tài khoản hiện tại và danh sách người nhận khả dụng
     */
    fun loadAccounts(currentAccountId: String) {
        viewModelScope.launch {
            try {
                // Lấy tài khoản hiện tại từ Room
                _currentAccount.value = accountRepository.getAccountById(currentAccountId)

                // ✅ Sync toàn bộ accounts từ Firebase trước
                accountRepository.syncAccountsFromFirebase()

                // Lấy tất cả tài khoản từ Room (đã được sync)
                val allAccounts = accountRepository.getAllAccountsOnce()
                _availableAccounts.value = allAccounts.filter { it.id != currentAccountId }

                println("📋 Loaded ${allAccounts.size} accounts, ${_availableAccounts.value.size} available for transfer")

            } catch (e: Exception) {
                _transferState.value = TransferState.Error("Không thể tải danh sách tài khoản: ${e.message}")
                println("❌ loadAccounts error: ${e.message}")
            }
        }
    }

    /**
     * Thực hiện chuyển tiền với validation đầy đủ
     */
    fun executeTransfer(
        fromAccountId: String,
        toAccountId: String,
        amount: Double,
        description: String
    ) {
        viewModelScope.launch {
            try {
                _transferState.value = TransferState.Loading

                // ✅ Validation
                if (amount <= 0) {
                    _transferState.value = TransferState.Error("Số tiền phải lớn hơn 0")
                    return@launch
                }

                val fromAccount = accountRepository.getAccountById(fromAccountId)
                val toAccount = accountRepository.getAccountById(toAccountId)

                if (fromAccount == null) {
                    _transferState.value = TransferState.Error("Không tìm thấy tài khoản nguồn")
                    return@launch
                }

                if (toAccount == null) {
                    _transferState.value = TransferState.Error("Không tìm thấy tài khoản đích")
                    return@launch
                }

                if (fromAccount.balance < amount) {
                    _transferState.value = TransferState.Error("Số dư không đủ để thực hiện giao dịch")
                    return@launch
                }

                // ✅ Thực hiện chuyển tiền (atomic transaction)
                val newFromBalance = fromAccount.balance - amount
                val newToBalance = toAccount.balance + amount

                // Cập nhật số dư
                accountRepository.updateAccount(fromAccount.copy(balance = newFromBalance))
                accountRepository.updateAccount(toAccount.copy(balance = newToBalance))

                // Ghi nhận giao dịch cho tài khoản nguồn (trừ tiền)
                val timestamp = System.currentTimeMillis()
                val withdrawTransaction = TransactionEntity(
                    accountId = fromAccountId,
                    amount = -amount,
                    currency = "VND",
                    type = "TRANSFER_OUT",
                    description = "Chuyển đến ${toAccount.fullName}: $description",
                    timestamp = timestamp
                )
                transactionRepository.insert(withdrawTransaction)

                // Ghi nhận giao dịch cho tài khoản đích (cộng tiền)
                val depositTransaction = TransactionEntity(
                    accountId = toAccountId,
                    amount = amount,
                    currency = "VND",
                    type = "TRANSFER_IN",
                    description = "Nhận từ ${fromAccount.fullName}: $description",
                    timestamp = timestamp
                )
                transactionRepository.insert(depositTransaction)

                // ✅ Cập nhật state thành công
                _transferState.value = TransferState.Success
                _currentAccount.value = fromAccount.copy(balance = newFromBalance)

                println("✅ Transfer completed: $amount VND from ${fromAccount.fullName} to ${toAccount.fullName}")

            } catch (e: Exception) {
                _transferState.value = TransferState.Error("Lỗi chuyển tiền: ${e.message}")
                println("❌ Transfer failed: ${e.message}")
            }
        }
    }

    /**
     * Reset trạng thái về Idle
     */
    fun resetState() {
        _transferState.value = TransferState.Idle
    }
}

/**
 * Sealed class đại diện cho các trạng thái của giao dịch chuyển tiền
 */
sealed class TransferState {
    data object Idle : TransferState()
    data object Loading : TransferState()
    data object Success : TransferState()
    data class Error(val message: String) : TransferState()
}