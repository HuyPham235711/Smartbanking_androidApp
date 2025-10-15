
package com.example.afinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.afinal.ui.auth.AuthNavigation
import com.example.afinal.ui.customer.CustomerBalanceScreen
import com.example.afinal.ui.officer.CreateAccountScreen
import com.example.afinal.ui.officer.OfficerInterestScreen
import com.example.afinal.ui.test.TestInterestScreen
import com.example.afinal.ui.theme.FinalTheme
import androidx.lifecycle.lifecycleScope
import com.example.afinal.dev.Seed
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        lifecycleScope.launch { Seed.run(applicationContext) }

        setContent {
            //Đặt tên hàm muốn chạy ở đây

            //OfficerInterestScreen()
        }
    }
}