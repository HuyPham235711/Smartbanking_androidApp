package com.example.afinal.data.transaction

import com.example.afinal.data.sync.*
import com.example.afinal.data.sync.SyncMapper.toDTO
import com.example.afinal.data.sync.SyncMapper.toEntity
import com.example.afinal.data.sync.SyncMapper.toMap
import com.example.afinal.data.sync.SyncMapper.toTransactionDTO
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow

/**
 * Repository quản lý lịch sử giao dịch.
 * Đồng bộ 1 chiều Room → Firestore (collection: "transactions").
 */
class TransactionRepository(
    private val dao: TransactionDao
) : SyncableRepository<TransactionEntity> {

    // --- Firestore Sync Service ---
    private val firebaseSync = FirebaseSyncService(SyncConfig.Collections.TRANSACTIONS)

    // =============================
    // 🔸 SYNCABLE REPOSITORY
    // =============================

    override suspend fun pushLocalChange(entity: TransactionEntity) {
        val dto = entity.toDTO()
        try {
            firebaseSync.upsert(dto.id, dto.toMap())
            println("✅ Firestore upsert success [transactions/${dto.id}] (${dto.type})")
        } catch (e: Exception) {
            println("❌ Firestore upsert failed [transactions/${dto.id}]: ${e.message}")
        }
    }


    override fun listenRemoteChanges(): Flow<List<TransactionEntity>> {
        return firebaseSync.listenCollection { it.toTransactionDTO().toEntity() }
            .distinctUntilChanged()  // ✅ tránh emit lại list y hệt
    }


    // =============================
    // 🔸 ROOM OPERATIONS
    // =============================

    /** Thêm 1 giao dịch mới (và auto sync Firestore). */
    suspend fun insert(transaction: TransactionEntity, isRemote: Boolean = false) {
        dao.insert(transaction)
        if (!isRemote) pushLocalChange(transaction)
    }

    /** Lấy danh sách giao dịch theo accountId (Flow realtime từ Room). */
    fun getTransactions(accountId: String): Flow<List<TransactionEntity>> {
        return dao.getTransactionsByAccount(accountId)
    }

    /** Xóa toàn bộ giao dịch trong DB (dành cho seed/reset). */
    suspend fun clearAll() {
        dao.clearAll()
    }
}
