package com.example.afinal.data.bill

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import com.example.afinal.data.sync.*
import com.example.afinal.data.bill.BillPaymentEntity
/**
 * DAO cho bill payments
 */
@Dao
interface BillPaymentDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(payment: BillPaymentEntity)

    @Query("SELECT * FROM bill_payments WHERE accountId = :accountId ORDER BY timestamp DESC")
    fun getPaymentsByAccount(accountId: String): Flow<List<BillPaymentEntity>>

    @Query("SELECT * FROM bill_payments WHERE billType = :billType AND accountId = :accountId ORDER BY timestamp DESC LIMIT 10")
    fun getRecentPaymentsByType(accountId: String, billType: String): Flow<List<BillPaymentEntity>>

    @Query("SELECT * FROM bill_payments WHERE id = :paymentId LIMIT 1")
    suspend fun getPaymentById(paymentId: String): BillPaymentEntity?

    @Query("DELETE FROM bill_payments")
    suspend fun clearAll()
}

