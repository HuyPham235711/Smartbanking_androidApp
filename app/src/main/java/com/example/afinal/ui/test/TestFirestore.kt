package com.example.afinal.ui.test

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.afinal.R
import com.google.firebase.firestore.FirebaseFirestore

class TestFirestore : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.test_firebase) // layout trống cũng được

        val db = FirebaseFirestore.getInstance()

        // Data mẫu
        val user = hashMapOf(
            "first" to "Alan",
            "last" to "Turing",
            "born" to 1912
        )

        // Thêm vào Firestore
        db.collection("users")
            .add(user)
            .addOnSuccessListener { documentReference ->
                Log.d("FIRESTORE", "Document added with ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("FIRESTORE", "Error adding document", e)
            }
    }
}