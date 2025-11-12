package com.example.afinal.viewmodel.account

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.afinal.data.account.Account
import com.example.afinal.data.account.AccountRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
// 1. THÊM CÁC IMPORT
import com.example.afinal.data.auth.AuthRepository
import com.example.afinal.data.auth.AuthResult

// 2. SỬA CONSTRUCTOR ĐỂ NHẬN AUTH REPO
class AccountViewModel(
    private val repository: AccountRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    // ✅ Dòng dữ liệu realtime từ Room (Flow → StateFlow)
    val accounts: StateFlow<List<Account>> = repository.observeAllAccounts()
        .distinctUntilChanged()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    /**
     * 3. SỬA LẠI HÀM NÀY
     * Hàm này chỉ dùng để TẠO MỚI (không dùng để update)
     * Nó sẽ tạo Auth user, sau đó dùng UID của user đó làm ID cho Firestore/Room
     */
    fun createAccount(email: String, password: String, accountInfo: Account) {
        viewModelScope.launch {
            authRepository.registerUser(email, password).collect { result ->
                when (result) {
                    is AuthResult.Loading -> {
                        println("Đang tạo tài khoản Auth...")
                    }
                    is AuthResult.Success -> {
                        // Tạo Auth thành công!
                        val authUser = result.user
                        println("🟢 Tạo Auth user thành công: ${authUser.uid}")

                        // Dùng UID làm ID chính
                        val finalAccount = accountInfo.copy(
                            id = authUser.uid,
                            email = authUser.email!!
                        )

                        // Lưu vào Room (sẽ tự sync lên Firestore)
                        repository.insertAccount(finalAccount, isRemote = false)
                        println("🟢 Đã lưu tài khoản vào Firestore/Room: ${finalAccount.id}")
                    }
                    is AuthResult.Error -> {
                        println("❌ Lỗi khi tạo Auth user: ${result.message}")
                        // Cần thêm cơ chế báo lỗi cho Officer
                    }
                }
            }
        }
    }

    // ✅ Cập nhật tài khoản (Hàm này giữ nguyên)
    fun updateAccount(account: Account) {
        viewModelScope.launch {
            repository.updateAccount(account)
            println("🟡 Updated account ${account.username} (${account.id})")
        }
    }

    // ✅ Xoá tài khoản (local + Firestore)
    // ⚠️ LƯU Ý: HÀM NÀY CHỈ XÓA TRONG FIRESTORE/ROOM, CHƯA XÓA TRONG AUTHENTICATION
    // (Xóa Auth user là một hành động nhạy cảm, cần logic phức tạp hơn)
    fun deleteAccount(account: Account) {
        viewModelScope.launch {
            repository.deleteAccount(account)
            println("🗑️ Deleted account ${account.username} (${account.id})")
        }
    }

    // ✅ Lắng nghe thay đổi từ Firestore → chèn vào Room nếu khác biệt
    init {
        viewModelScope.launch {
            repository.listenRemoteChanges()
                .distinctUntilChanged()
                .collect { remoteAccounts ->
                    remoteAccounts.forEach { acc ->
                        repository.insertAccount(acc, isRemote = true)
                    }
                }
        }
    }
}