package com.example.afinal.data.auth
import com.google.firebase.auth.FirebaseUser

/**
 * Sealed class biểu diễn các trạng thái kết quả của việc xác thực.
 * Giúp xử lý các trường hợp thành công, lỗi, và đang tải một cách tường minh.
 */
sealed class AuthResult {
    data object Loading : AuthResult()
    data class Success(val user: FirebaseUser) : AuthResult()
    data class Error(val message: String) : AuthResult()
}