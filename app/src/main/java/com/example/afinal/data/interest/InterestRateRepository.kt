package com.example.afinal.data.interest

import com.example.afinal.data.sync.*
import com.example.afinal.data.sync.SyncMapper.toDTO
import com.example.afinal.data.sync.SyncMapper.toEntity
import com.example.afinal.data.sync.SyncMapper.toInterestDTO
import com.example.afinal.data.sync.SyncMapper.toMap
import kotlinx.coroutines.flow.Flow
import com.example.afinal.data.sync.SyncableRepository
import kotlinx.coroutines.flow.emptyFlow

/**
 * Repository quản lý bảng lãi suất (interest_rate)
 * Cho phép Officer CRUD và đồng bộ 2 chiều với Firestore.
 */
class InterestRateRepository(
    private val dao: InterestRateDao
) : SyncableRepository<InterestRate> {

    // Firestore collection: "interest"
    private val firebaseSync = FirebaseSyncService(SyncConfig.Collections.INTEREST)

    // =============================
    // 🔸 SYNCABLE REPOSITORY
    // =============================

    override suspend fun pushLocalChange(entity: InterestRate) {
        val dto = entity.toDTO()
        firebaseSync.upsert(dto.termMonths.toString(), dto.toMap())
    }

    override fun listenRemoteChanges(): Flow<List<InterestRate>> {
        return firebaseSync.listenCollection { it.toInterestDTO().toEntity() }
    }

    // =============================
    // 🔸 ROOM OPERATIONS
    // =============================

    suspend fun getAll(): List<InterestRate> = dao.getAll()

    suspend fun upsert(termMonths: Int, rate: Double, isRemote: Boolean = false) {
        val interest = InterestRate(termMonths, rate)
        dao.upsert(interest)
        if (!isRemote) pushLocalChange(interest)
    }

    suspend fun update(rate: InterestRate, isRemote: Boolean = false) {
        dao.update(rate)
        if (!isRemote) pushLocalChange(rate)
    }
}
