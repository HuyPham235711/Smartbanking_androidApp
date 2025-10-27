package com.example.afinal.viewmodel.officer

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.database.AppDatabase
import com.example.afinal.data.interest.InterestRate
import com.example.afinal.data.interest.InterestRateRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class InterestRateViewModel(app: Application) : AndroidViewModel(app) {

    private val dao = AppDatabase.getDatabase(app).interestRateDao()
    private val repo = InterestRateRepository(dao)

    private val _rates = MutableStateFlow<List<InterestRate>>(emptyList())
    val rates: StateFlow<List<InterestRate>> = _rates

    fun load() {
        viewModelScope.launch {
            _rates.value = repo.getAll()
        }
    }

    fun save(termMonths: Int, rate: Double) {
        viewModelScope.launch {
            repo.upsert(termMonths, rate)
            _rates.value = repo.getAll() // reload lại để cập nhật UI
        }
    }

    init {
        viewModelScope.launch {
            repo.listenRemoteChanges().collect { list ->
                list.forEach { rate ->
                    repo.upsert(rate.termMonths, rate.rate, isRemote = true)
                }
                _rates.value = repo.getAll()
            }
        }
    }

}
