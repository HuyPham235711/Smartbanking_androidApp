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
// 1. THÊM CÁC IMPORT NÀY
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlinx.coroutines.tasks.await

/**
 * Repository chịu trách nhiệm gọi DAO và xử lý logic trung gian.
 * UI (ViewModel) chỉ làm việc với Repository, không gọi DAO trực tiếp.
 */
class AccountRepository(private val accountDao: AccountDao) :
    SyncableRepository<Account> {

    private val firebaseSync = FirebaseSyncService(SyncConfig.Collections.ACCOUNTS)

    // 2. THÊM BIẾN NÀY
    private val firestoreDb = Firebase.firestore

    // -----------------------------
    // 🔸 Room Local Operations
    // -----------------------------
    suspend fun getAllAccounts() = accountDao.getAllAccounts()

    fun observeAllAccounts(): Flow<List<Account>> = accountDao.observeAll()

    suspend fun getAccountById(id: String) = accountDao.getAccountById(id)

    // Hàm này đọc từ ROOM (dữ liệu cục bộ)
    suspend fun getAccountByEmail(email: String) = accountDao.getAccountByEmail(email)

    /**
     * 3. THÊM HÀM MỚI NÀY
     * Hàm này đọc TRỰC TIẾP TỪ FIRESTORE (dữ liệu mới nhất)
     */
    suspend fun getAccountByEmailFromFirestore(email: String): Account? {
        return try {
            val snapshot = firestoreDb.collection(SyncConfig.Collections.ACCOUNTS)
                .whereEqualTo("email", email)
                .limit(1)
                .get()
                .await()

            if (snapshot.isEmpty) {
                null // Không tìm thấy
            } else {
                // Chuyển đổi Map<String, Any> sang AccountDTO rồi sang Account
                snapshot.documents.first().data?.toAccountDTO()?.toEntity()
            }
        } catch (e: Exception) {
            println("❌ Lỗi khi getAccountByEmailFromFirestore: ${e.message}")
            null
        }
    }


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


    suspend fun syncAccountsFromFirebase() {
        try {
            println("☁️ Syncing accounts from Firebase...")

            // Lấy toàn bộ accounts từ Firestore
            val remoteAccounts = firebaseSync.getAllOnce { it.toAccountDTO().toEntity() }

            println("📥 Fetched ${remoteAccounts.size} accounts from Firebase")

            // Insert/Update vào Room (với flag isRemote để không push lại)
            remoteAccounts.forEach { account ->
                accountDao.insertAccount(account)
            }

            println("✅ Synced ${remoteAccounts.size} accounts to local database")

        } catch (e: Exception) {
            println("❌ Failed to sync accounts from Firebase: ${e.message}")
            throw e
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
