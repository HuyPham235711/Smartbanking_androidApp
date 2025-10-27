package com.example.afinal.viewmodel.officer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.mortgage.MortgageAccountEntity
import com.example.afinal.data.mortgage.MortgageRepository
import com.example.afinal.data.mortgage.MortgageScheduleEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OfficerMortgageViewModel(
    private val repository: MortgageRepository
) : ViewModel() {

    private val _mortgages = MutableStateFlow<List<MortgageAccountEntity>>(emptyList())
    val mortgages: StateFlow<List<MortgageAccountEntity>> = _mortgages

    private val _schedules = MutableStateFlow<List<MortgageScheduleEntity>>(emptyList())
    val schedules: StateFlow<List<MortgageScheduleEntity>> = _schedules

    // 🔹 State hiển thị dialog tạo khoản vay
    private val _showDialog = MutableStateFlow(false)
    val showDialog = _showDialog.asStateFlow()

    /** 🔹 Load toàn bộ khoản vay */
    fun loadAllMortgages() {
        viewModelScope.launch {
            _mortgages.value = repository.getAllAccounts()
        }
    }

    /** 🔹 Load lịch trả nợ theo mortgageId */
    fun loadSchedule(mortgageId: String) {
        viewModelScope.launch {
            println("🔹 loadSchedule($mortgageId)")
            _schedules.value = repository.getSchedulesByMortgage(mortgageId)
        }
    }


    fun markAsPaid(scheduleId: String, mortgageId: String) {
        viewModelScope.launch {
            repository.markScheduleAsPaid(scheduleId)
            loadSchedule(mortgageId)
        }
    }

    // ✅ Hàm mở / đóng dialog
    fun openDialog() {
        _showDialog.value = true
    }

    fun closeDialog() {
        _showDialog.value = false
    }

    suspend fun addMortgage(
        accountName: String,
        principal: Double,
        annualRate: Double,
        termMonths: Int,
        ownerAccountId: String
    ): String {
        val startDate = System.currentTimeMillis()
        val account = MortgageAccountEntity(
            accountName = accountName,
            principal = principal,
            annualInterestRate = annualRate,
            termMonths = termMonths,
            startDate = startDate,
            remainingBalance = principal,
            status = "ACTIVE",
            ownerAccountId = ownerAccountId
        )
        return repository.insertAccountWithSchedule(account)
    }
}


