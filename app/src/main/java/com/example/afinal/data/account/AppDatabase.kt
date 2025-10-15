package com.example.afinal.data.account

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.afinal.data.savings.SavingsAccount
import com.example.afinal.data.savings.SavingsAccountDao
import com.example.afinal.data.interest.InterestRate
import com.example.afinal.data.interest.InterestRateDao

/**
 * AppDatabase — chứa toàn bộ bảng và DAO.
 * Dùng singleton để đảm bảo chỉ có 1 instance trong toàn app.
 */
@Database(
    entities = [
        Account::class,
        SavingsAccount::class,
        InterestRate::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun accountDao(): AccountDao
    abstract fun savingsAccountDao(): SavingsAccountDao
    abstract fun interestRateDao(): InterestRateDao

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
