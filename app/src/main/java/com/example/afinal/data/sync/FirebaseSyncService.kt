package com.example.afinal.data.sync

import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class FirebaseSyncService(
    private val collectionName: String
) {
    private val db = Firebase.firestore

    // --- Upsert document (Create or Update) ---
    suspend fun upsert(documentId: String, data: Map<String, Any>) {
        try {
            db.collection(collectionName)
                .document(documentId)
                .set(data)
                .await() // suspend until success
            println("✅ Firestore upsert success [$collectionName/$documentId]")
        } catch (e: Exception) {
            println("❌ Firestore upsert failed [$collectionName/$documentId]: ${e.message}")
        }
    }

    // --- Delete document (Remove from Firestore) ---
    suspend fun delete(documentId: String) {
        try {
            db.collection(collectionName)
                .document(documentId)
                .delete()
                .await() // suspend until success
            println("🗑️ Firestore delete success [$collectionName/$documentId]")
        } catch (e: Exception) {
            println("❌ Firestore delete failed [$collectionName/$documentId]: ${e.message}")
        }
    }


    // --- Listen to collection changes in real-time ---
    fun <T> listenCollection(mapper: (Map<String, Any>) -> T): Flow<List<T>> = callbackFlow {
        val listener = db.collection(collectionName)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("⚠️ Firestore listen error [${collectionName}]: ${error.message}")
                    return@addSnapshotListener
                }

                val list = snapshot?.documents?.mapNotNull { doc ->
                    doc.data?.let { mapper(it) }
                } ?: emptyList()

                trySend(list)
            }

        awaitClose { listener.remove() }
    }

    suspend fun <T> getAllOnce(mapper: (Map<String, Any>) -> T): List<T> {
        return try {
            val snapshot = db.collection(collectionName).get().await()
            snapshot.documents.mapNotNull { doc ->
                doc.data?.let(mapper)
            }
        } catch (e: Exception) {
            println("❌ Firestore getAllOnce failed: ${e.message}")
            emptyList()
        }
    }


}
