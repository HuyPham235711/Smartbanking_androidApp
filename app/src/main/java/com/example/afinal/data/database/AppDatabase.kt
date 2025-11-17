package com.example.afinal.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.afinal.data.account.Account
import com.example.afinal.data.account.AccountDao
import com.example.afinal.data.interest.InterestRate
import com.example.afinal.data.interest.InterestRateDao
import com.example.afinal.data.mortgage.MortgageAccountDao
import com.example.afinal.data.savings.SavingsAccount
import com.example.afinal.data.savings.SavingsAccountDao
import com.example.afinal.data.transaction.TransactionEntity
import com.example.afinal.data.transaction.TransactionDao
import com.example.afinal.data.mortgage.MortgageAccountEntity
import com.example.afinal.data.mortgage.MortgageScheduleDao
import com.example.afinal.data.mortgage.MortgageScheduleEntity
import com.example.afinal.data.bill.BillPaymentEntity
import com.example.afinal.data.bill.BillPaymentDao


/**
 * AppDatabase — chứa toàn bộ bảng và DAO.
 * Dùng singleton để đảm bảo chỉ có 1 instance trong toàn app.
 */
@Database(
    entities = [
        Account::class,
        SavingsAccount::class,
        InterestRate::class,
        TransactionEntity::class,
        MortgageAccountEntity::class,
        MortgageScheduleEntity::class,
        BillPaymentEntity::class
    ],
    version = 14,  // ✅ Tăng version từ 13 lên 14
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun savingsAccountDao(): SavingsAccountDao
    abstract fun interestRateDao(): InterestRateDao

    abstract fun transactionDao(): TransactionDao

    abstract fun mortgageAccountDao(): MortgageAccountDao

    abstract fun mortgageScheduleDao(): MortgageScheduleDao

    abstract fun billPaymentDao(): BillPaymentDao


    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smartbanking_db"
                )
                    .fallbackToDestructiveMigration() // cho dev nhanh
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
