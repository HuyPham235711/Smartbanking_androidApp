package com.example.afinal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.afinal.ui.auth.*
import com.example.afinal.ui.mortgage.MortgageListScreen
import com.example.afinal.viewmodel.mortgage.MortgageViewModel
import com.example.afinal.ui.theme.FinalTheme
import com.example.afinal.viewmodel.ViewModelFactory
import com.example.afinal.viewmodel.auth.LoginViewModel
import com.example.afinal.viewmodel.auth.PhoneAuthViewModel
import com.example.afinal.viewmodel.auth.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider


class LoginActivity : ComponentActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModelFactory = ViewModelFactory(applicationContext)

        setContent {
            FinalTheme {
                val navController = rememberNavController()
                val startDestination = if (auth.currentUser != null) "home" else "welcome"

                NavHost(navController = navController, startDestination = startDestination) {

                    composable("welcome") {
                        WelcomeScreen(
                            onContinue = { navController.navigate("login") }
                        )
                    }

                    // CHỈ CÓ MỘT composable("login") DUY NHẤT Ở ĐÂY
                    composable("login") {
                        val loginViewModel = ViewModelProvider(this@LoginActivity, viewModelFactory)[LoginViewModel::class.java]
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = {
                                // KIỂM TRA SAU KHI ĐĂNG NHẬP THÀNH CÔNG
                                val user = auth.currentUser
                                val hasPhoneLinked = user?.providerData?.any { it.providerId == PhoneAuthProvider.PROVIDER_ID } ?: false

                                if (hasPhoneLinked) {
                                    // Nếu đã có SĐT rồi, vào thẳng màn hình chính
                                    navController.navigate("home") {
                                        popUpTo("welcome") { inclusive = true }
                                    }
                                } else {
                                    // Nếu chưa có SĐT, mới đi đến màn hình 2FA (simplified flow)
                                    navController.navigate("otp_2fa")
                                }
                            },
                            onNavigateToRegister = { navController.navigate("register") }
                        )
                    }

                    composable("register") {
                        val registerViewModel = ViewModelProvider(this@LoginActivity, viewModelFactory)[RegisterViewModel::class.java]
                        RegisterScreen(
                            viewModel = registerViewModel,
                            onRegistrationSuccess = { navController.popBackStack() },
                            onNavigateBackToLogin = { navController.popBackStack() }
                        )
                    }

                    composable("otp_2fa") {
                        val phoneAuthViewModel = ViewModelProvider(this@LoginActivity, viewModelFactory)[PhoneAuthViewModel::class.java]
                        PhoneAuthScreen(
                            viewModel = phoneAuthViewModel,
                            onVerificationSuccess = {
                                navController.navigate("home") {
                                    popUpTo("welcome") { inclusive = true }
                                }
                            }
                        )
                    }

                    composable("home") {
                        val mortgageViewModel = ViewModelProvider(this@LoginActivity, viewModelFactory)[MortgageViewModel::class.java]
                        MortgageListScreen(
                            viewModel = mortgageViewModel,
                            onSelect = {
                                auth.signOut()
                                navController.navigate("welcome") {
                                    popUpTo(0)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}