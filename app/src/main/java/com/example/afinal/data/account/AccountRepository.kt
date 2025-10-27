package com.example.afinal.data.account

import com.example.afinal.data.sync.*
import com.example.afinal.data.sync.SyncMapper.toAccountDTO
import com.example.afinal.data.sync.SyncMapper.toDTO
import com.example.afinal.data.sync.SyncMapper.toEntity
import com.example.afinal.data.sync.SyncMapper.toMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filter

/**
 * Repository chịu trách nhiệm gọi DAO và xử lý logic trung gian.
 * UI (ViewModel) chỉ làm việc với Repository, không gọi DAO trực tiếp.
 */
class AccountRepository(private val accountDao: AccountDao) :
    SyncableRepository<Account> {

    private val firebaseSync = FirebaseSyncService(SyncConfig.Collections.ACCOUNTS)

    // -----------------------------
    // 🔸 Room Local Operations
    // -----------------------------
    suspend fun getAllAccounts() = accountDao.getAllAccounts()

    fun observeAllAccounts(): Flow<List<Account>> = accountDao.observeAll()

    suspend fun getAccountById(id: String) = accountDao.getAccountById(id)

    suspend fun insertAccount(account: Account, isRemote: Boolean = false) {
        accountDao.insertAccount(account)
        if (!isRemote) {
            pushLocalChange(account)
        }
    }


    suspend fun updateAccount(account: Account) {
        accountDao.updateAccount(account)
        pushLocalChange(account)
    }


    suspend fun deleteAccount(account: Account, isRemote: Boolean = false) {
        accountDao.deleteAccount(account)

        // ✅ Chỉ push lên Firestore nếu delete local
        if (!isRemote) {
            firebaseSync.delete(account.id)
            println("🗑️ Deleted account ${account.id} from Firestore")
        }
    }


    suspend fun getFirstAccountId(): String? = accountDao.getFirstAccountId()

    // -----------------------------
    // 🔸 Firebase Sync Interface
    // -----------------------------
    override suspend fun pushLocalChange(entity: Account) {
        val dto = entity.toDTO()
        firebaseSync.upsert(entity.id, dto.toMap())   // vì giờ id đã là String UUID
    }

    override fun listenRemoteChanges(): Flow<List<Account>> {
        return firebaseSync.listenCollection { it.toAccountDTO().toEntity() }
            .filter { list ->  // ⚠️ chỉ emit nếu không rỗng
                val shouldEmit = list.isNotEmpty()
                if (!shouldEmit) println("⚠️ Skip empty Firestore snapshot for accounts")
                shouldEmit
            }
    }

    suspend fun getAllAccountsOnce(): List<Account> {
        return accountDao.getAllAccounts()
    }



}
