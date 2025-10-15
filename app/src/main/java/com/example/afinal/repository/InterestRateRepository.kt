package com.example.afinal.repository

import com.example.afinal.data.interest.InterestRate
import com.example.afinal.data.interest.InterestRateDao

class InterestRateRepository(private val dao: InterestRateDao) {

    suspend fun getAll(): List<InterestRate> = dao.getAll()

    suspend fun upsert(termMonths: Int, rate: Double) {
        val record = InterestRate(termMonths = termMonths, rate = rate)
        dao.upsert(record)
    }
}
