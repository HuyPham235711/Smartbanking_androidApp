
package com.example.afinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.afinal.ui.auth.AuthNavigation
import com.example.afinal.ui.officer.CreateAccountScreen
import com.example.afinal.ui.theme.FinalTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FinalTheme {
                // Hiển thị màn hình điều hướng xác thực
                //AuthNavigation()
                //Hiển thị màn hình create account của officer
                CreateAccountScreen()
            }
        }
    }
}