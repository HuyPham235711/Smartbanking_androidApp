package com.example.afinal.ui.test

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import com.example.afinal.utils.InterestCalculator

/**
 * Màn hình test logic tính lãi suất.
 * Có thể chạy trực tiếp bằng cách set trong MainActivity.
 */
class TestInterest : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TestInterestScreen()
        }
    }
}

@Composable
fun TestInterestScreen() {
    val interest = remember {
        InterestCalculator.simpleAnnual(
            balance = 1_000_000.0,
            ratePercentPerYear = 6.0,
            months = 6
        )
    }

    Text(
        text = "Lãi dự kiến: " + String.format("%,.0f đ", interest)
    )

}
