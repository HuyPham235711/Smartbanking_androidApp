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
import com.example.afinal.utils.SessionManager
import com.example.afinal.viewmodel.ViewModelFactory
import com.example.afinal.viewmodel.auth.LoginViewModel
import com.example.afinal.viewmodel.auth.PhoneAuthViewModel
import com.example.afinal.viewmodel.auth.RegisterViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthProvider
import androidx.lifecycle.lifecycleScope
import com.example.afinal.data.account.AccountRepository
import com.example.afinal.data.database.AppDatabase
import kotlinx.coroutines.launch


class LoginActivity : ComponentActivity() {

    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    // TRUY CẬP DATABASE
    private val accountRepository: AccountRepository by lazy {
        AccountRepository(AppDatabase.getDatabase(applicationContext).accountDao())
    }

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
            // ✅ SỬA LỖI 1: Phải lấy role bất đồng bộ giống như logic login
            lifecycleScope.launch {
                val user = auth.currentUser // Lấy user an toàn
                if (user?.email != null) {
                    // ⭐️ GỌI HÀM MỚI (ĐỌC TỪ FIRESTORE)
                    val account = accountRepository.getAccountByEmailFromFirestore(user.email!!)
                    val userRole = account?.role ?: "Customer"
                    goToMainActivity(userRole)
                } else {
                    // Trường hợp hiếm: user tồn tại nhưng không có email (ví dụ: chỉ đăng nhập SĐT)
                    goToMainActivity("Customer") // Mặc định là Customer
                }
            }
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

                    // Đây là code bạn đã sửa đúng
                    composable("login") {
                        val loginViewModel = ViewModelProvider(this@LoginActivity, viewModelFactory)[LoginViewModel::class.java]
                        LoginScreen(
                            viewModel = loginViewModel,
                            onLoginSuccess = {
                                val user = auth.currentUser
                                if (user?.email == null) {
                                    return@LoginScreen
                                }

                                // ⭐️ TÌM VAI TRÒ DỰA TRÊN EMAIL
                                lifecycleScope.launch {
                                    // ⭐️ GỌI HÀM MỚI (ĐỌC TỪ FIRESTORE)
                                    val account = accountRepository.getAccountByEmailFromFirestore(user.email!!)
                                    val userRole = account?.role ?: "Customer" // Mặc định là Customer nếu không tìm thấy

                                    val hasPhoneLinked = user.providerData.any { it.providerId == PhoneAuthProvider.PROVIDER_ID }

                                    if (hasPhoneLinked) {
                                        goToMainActivity(userRole)
                                    } else {
                                        // Chuyển sang 2FA
                                        navController.navigate("otp_2fa/${userRole}")
                                    }
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

                    // Thêm {userRole} vào định nghĩa route
                    composable("otp_2fa/{userRole}") { backStackEntry ->
                        // Lấy userRole từ đường dẫn
                        val userRole = backStackEntry.arguments?.getString("userRole") ?: "Customer"

                        val phoneAuthViewModel = ViewModelProvider(this@LoginActivity, viewModelFactory)[PhoneAuthViewModel::class.java]
                        PhoneAuthScreen(
                            viewModel = phoneAuthViewModel,
                            onVerificationSuccess = {
                                // Truyền userRole khi gọi hàm
                                goToMainActivity(userRole)
                            }
                        )
                    }
                }
            }
        }
    }

    /**
     * 9. Hàm mới để khởi chạy MainActivity
     */
    private fun goToMainActivity(userRole: String) {
        val intent = Intent(this, MainActivity::class.java)

        //  Gửi vai trò (role) sang MainActivity
        intent.putExtra("USER_ROLE", userRole)

        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish() // Đóng LoginActivity
    }
}