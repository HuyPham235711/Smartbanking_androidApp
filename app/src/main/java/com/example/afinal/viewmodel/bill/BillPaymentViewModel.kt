package com.example.afinal.viewmodel.bill

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.account.AccountRepository
import com.example.afinal.data.bill.*
import com.example.afinal.data.transaction.TransactionEntity
import com.example.afinal.data.transaction.TransactionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel xử lý thanh toán hóa đơn
 */
class BillPaymentViewModel(
    private val billPaymentRepository: BillPaymentRepository,
    private val accountRepository: AccountRepository,
    private val transactionRepository: TransactionRepository
) : ViewModel() {

    private val _currentAccount = MutableStateFlow<com.example.afinal.data.account.Account?>(null)
    val currentAccount: StateFlow<com.example.afinal.data.account.Account?> = _currentAccount.asStateFlow()

    private val _paymentHistory = MutableStateFlow<List<BillPaymentEntity>>(emptyList())
    val paymentHistory: StateFlow<List<BillPaymentEntity>> = _paymentHistory.asStateFlow()

    private val _paymentState = MutableStateFlow<PaymentState>(PaymentState.Idle)
    val paymentState: StateFlow<PaymentState> = _paymentState.asStateFlow()

    private val _billInfo = MutableStateFlow<BillInfo?>(null)
    val billInfo: StateFlow<BillInfo?> = _billInfo.asStateFlow()

    /**
     * Tải thông tin tài khoản và lịch sử thanh toán
     */
    fun loadAccountAndHistory(accountId: String) {
        viewModelScope.launch {
            _currentAccount.value = accountRepository.getAccountById(accountId)
            billPaymentRepository.getPaymentsByAccount(accountId)
                .collect { history ->
                    _paymentHistory.value = history
                }
        }
    }

    /**
     * Tra cứu thông tin hóa đơn (giả lập API)
     */
    fun lookupBill(billType: BillType, customerCode: String, serviceProvider: String) {
        viewModelScope.launch {
            try {
                _paymentState.value = PaymentState.Loading

                // ✅ Giả lập tra cứu hóa đơn (trong thực tế gọi API)
                kotlinx.coroutines.delay(1500) // Simulate network delay

                // Mock data
                val mockBillInfo = when (billType) {
                    BillType.ELECTRIC -> BillInfo(
                        customerCode = customerCode,
                        customerName = "Nguyễn Văn A",
                        address = "123 Nguyễn Huệ, Q.1, TP.HCM",
                        amount = (150000..500000).random().toDouble(),
                        period = "Tháng 11/2025",
                        serviceProvider = serviceProvider
                    )
                    BillType.WATER -> BillInfo(
                        customerCode = customerCode,
                        customerName = "Nguyễn Văn A",
                        address = "123 Nguyễn Huệ, Q.1, TP.HCM",
                        amount = (80000..200000).random().toDouble(),
                        period = "Tháng 11/2025",
                        serviceProvider = serviceProvider
                    )
                    BillType.PHONE_TOPUP -> BillInfo(
                        customerCode = customerCode,
                        customerName = "Số điện thoại: $customerCode",
                        amount = 0.0, // User sẽ nhập số tiền
                        serviceProvider = serviceProvider
                    )
                }

                _billInfo.value = mockBillInfo
                _paymentState.value = PaymentState.BillFound

            } catch (e: Exception) {
                _paymentState.value = PaymentState.Error("Không tìm thấy hóa đơn: ${e.message}")
            }
        }
    }

    /**
     * Thực hiện thanh toán hóa đơn
     */
    fun executeBillPayment(
        accountId: String,
        billType: BillType,
        serviceProvider: String,
        customerCode: String,
        amount: Double,
        billPeriod: String? = null
    ) {
        viewModelScope.launch {
            try {
                _paymentState.value = PaymentState.Processing

                // ✅ Kiểm tra số dư
                val account = accountRepository.getAccountById(accountId)
                if (account == null) {
                    _paymentState.value = PaymentState.Error("Không tìm thấy tài khoản")
                    return@launch
                }

                if (account.balance < amount) {
                    _paymentState.value = PaymentState.Error("Số dư không đủ để thanh toán")
                    return@launch
                }

                // ✅ Trừ tiền trong tài khoản
                val newBalance = account.balance - amount
                accountRepository.updateAccount(account.copy(balance = newBalance))

                // ✅ Tạo bản ghi thanh toán hóa đơn
                val timestamp = System.currentTimeMillis()
                val billPayment = BillPaymentEntity(
                    accountId = accountId,
                    billType = billType.name,
                    serviceProvider = serviceProvider,
                    customerCode = customerCode,
                    amount = amount,
                    timestamp = timestamp,
                    billPeriod = billPeriod,
                    description = "Thanh toán ${billType.displayName}"
                )
                billPaymentRepository.insert(billPayment)

                // ✅ Ghi nhận giao dịch
                val transaction = TransactionEntity(
                    accountId = accountId,
                    amount = -amount,
                    currency = "VND",
                    type = "BILL_PAYMENT",
                    description = "Thanh toán ${billType.displayName} - $serviceProvider",
                    timestamp = timestamp
                )
                transactionRepository.insert(transaction)

                // ✅ Cập nhật state và tài khoản hiện tại
                _currentAccount.value = account.copy(balance = newBalance)
                _paymentState.value = PaymentState.Success

                println("✅ Bill payment completed: $amount VND for ${billType.displayName}")

            } catch (e: Exception) {
                _paymentState.value = PaymentState.Error("Lỗi thanh toán: ${e.message}")
                println("❌ Bill payment failed: ${e.message}")
            }
        }
    }

    /**
     * Reset trạng thái
     */
    fun resetState() {
        _paymentState.value = PaymentState.Idle
        _billInfo.value = null
    }

    /**
     * Lắng nghe thay đổi từ Firestore
     */
    init {
        viewModelScope.launch {
            billPaymentRepository.listenRemoteChanges().collect { remotePayments ->
                remotePayments.forEach { payment ->
                    billPaymentRepository.insert(payment, isRemote = true)
                }
            }
        }
    }
}

/**
 * Sealed class đại diện cho trạng thái thanh toán
 */
sealed class PaymentState {
    data object Idle : PaymentState()
    data object Loading : PaymentState()
    data object BillFound : PaymentState()
    data object Processing : PaymentState()
    data object Success : PaymentState()
    data class Error(val message: String) : PaymentState()
}