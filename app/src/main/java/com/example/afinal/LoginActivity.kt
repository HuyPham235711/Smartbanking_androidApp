package com.example.afinal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.afinal.ui.auth.*
import com.example.afinal.ui.theme.FinalTheme
import com.example.afinal.utils.SessionManager // 1. Import
import com.example.afinal.viewmodel.ViewModelFactory
import com.example.afinal.viewmodel.auth.LoginViewModel
import com.example.afinal.viewmodel.auth.PhoneAuthViewModel
import com.example.afinal.viewmodel.auth.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider


class LoginActivity : ComponentActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // 2. Khai báo SessionManager
    private lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val viewModelFactory = ViewModelFactory(applicationContext)

        // 3. Khởi tạo SessionManager
        sessionManager = SessionManager(applicationContext)

        // 4. KIỂM TRA SESSION NGAY KHI MỞ APP (TRƯỚC SETCONTENT)
        if (auth.currentUser != null) {
            // Nếu đã đăng nhập (Firebase token còn),
            // thì đi thẳng vào app chính, KHÔNG hiển thị màn hình Login
            goToMainActivity()
            return // Dừng hàm onCreate ở đây
        }

        // 5. Nếu chưa đăng nhập, mới hiển thị UI của Login
        setContent {
            FinalTheme {
                val navController = rememberNavController()

                // Luôn bắt đầu từ "welcome" vì ta đã xử lý "home" ở trên
                NavHost(navController = navController, startDestination = "welcome") {

                    composable("welcome") {
                        WelcomeScreen(
                            onContinue = { navController.navigate("login") }
                        )
                    }

                    composable("login") {
                        val loginViewModel = ViewModelProvider(this@LoginActivity, viewModelFactory)[LoginViewModel::class.java]
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = {
                                val user = auth.currentUser
                                val hasPhoneLinked = user?.providerData?.any { it.providerId == PhoneAuthProvider.PROVIDER_ID } ?: false

                                if (hasPhoneLinked) {
                                    // 6. ✅ SỬA LOGIC: Khởi chạy MainActivity
                                    goToMainActivity()
                                } else {
                                    // Chuyển sang 2FA
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
                                // 7. ✅ SỬA LOGIC: Sau khi 2FA cũng phải vào MainActivity
                                goToMainActivity()
                            }
                        )
                    }

                    // 8. (KHÔNG CÒN) composable("home") đã bị xóa.
                    //    Màn hình "home" cũ của bạn (MortgageListScreen)
                    //    sẽ được hiển thị BÊN TRONG MainActivity.
                }
            }
        }
    }

    /**
     * 9. Hàm mới để khởi chạy MainActivity
     */
    private fun goToMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        // Cờ này rất quan trọng để xóa LoginActivity khỏi stack
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Đóng LoginActivity
    }
}