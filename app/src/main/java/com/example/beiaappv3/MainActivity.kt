package com.example.beiaappv3

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var usernameText: EditText
    private lateinit var passwordText: EditText
    private lateinit var loginButton: ImageButton
    private lateinit var registerButton: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
         auth = FirebaseAuth.getInstance()
         usernameText = findViewById(R.id.usernameText)
         passwordText = findViewById(R.id.password_text)
         loginButton = findViewById(R.id.login_btn)
         registerButton = findViewById(R.id.register_btn)

        registerButton.setOnClickListener {
            // Inițiază un intent pentru a deschide activitatea Register
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }

        loginButton.setOnClickListener {
            val username = usernameText.text.toString().trim()
            val password = passwordText.text.toString().trim()

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both email and password", Toast.LENGTH_SHORT).show()
            } else {
                loginUser(username, password)
            }
        }

    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                // Login success, navigate to SelectActivity
                val intent = Intent(this, SelectActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                // Login failed, show error message
                Toast.makeText(this, "Account does not exist or invalid credentials", Toast.LENGTH_SHORT).show()
            }
        }
    }



}
