package com.example.afinal.ui.mortgage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.mortgage.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.pow
import kotlin.math.roundToLong

class MortgageViewModel(
    private val repository: MortgageRepository
) : ViewModel() {

    private val _accounts = MutableStateFlow<List<MortgageAccountEntity>>(emptyList())
    val accounts: StateFlow<List<MortgageAccountEntity>> = _accounts

    private val _schedule = MutableStateFlow<List<MortgageScheduleEntity>>(emptyList())
    val schedule: StateFlow<List<MortgageScheduleEntity>> = _schedule

    /** Nạp toàn bộ tài khoản vay */
    fun loadAccounts() {
        viewModelScope.launch {
            _accounts.value = repository.getAllAccounts()
        }
    }

    /** Nạp lịch trả nợ cho một khoản vay */
    fun loadSchedule(mortgageId: Long) {
        viewModelScope.launch {
            _schedule.value = repository.getSchedulesByMortgage(mortgageId)
        }
    }

    /** Thêm khoản vay mới + tính tự động lịch trả nợ */
    fun addMortgage(accountName: String, principal: Long, annualRate: Double, termMonths: Int) {
        viewModelScope.launch {
            val start = Calendar.getInstance().timeInMillis
            val account = MortgageAccountEntity(
                accountName = accountName,
                principal = principal,
                annualInterestRate = annualRate,
                termMonths = termMonths,
                startDate = start
            )
            val mortgageId = repository.insertAccount(account)
            val schedules = calculateSchedule(mortgageId, principal, annualRate, termMonths, start)
            repository.insertSchedules(schedules)
            loadAccounts()
        }
    }

    /** Công thức tính trả góp đều */
    private fun calculateSchedule(
        mortgageId: Long,
        principal: Long,
        annualRate: Double,
        termMonths: Int,
        startDate: Long
    ): List<MortgageScheduleEntity> {
        val monthlyRate = annualRate / 12 / 100.0
        val monthlyPayment = (principal * monthlyRate * (1 + monthlyRate).pow(termMonths)) /
                ((1 + monthlyRate).pow(termMonths) - 1)
        val cal = Calendar.getInstance().apply { timeInMillis = startDate }
        var remaining = principal.toDouble()
        val list = mutableListOf<MortgageScheduleEntity>()

        for (i in 1..termMonths) {
            val interest = remaining * monthlyRate
            val principalPart = monthlyPayment - interest
            remaining -= principalPart

            cal.add(Calendar.MONTH, 1)
            list.add(
                MortgageScheduleEntity(
                    mortgageId = mortgageId,
                    period = i,
                    dueDate = cal.timeInMillis,
                    principalAmount = principalPart.roundToLong(),
                    interestAmount = interest.roundToLong(),
                    totalAmount = monthlyPayment.roundToLong(),
                    status = "PENDING"
                )
            )
        }
        return list
    }
}
