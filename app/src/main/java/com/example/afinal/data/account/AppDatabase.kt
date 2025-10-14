package com.example.afinal.data.account

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

/**
 * AppDatabase — chứa toàn bộ bảng và DAO.
 * Dùng singleton để đảm bảo chỉ có 1 instance trong toàn app.
 */
@Database(
    entities = [Account::class], // Liệt kê các bảng
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Trỏ tới DAO
    abstract fun accountDao(): AccountDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "smartbanking_db" // tên file database
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
