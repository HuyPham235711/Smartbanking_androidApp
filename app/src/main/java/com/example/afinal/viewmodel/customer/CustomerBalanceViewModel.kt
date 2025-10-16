package com.example.afinal.viewmodel.customer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.database.AppDatabase
import com.example.afinal.data.savings.SavingsAccount
import com.example.afinal.repository.SavingsRepository
import com.example.afinal.utils.InterestCalculator
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class CustomerBalanceViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getDatabase(app).savingsAccountDao()
    private val repo = SavingsRepository(dao)

    private val _accounts = MutableStateFlow<List<SavingsAccount>>(emptyList())
    val accounts: StateFlow<List<SavingsAccount>> = _accounts

    fun load(customerId: Int) {
        viewModelScope.launch {
            _accounts.value = repo.getAccountsByClient(customerId)
        }
    }

    fun totalBalance(): Double =
        _accounts.value.sumOf { it.balance }

    fun totalProjectedInterest(): Double =
        _accounts.value.sumOf {
            InterestCalculator.simpleAnnual(
                it.balance,
                it.interestRate,
                it.termMonths
            )
        }
}
