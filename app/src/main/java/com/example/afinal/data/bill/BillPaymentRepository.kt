package com.example.afinal.data.bill

import com.example.afinal.data.sync.FirebaseSyncService
import com.example.afinal.data.sync.SyncConfig
import com.example.afinal.data.sync.SyncableRepository
import kotlinx.coroutines.flow.Flow
import com.example.afinal.data.bill.BillPaymentEntity
import com.example.afinal.data.bill.BillPaymentDao
import com.example.afinal.data.sync.dto.BillPaymentDTO
import com.example.afinal.data.sync.SyncMapper
import com.example.afinal.data.sync.SyncMapper.toBillPaymentDTO
import com.example.afinal.data.sync.SyncMapper.toDTO
import com.example.afinal.data.sync.SyncMapper.toEntity
import com.example.afinal.data.sync.SyncMapper.toMap

/**
 * Repository xử lý logic thanh toán hóa đơn
 */
class BillPaymentRepository(
    private val dao: BillPaymentDao
) : SyncableRepository<BillPaymentEntity> {

    private val firebaseSync = FirebaseSyncService(SyncConfig.Collections.BILL_PAYMENTS)

    /**
     * Thêm bản ghi thanh toán mới
     */
    suspend fun insert(payment: BillPaymentEntity, isRemote: Boolean = false) {
        dao.insert(payment)
        if (!isRemote) pushLocalChange(payment)
    }

    /**
     * Lấy lịch sử thanh toán theo tài khoản
     */
    fun getPaymentsByAccount(accountId: String): Flow<List<BillPaymentEntity>> {
        return dao.getPaymentsByAccount(accountId)
    }

    /**
     * Lấy các giao dịch gần đây theo loại hóa đơn
     */
    fun getRecentPaymentsByType(accountId: String, billType: String): Flow<List<BillPaymentEntity>> {
        return dao.getRecentPaymentsByType(accountId, billType)
    }

    /**
     * Đồng bộ lên Firestore
     */
    // Trong BillPaymentRepository.kt
    override suspend fun pushLocalChange(entity: BillPaymentEntity) {
        val dto = entity.toDTO()
        try {
            // ✅ Filter null values trước khi upsert
            val map = dto.toMap().filterValues { it != null } as Map<String, Any>

            firebaseSync.upsert(dto.id, map)
            println("✅ Synced bill payment [${dto.id}] to Firestore")
        } catch (e: Exception) {
            println("❌ Failed to sync bill payment: ${e.message}")
        }
    }

    /**
     * Lắng nghe thay đổi từ Firestore
     */
    override fun listenRemoteChanges(): Flow<List<BillPaymentEntity>> {
        return firebaseSync.listenCollection { it.toBillPaymentDTO().toEntity() }
    }

    /**
     * Xóa toàn bộ (dành cho test)
     */
    suspend fun clearAll() {
        dao.clearAll()
    }
}
