package com.example.afinal.viewmodel.savings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.savings.SavingRepository
import com.example.afinal.data.savings.SavingsAccount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.UUID

class SavingViewModel(private val repo: SavingRepository) : ViewModel() {
    private val _savings = MutableStateFlow<List<SavingsAccount>>(emptyList())
    val savings = _savings.asStateFlow()

    private val _totalBalance = MutableStateFlow(0.0)
    val totalBalance = _totalBalance.asStateFlow()

    fun loadSavings(accountId: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val list = repo.getByAccount(accountId)
            val total = repo.getTotalBalance(accountId)
            _savings.value = list
            _totalBalance.value = total
        }
    }

    // ✅ thêm sổ tiết kiệm mới
    fun addSaving(accountId: String, amount: Double, ratePercent: Double, termMonths: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            val now = LocalDate.now()
            val maturity = now.plusMonths(termMonths.toLong())
            val fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd")

            val newSaving = SavingsAccount(
                id = UUID.randomUUID().toString(),
                ownerAccountId = accountId,
                balance = amount,
                interestRate = ratePercent, // nhập dạng %, ví dụ 5.5
                termMonths = termMonths,
                openDate = fmt.format(now),
                maturityDate = fmt.format(maturity)
            )

            repo.insert(newSaving)
            val updated = repo.getByAccount(accountId)
            val total = repo.getTotalBalance(accountId)
            _savings.value = updated
            _totalBalance.value = total
        }
    }

    // ✅ Lắng nghe sync Firebase
    init {
        viewModelScope.launch(Dispatchers.IO) {
            repo.listenRemoteChanges().collect { remote ->
                remote.forEach { s ->
                    repo.insert(s, isRemote = true)
                }
            }
        }
    }
}
