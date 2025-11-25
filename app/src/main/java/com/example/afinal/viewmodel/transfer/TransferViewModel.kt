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
 * Hỗ trợ tìm kiếm người nhận bằng ID.
 */
class TransferViewModel(
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _currentAccount = MutableStateFlow<Account?>(null)
    val currentAccount: StateFlow<Account?> = _currentAccount.asStateFlow()

    private val _recipientSearchState = MutableStateFlow<RecipientSearchState>(RecipientSearchState.Idle)
    val recipientSearchState: StateFlow<RecipientSearchState> = _recipientSearchState.asStateFlow()

    private val _transferState = MutableStateFlow<TransferState>(TransferState.Idle)
    val transferState: StateFlow<TransferState> = _transferState.asStateFlow()

    /**
     * Tải thông tin tài khoản hiện tại
     */
    fun loadCurrentAccount(currentAccountId: String) {
        viewModelScope.launch {
            try {
                _currentAccount.value = accountRepository.getAccountById(currentAccountId)
                println("✅ Loaded current account: ${_currentAccount.value?.fullName}")
            } catch (e: Exception) {
                println("❌ Failed to load current account: ${e.message}")
            }
        }
    }

    /**
     * Tìm kiếm người nhận bằng ID
     */
    fun searchRecipient(recipientId: String, currentAccountId: String) {
        viewModelScope.launch {
            try {
                _recipientSearchState.value = RecipientSearchState.Loading
                println("🔍 Searching for recipient ID: $recipientId")

                // Kiểm tra không chuyển cho chính mình
                if (recipientId == currentAccountId) {
                    _recipientSearchState.value = RecipientSearchState.SameAccount
                    println("⚠️ Cannot transfer to same account")
                    return@launch
                }

                // Sync từ Firebase để có data mới nhất
                accountRepository.syncAccountsFromFirebase()

                // Tìm account trong Room
                val account = accountRepository.getAccountById(recipientId)

                if (account != null) {
                    _recipientSearchState.value = RecipientSearchState.Found(account)
                    println("✅ Found recipient: ${account.fullName} (${account.id})")
                } else {
                    _recipientSearchState.value = RecipientSearchState.NotFound
                    println("❌ Recipient not found: $recipientId")
                }

            } catch (e: Exception) {
                _recipientSearchState.value = RecipientSearchState.Error("Lỗi tìm kiếm: ${e.message}")
                println("❌ searchRecipient error: ${e.message}")
            }
        }
    }

    /**
     * Reset trạng thái tìm kiếm người nhận
     */
    fun resetRecipientSearch() {
        _recipientSearchState.value = RecipientSearchState.Idle
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
                println("🚀 [TRANSFER] START")
                println("   From: $fromAccountId")
                println("   To: $toAccountId")
                println("   Amount: $amount")

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

                println("✅ [TRANSFER] SUCCESS: $amount VND from ${fromAccount.fullName} to ${toAccount.fullName}")

            } catch (e: Exception) {
                _transferState.value = TransferState.Error("Lỗi chuyển tiền: ${e.message}")
                println("❌ [TRANSFER] FAILED: ${e.message}")
                e.printStackTrace()
            }
        }
    }

    /**
     * Reset trạng thái transfer về Idle
     */
    fun resetTransferState() {
        _transferState.value = TransferState.Idle
    }
}

/**
 * Sealed class đại diện cho trạng thái tìm kiếm người nhận
 */
sealed class RecipientSearchState {
    data object Idle : RecipientSearchState()
    data object Loading : RecipientSearchState()
    data class Found(val account: Account) : RecipientSearchState()
    data object NotFound : RecipientSearchState()
    data object SameAccount : RecipientSearchState()
    data class Error(val message: String) : RecipientSearchState()
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