package com.example.afinal.viewmodel.officer

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.mortgage.MortgageAccountEntity
import com.example.afinal.data.mortgage.MortgageRepository
import com.example.afinal.data.mortgage.MortgageScheduleEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class OfficerMortgageDetailViewModel(
    private val repository: MortgageRepository
) : ViewModel() {

    private val _account = MutableStateFlow<MortgageAccountEntity?>(null)
    val account: StateFlow<MortgageAccountEntity?> = _account

    private val _schedules = MutableStateFlow<List<MortgageScheduleEntity>>(emptyList())
    val schedules: StateFlow<List<MortgageScheduleEntity>> = _schedules

    fun loadMortgage(mortgageId: String) {
        viewModelScope.launch {
            println("🔹 loadMortgage($mortgageId)")
            _account.value = repository.getAccountById(mortgageId)
            _schedules.value = repository.getSchedulesByMortgage(mortgageId)
        }
    }


    fun markAsPaid(scheduleId: String, mortgageId: String) {
        viewModelScope.launch {
            repository.markScheduleAsPaid(scheduleId)
            _schedules.value = repository.getSchedulesByMortgage(mortgageId)
        }
    }
}
