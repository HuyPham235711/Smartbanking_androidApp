package com.example.afinal.viewmodel.mortgage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.mortgage.MortgageRepository
import com.example.afinal.data.mortgage.MortgageAccountEntity
import com.example.afinal.data.mortgage.MortgageScheduleEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MortgageDetailViewModel(
    private val repository: MortgageRepository
) : ViewModel() {

    private val _mortgage = MutableStateFlow<MortgageAccountEntity?>(null)
    val mortgage: StateFlow<MortgageAccountEntity?> = _mortgage

    private val _schedule = MutableStateFlow<List<MortgageScheduleEntity>>(emptyList())
    val schedule: StateFlow<List<MortgageScheduleEntity>> = _schedule

    /** 📄 Nạp chi tiết 1 khoản vay + toàn bộ schedule của nó */
    fun loadMortgage(id: String) {
        viewModelScope.launch {
            _mortgage.value = repository.getAccountById(id)
            _schedule.value = repository.getSchedulesByMortgage(id)
        }
    }

    /** 🔄 Nạp lại schedule của một khoản vay cụ thể */
    fun loadSchedule(mortgageId: String) {
        viewModelScope.launch {
            _schedule.value = repository.getSchedulesByMortgage(mortgageId)
        }
    }

    /** 💰 Đánh dấu 1 kỳ là PAID rồi reload lại UI */
    fun markSchedulePaid(scheduleId: String, mortgageId: String) {
        viewModelScope.launch {
            repository.markScheduleAsPaid(scheduleId)
            loadSchedule(mortgageId)
        }
    }

    init {
        // ❌ Không cần listener Firestore — dữ liệu đã sync xuống Room khi app startup
        println("🏁 MortgageDetailViewModel initialized (local-only mode, Firestore sync handled at startup).")
    }
}
