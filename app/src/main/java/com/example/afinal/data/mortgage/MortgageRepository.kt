package com.example.afinal.data.mortgage

import com.example.afinal.data.database.AppDatabase
import com.example.afinal.data.sync.FirebaseSyncService
import com.example.afinal.data.sync.SyncConfig
import com.example.afinal.data.sync.SyncMapper.toMap
import com.example.afinal.data.sync.SyncMapper.toMortgageDTO
import com.example.afinal.data.sync.SyncMapper.toEntity
import com.example.afinal.data.sync.SyncMapper.toScheduleDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class MortgageRepository(
    private val accountDao: MortgageAccountDao,
    private val scheduleDao: MortgageScheduleDao
) {

    // ============================================================== //
    // 🔹 FIREBASE SYNC SERVICE
    // ============================================================== //
    private val firebaseAccountSync = FirebaseSyncService(SyncConfig.Collections.MORTGAGES)
    private val firebaseScheduleSync = FirebaseSyncService(SyncConfig.Collections.SCHEDULES)

    // ============================================================== //
    // 🔹 MORTGAGE ACCOUNT LAYER
    // ============================================================== //

    suspend fun getAllAccounts(): List<MortgageAccountEntity> = withContext(Dispatchers.IO) {
        accountDao.getAll()
    }

    suspend fun getAccountById(id: String): MortgageAccountEntity? = withContext(Dispatchers.IO) {
        accountDao.getById(id)
    }

    suspend fun getAccountsByOwner(ownerId: String): List<MortgageAccountEntity> =
        withContext(Dispatchers.IO) {
            accountDao.getByOwner(ownerId)
        }

    // ✅ Thêm khoản vay (chỉ insert 1 lần, không trả về Long)
    suspend fun insertAccount(account: MortgageAccountEntity, isRemote: Boolean = false) = withContext(Dispatchers.IO) {
        accountDao.insert(account)
        if (!isRemote) pushLocalChangeAccount(account)
    }

    suspend fun insertAccountWithSchedule(
        account: MortgageAccountEntity,
        isRemote: Boolean = false
    ): String = withContext(Dispatchers.IO) {
        val id = account.id.ifBlank { java.util.UUID.randomUUID().toString() }
        val accountWithId = account.copy(id = id)

        accountDao.insert(accountWithId)
        if (!isRemote) pushLocalChangeAccount(accountWithId)

        val schedules = generateSchedules(accountWithId)
        scheduleDao.insertAll(schedules)

    // 🔥 Đồng bộ từng schedule lên Firestore
        if (!isRemote) {
            schedules.forEach { pushLocalChangeSchedule(it) }
        }

        println("✅ Created mortgage #$id with ${schedules.size} schedules and synced to Firestore.")
        id

    }


    suspend fun deleteAccount(account: MortgageAccountEntity) = withContext(Dispatchers.IO) {
        accountDao.delete(account)
    }

    suspend fun clearAllData() = withContext(Dispatchers.IO) {
        scheduleDao.clearAll()
        accountDao.clearAll()
        println("🧹 Cleared all mortgage accounts and schedules (local Room only).")
    }

    // 🔹 Cho phép insert schedule từ remote hoặc local
    suspend fun insertSchedule(entity: MortgageScheduleEntity, isRemote: Boolean = false) {
        withContext(Dispatchers.IO) {
            val existing = scheduleDao.getScheduleById(entity.id)
            if (existing == null) {
                scheduleDao.insertAll(listOf(entity))
            } else if (existing.status != "PAID") {
                // chỉ cập nhật nếu local chưa thanh toán
                scheduleDao.insertAll(listOf(entity.copy(status = existing.status)))
            }
            if (!isRemote) pushLocalChangeSchedule(entity)
        }
    }

    // ============================================================== //
    // 🔹 MORTGAGE SCHEDULE LAYER
    // ============================================================== //

    suspend fun getSchedulesByMortgage(mortgageId: String): List<MortgageScheduleEntity> =
        withContext(Dispatchers.IO) {
            val list = scheduleDao.getByMortgage(mortgageId)
            println("🧩 getSchedulesByMortgage($mortgageId) → ${list.size} schedules found")
            list.forEach { s ->
                println("   • period=${s.period}, status=${s.status}, due=${s.dueDate}")
            }
            list
        }

    suspend fun syncSchedulesFromFirestoreOnce() {
        val remote = firebaseScheduleSync.getAllOnce { it.toScheduleDTO().toEntity() }
        println("☁️ Pulled ${remote.size} schedules from Firestore")

        remote.forEach { s ->
            val existing = scheduleDao.getScheduleById(s.id)
            if (existing == null) {
                scheduleDao.insertAll(listOf(s))
            }
        }
    }

    suspend fun syncMortgagesFromFirestoreOnce() {
        val remote = firebaseAccountSync.getAllOnce { it.toMortgageDTO().toEntity() }
        println("☁️ Pulled ${remote.size} mortgages from Firestore")

        remote.forEach { m ->
            val existing = accountDao.getById(m.id)
            if (existing == null) {
                accountDao.insert(m)
            }
        }
    }




    // ✅ Cập nhật trạng thái "PAID" (update chắc chắn + sync Firebase)
    suspend fun markScheduleAsPaid(scheduleId: String) = withContext(Dispatchers.IO) {
        scheduleDao.updateStatus(scheduleId, "PAID")
        println("✅ Updated schedule #$scheduleId → PAID (via updateStatus)")
        val updated = scheduleDao.getScheduleById(scheduleId)
        if (updated != null) pushLocalChangeSchedule(updated)
    }

    // ============================================================== //
    // 🔹 FIREBASE SYNC HELPERS
    // ============================================================== //

    private suspend fun pushLocalChangeAccount(entity: MortgageAccountEntity) {
        val dto = entity.toMortgageDTO()
        firebaseAccountSync.upsert(dto.id, dto.toMap())
    }

    private suspend fun pushLocalChangeSchedule(entity: MortgageScheduleEntity) {
        val dto = entity.toScheduleDTO()
        firebaseScheduleSync.upsert(dto.id, dto.toMap())
        println("☁️ Synced schedule ${dto.id} to Firestore (status=${dto.status})")
    }

    fun listenRemoteChanges(): Flow<List<MortgageAccountEntity>> {
        return firebaseAccountSync.listenCollection { it.toMortgageDTO().toEntity() }
    }

    fun listenRemoteScheduleChanges(): Flow<List<MortgageScheduleEntity>> {
        return firebaseScheduleSync.listenCollection { it.toScheduleDTO().toEntity() }
    }

    // ============================================================== //
    // 🔹 AUTO-GENERATE SCHEDULES
    // ============================================================== //

    private fun generateSchedules(account: MortgageAccountEntity): List<MortgageScheduleEntity> {
        val list = mutableListOf<MortgageScheduleEntity>()
        val monthlyInterestRate = account.annualInterestRate / 100.0 / 12.0
        val monthlyPrincipal = account.principal / account.termMonths
        val start = java.time.Instant.ofEpochMilli(account.startDate)
            .atZone(java.time.ZoneId.systemDefault())
            .toLocalDate()

        var remaining = account.principal
        for (i in 1..account.termMonths) {
            val interest = remaining * monthlyInterestRate
            val total = monthlyPrincipal + interest
            remaining -= monthlyPrincipal

            val dueDate = start.plusMonths(i.toLong())
            val dueMillis = dueDate.atStartOfDay(java.time.ZoneId.systemDefault()).toInstant().toEpochMilli()

            list.add(
                MortgageScheduleEntity(
                    mortgageId = account.id, // UUID
                    period = i,
                    dueDate = dueMillis,
                    principalAmount = monthlyPrincipal,
                    interestAmount = interest,
                    totalAmount = total,
                    status = "PENDING"
                )
            )
        }
        return list
    }
}
