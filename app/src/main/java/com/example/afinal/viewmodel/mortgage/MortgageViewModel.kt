package com.example.afinal.viewmodel.mortgage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.mortgage.MortgageAccountEntity
import com.example.afinal.data.mortgage.MortgageRepository
import com.example.afinal.data.mortgage.MortgageScheduleEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.Calendar

class MortgageViewModel(
    private val repository: MortgageRepository
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<MortgageAccountEntity>>(emptyList())
    val accounts: StateFlow<List<MortgageAccountEntity>> = _accounts

    private val _schedule = MutableStateFlow<List<MortgageScheduleEntity>>(emptyList())
    val schedule: StateFlow<List<MortgageScheduleEntity>> = _schedule

    // 🧠 Lưu lại ID của user hiện tại để reload dữ liệu
    private var currentOwnerId: String? = null

    /** 🏦 Nạp toàn bộ tài khoản vay (chỉ dành cho officer) */
    fun loadAccounts() {
        viewModelScope.launch {
            _accounts.value = repository.getAllAccounts()
        }
    }

    /** 📅 Nạp lịch trả nợ cho một khoản vay cụ thể */
    fun loadSchedule(mortgageId: String) {
        viewModelScope.launch {
            _schedule.value = repository.getSchedulesByMortgage(mortgageId)
        }
    }

    /** 👤 Nạp danh sách khoản vay thuộc về một user */
    fun loadMortgagesForUser(accountId: String) {
        currentOwnerId = accountId
        viewModelScope.launch {
            _accounts.value = repository.getAccountsByOwner(accountId)
        }
    }

    /** 💰 Đánh dấu kỳ trả nợ là "PAID" rồi reload lại UI */
    fun markSchedulePaid(scheduleId: String, mortgageId: String) {
        viewModelScope.launch {
            repository.markScheduleAsPaid(scheduleId)
            loadSchedule(mortgageId) // reload lại list để UI đổi màu
        }
    }

    /** 🆕 Thêm khoản vay mới — chỉ officer dùng */
    fun addMortgage(
        accountName: String,
        principal: Double,
        annualRate: Double,
        termMonths: Int,
        ownerAccountId: String
    ) {
        viewModelScope.launch {
            val start = Calendar.getInstance().timeInMillis

            val account = MortgageAccountEntity(
                accountName = accountName,
                principal = principal,
                annualInterestRate = annualRate,
                termMonths = termMonths,
                startDate = start,
                remainingBalance = principal,
                status = "ACTIVE",
                ownerAccountId = ownerAccountId
            )

            // ✅ Gọi repository tự insert + generate schedule + sync Firestore
            repository.insertAccountWithSchedule(account)

            // ✅ Reload lại danh sách đúng cho user hiện tại
            loadMortgagesForUser(ownerAccountId)
        }
    }

    init {
        // ❌ Không cần listener Firestore, vì dữ liệu đã sync xuống Room ở MainActivity
        println("🏁 Customer MortgageViewModel initialized (local-only mode, Firestore sync handled at startup).")
    }
}
