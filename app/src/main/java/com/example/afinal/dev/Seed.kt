//package com.example.afinal.dev
//
//import android.content.Context
//import com.example.afinal.data.database.AppDatabase
//import com.example.afinal.data.interest.InterestRate
//import com.example.afinal.data.savings.SavingsAccount
//
///**
// * Lớp này dùng để khởi tạo dữ liệu mẫu (seed)
// * chỉ nên chạy khi phát triển để test UI.
// */
//object Seed {
//    suspend fun run(context: Context) {
//        val db = AppDatabase.getDatabase(context)
//
//        // --- SEED LÃI SUẤT ---
//        val interestDao = db.interestRateDao()
//        listOf(
//            InterestRate(termMonths = 1, rate = 3.0),
//            InterestRate(termMonths = 3, rate = 3.8),
//            InterestRate(termMonths = 6, rate = 4.5),
//            InterestRate(termMonths = 12, rate = 5.6)
//        ).forEach { interestDao.upsert(it) }
//
//        // --- SEED SỔ TIẾT KIỆM CHO CUSTOMER ID = 1 ---
//        val savingsDao = db.savingsAccountDao()
//        listOf(
//            SavingsAccount(
//                ownerAccountId = 1,
//                balance = 50_000_000.0,
//                interestRate = 4.5,
//                termMonths = 6,
//                openDate = "2025-09-01",
//                maturityDate = "2026-03-01"
//            ),
//            SavingsAccount(
//                ownerAccountId = 1,
//                balance = 120_000_000.0,
//                interestRate = 5.6,
//                termMonths = 12,
//                openDate = "2025-07-15",
//                maturityDate = "2026-07-15"
//            )
//        ).forEach { savingsDao.insert(it) }
//    }
//}
