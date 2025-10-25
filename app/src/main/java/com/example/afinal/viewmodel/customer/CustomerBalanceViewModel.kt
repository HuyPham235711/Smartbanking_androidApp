package com.example.afinal.viewmodel.customer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.database.AppDatabase
import com.example.afinal.data.savings.SavingsAccount
import com.example.afinal.data.savings.SavingRepository
import com.example.afinal.utils.InterestCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CustomerBalanceViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getDatabase(app).savingsAccountDao()
    private val repo = SavingRepository(dao)

    private val _accounts = MutableStateFlow<List<SavingsAccount>>(emptyList())
    val accounts: StateFlow<List<SavingsAccount>> = _accounts

    /**
     * 🔹 Load tất cả sổ tiết kiệm của 1 account (UUID)
     */
    fun load(accountId: String) {
        viewModelScope.launch {
            _accounts.value = repo.getByAccount(accountId)
        }
    }

    /**
     * 🔹 Tổng tiền gửi
     */
    fun totalBalance(): Double =
        _accounts.value.sumOf { it.balance }

    /**
     * 🔹 Tổng tiền lãi dự kiến
     */
    fun totalProjectedInterest(): Double =
        _accounts.value.sumOf {
            InterestCalculator.simpleAnnual(
                it.balance,
                it.interestRate,
                it.termMonths
            )
        }
}
