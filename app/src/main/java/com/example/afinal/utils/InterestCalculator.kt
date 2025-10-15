package com.example.afinal.utils

object InterestCalculator {

    /**
     * Tính lãi đơn theo công thức cơ bản:
     *  lãi = (số dư * lãi suất / 100) * (số tháng / 12)
     *
     * @param balance Số tiền gốc
     * @param ratePercentPerYear Lãi suất %/năm
     * @param months Số tháng gửi
     */
    fun simpleAnnual(balance: Double, ratePercentPerYear: Double, months: Int): Double {
        return balance * (ratePercentPerYear / 100.0) * (months.toDouble() / 12.0)
    }
}
