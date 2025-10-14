package com.example.afinal.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.afinal.R

class NavHostActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_nav_host)
    }
}