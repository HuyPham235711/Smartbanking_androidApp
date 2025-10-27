package com.example.afinal.data.sync

sealed class FirebaseSyncResult {
    object Success : FirebaseSyncResult()
    data class Error(val exception: Exception) : FirebaseSyncResult()
    object InProgress : FirebaseSyncResult()
}
