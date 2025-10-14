package com.example.afinal.data.account

import androidx.room.*

/**
 * DAO (Data Access Object) — cung cấp các hàm CRUD cho bảng Account
 */
@Dao
interface AccountDao {

    // Lấy toàn bộ tài khoản
    @Query("SELECT * FROM accounts")
    suspend fun getAllAccounts(): List<Account>

    // Lấy 1 tài khoản theo id
    @Query("SELECT * FROM accounts WHERE id = :id")
    suspend fun getAccountById(id: Int): Account?

    // Thêm tài khoản mới
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAccount(account: Account)

    // Cập nhật tài khoản
    @Update
    suspend fun updateAccount(account: Account)

    // Xóa tài khoản
    @Delete
    suspend fun deleteAccount(account: Account)
}
