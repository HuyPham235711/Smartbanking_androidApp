package com.example.afinal.data.auth

import android.app.Activity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import java.util.concurrent.TimeUnit

/**
 * Repository chịu trách nhiệm xử lý các tác vụ xác thực với Firebase.
 * Tách biệt hoàn toàn logic Firebase khỏi ViewModel.
 */
class AuthRepository(private val auth: FirebaseAuth) {

    /**
     * Thực hiện đăng nhập bằng email và mật khẩu.
     */
    fun loginUser(email: String, password: String): Flow<AuthResult> = callbackFlow {
        trySend(AuthResult.Loading)
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(AuthResult.Success(task.result.user!!))
                } else {
                    trySend(AuthResult.Error(task.exception?.message ?: "Lỗi đăng nhập không xác định"))
                }
                close()
            }
            .addOnFailureListener { e ->
                trySend(AuthResult.Error(e.message ?: "Lỗi kết nối"))
                close()
            }
        awaitClose { /* Cleanup */ }
    }

    /**
     * Thực hiện đăng ký bằng email và mật khẩu.
     */
    fun registerUser(email: String, password: String): Flow<AuthResult> = callbackFlow {
        trySend(AuthResult.Loading)
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(AuthResult.Success(task.result.user!!))
                } else {
                    trySend(AuthResult.Error(task.exception?.message ?: "Lỗi đăng ký không xác định"))
                }
                close()
            }
            .addOnFailureListener { e ->
                trySend(AuthResult.Error(e.message ?: "Lỗi kết nối"))
                close()
            }
        awaitClose { /* Cleanup */ }
    }

    /**
     * Gửi mã OTP đến số điện thoại.
     */
    fun sendOtp(phoneNumber: String, activity: Activity): Flow<OtpResult> = callbackFlow {
        trySend(OtpResult.Loading)
        val callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                trySend(OtpResult.VerificationCompleted(credential))
                close()
            }
            override fun onVerificationFailed(e: com.google.firebase.FirebaseException) {
                trySend(OtpResult.Error(e.message ?: "Lỗi gửi OTP"))
                close()
            }
            override fun onCodeSent(verificationId: String, token: PhoneAuthProvider.ForceResendingToken) {
                trySend(OtpResult.CodeSent(verificationId, token))
            }
        }
        val options = com.google.firebase.auth.PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(activity)
            .setCallbacks(callbacks)
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
        awaitClose { /* Cleanup */ }
    }

    /**
     * Xác thực bằng OTP.
     */
    fun signInOrLinkWithOtp(verificationId: String, otpCode: String): Flow<AuthResult> = callbackFlow {
        trySend(AuthResult.Loading)
        val credential = PhoneAuthProvider.getCredential(verificationId, otpCode)
        val currentUser = auth.currentUser
        val authTask = if (currentUser != null) {
            currentUser.linkWithCredential(credential)
        } else {
            auth.signInWithCredential(credential)
        }
        authTask.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                trySend(AuthResult.Success(task.result.user!!))
            } else {
                trySend(AuthResult.Error(task.exception?.message ?: "Mã OTP không hợp lệ"))
            }
            close()
        }
        awaitClose { /* Cleanup */ }
    }

    /**
     * Gửi email xác thực đến người dùng hiện tại.
     */
    fun sendEmailVerification(): Flow<Boolean> = callbackFlow {
        val user = auth.currentUser
        if (user == null) {
            trySend(false)
            close()
            return@callbackFlow
        }
        user.sendEmailVerification()
            .addOnCompleteListener { task ->
                trySend(task.isSuccessful)
                close()
            }
        awaitClose { /* Cleanup */ }
    }

    fun sendPasswordResetEmail(email: String): Flow<Result<String>> = callbackFlow {

        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    trySend(Result.success("Đã gửi email đặt lại mật khẩu! Vui lòng kiểm tra hộp thư."))
                } else {
                    trySend(Result.failure(Exception(task.exception?.message ?: "Lỗi không xác định")))
                }
                close()
            }
        awaitClose { }
    }
}

// Sealed class để quản lý các trạng thái của việc gửi OTP
sealed class OtpResult {
    data object Loading : OtpResult()
    data class CodeSent(val verificationId: String, val token: PhoneAuthProvider.ForceResendingToken) : OtpResult()
    data class VerificationCompleted(val credential: PhoneAuthCredential) : OtpResult()
    data class Error(val message: String) : OtpResult()
}