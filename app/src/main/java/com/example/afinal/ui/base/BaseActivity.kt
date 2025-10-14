package com.example.afinal.ui.base

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.afinal.R
import com.example.afinal.databinding.ActivityBaseBinding

class BaseActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBaseBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBaseBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Thiết lập Toolbar
        setSupportActionBar(binding.topAppBar)

        // Navigation setup
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Gắn với Bottom Navigation
        binding.bottomNavigation.setupWithNavController(navController)

        // FAB Click
        binding.fab.setOnClickListener {
            // Ví dụ: mở fragment mới hoặc toast
            // navController.navigate(R.id.action_to_addFragment)
        }
    }
}