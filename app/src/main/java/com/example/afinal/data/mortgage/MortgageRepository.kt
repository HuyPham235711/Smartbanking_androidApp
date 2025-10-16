package com.example.afinal.data.mortgage

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class MortgageRepository(
    private val accountDao: MortgageAccountDao,
    private val scheduleDao: MortgageScheduleDao
) {
    // Thêm khoản vay mới
    suspend fun insertAccount(account: MortgageAccountEntity): Long = withContext(Dispatchers.IO) {
        accountDao.insert(account)
    }

    // Thêm danh sách lịch trả nợ
    suspend fun insertSchedules(schedules: List<MortgageScheduleEntity>) = withContext(Dispatchers.IO) {
        scheduleDao.insertAll(schedules)
    }

    // Lấy toàn bộ khoản vay
    suspend fun getAllAccounts(): List<MortgageAccountEntity> = withContext(Dispatchers.IO) {
        accountDao.getAll()
    }

    // Lấy lịch trả nợ của 1 khoản vay
    suspend fun getSchedulesByMortgage(mortgageId: Long): List<MortgageScheduleEntity> = withContext(Dispatchers.IO) {
        scheduleDao.getByMortgage(mortgageId)
    }
}
