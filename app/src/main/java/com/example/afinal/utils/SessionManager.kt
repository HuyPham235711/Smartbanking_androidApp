package com.example.afinal.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Quản lý session người dùng, chủ yếu để xử lý inactivity timeout.
 */
class SessionManager(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("SessionPrefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_LAST_ACTIVE = "last_active_timestamp"

        // 5 phút (tính bằng mili-giây)
        // Bạn có thể thay đổi giá trị này
        const val INACTIVITY_TIMEOUT_MS: Long = 10 * 1000
    }

    /**
     * Lưu lại thời điểm hiện tại khi app bị đưa vào background (onStop).
     */
    fun saveLastActiveTimestamp() {
        prefs.edit().putLong(KEY_LAST_ACTIVE, System.currentTimeMillis()).apply()
        println("SessionManager: Đã lưu thời điểm cuối cùng hoạt động.")
    }

    /**
     * Kiểm tra xem session có hết hạn do không hoạt động không.
     * Được gọi khi app quay lại (onStart).
     */
    fun isSessionExpired(): Boolean {
        val lastActive = prefs.getLong(KEY_LAST_ACTIVE, 0L)

        // Nếu chưa bao giờ lưu, thì không hết hạn
        if (lastActive == 0L) {
            return false
        }

        val elapsed = System.currentTimeMillis() - lastActive
        val isExpired = elapsed > INACTIVITY_TIMEOUT_MS

        if (isExpired) {
            println("SessionManager: Session đã hết hạn (vắng mặt ${elapsed / 1000}s).")
        }

        return isExpired
    }

    /**
     * Xóa timestamp khi người dùng chủ động đăng xuất.
     * Ngăn việc bị tự động logout ngay sau khi đăng nhập lại.
     */
    fun clearSession() {
        prefs.edit().remove(KEY_LAST_ACTIVE).apply()
        println("SessionManager: Đã xóa session timestamp.")
    }
}