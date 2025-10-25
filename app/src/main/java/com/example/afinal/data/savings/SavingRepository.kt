package com.example.afinal.data.savings

import com.example.afinal.data.sync.*
import com.example.afinal.data.sync.SyncMapper.toDTO
import com.example.afinal.data.sync.SyncMapper.toEntity
import com.example.afinal.data.sync.SyncMapper.toMap
import com.example.afinal.data.sync.SyncMapper.toSavingsDTO
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

class SavingRepository(private val dao: SavingsAccountDao) : SyncableRepository<SavingsAccount> {

    private val firebaseSync = FirebaseSyncService(SyncConfig.Collections.SAVINGS)

    // -----------------------------
    // 🔸 Room Local Operations
    // -----------------------------
    suspend fun getByAccount(accountId: String): List<SavingsAccount> = withContext(Dispatchers.IO) {
        dao.getByAccountId(accountId)
    }

    suspend fun getTotalBalance(accountId: String): Double = withContext(Dispatchers.IO) {
        dao.getTotalBalance(accountId) ?: 0.0
    }

    suspend fun insert(account: SavingsAccount, isRemote: Boolean = false) = withContext(Dispatchers.IO) {
        val existing = dao.getByAccountId(account.ownerAccountId)
            .firstOrNull {
                it.balance == account.balance &&
                        it.termMonths == account.termMonths &&
                        it.openDate == account.openDate
            }

        if (existing == null) {
            dao.insert(account)
            if (!isRemote) pushLocalChange(account)
        }
    }

    suspend fun update(account: SavingsAccount) = withContext(Dispatchers.IO) {
        dao.update(account)
        pushLocalChange(account)
    }

    suspend fun delete(account: SavingsAccount) = withContext(Dispatchers.IO) {
        dao.delete(account)
    }

    // -----------------------------
    // 🔸 Firebase Sync Interface
    // -----------------------------
    override suspend fun pushLocalChange(entity: SavingsAccount) {
        val dto = entity.toDTO()
        firebaseSync.upsert(dto.id.toString(), dto.toMap())
    }

    override fun listenRemoteChanges(): Flow<List<SavingsAccount>> {
        return firebaseSync.listenCollection { it.toSavingsDTO().toEntity() }
    }
}
