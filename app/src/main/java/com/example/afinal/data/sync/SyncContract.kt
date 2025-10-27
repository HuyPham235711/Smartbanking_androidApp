package com.example.afinal.data.sync

import kotlinx.coroutines.flow.Flow

interface SyncableRepository<T> {
    suspend fun pushLocalChange(entity: T)
    fun listenRemoteChanges(): Flow<List<T>>
}
