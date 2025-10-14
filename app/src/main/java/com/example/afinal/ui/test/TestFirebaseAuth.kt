package com.example.afinal.ui.test

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.afinal.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

class TestFirebaseAuth : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var statusText: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_test)

        auth = FirebaseAuth.getInstance()
        emailEdit = findViewById(R.id.emailEdit)
        passwordEdit = findViewById(R.id.passwordEdit)
        statusText = findViewById(R.id.statusText)

        findViewById<Button>(R.id.registerBtn).setOnClickListener {
            registerUser(emailEdit.text.toString(), passwordEdit.text.toString())
        }

        findViewById<Button>(R.id.loginBtn).setOnClickListener {
            loginUser(emailEdit.text.toString(), passwordEdit.text.toString())
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user, "Register success ✅")
                } else {
                    updateUI(null, "Register failed ❌: ${task.exception?.message}")
                }
            }
    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val user = auth.currentUser
                    updateUI(user, "Login success ✅")
                } else {
                    updateUI(null, "Login failed ❌: ${task.exception?.message}")
                }
            }
    }

    private fun updateUI(user: FirebaseUser?, message: String) {
        if (user != null) {
            val info = "UID: ${user.uid}\nEmail: ${user.email}"
            statusText.text = "$message\n$info"
            Log.d("AUTH_TEST", info)
        } else {
            statusText.text = message
        }
    }
}