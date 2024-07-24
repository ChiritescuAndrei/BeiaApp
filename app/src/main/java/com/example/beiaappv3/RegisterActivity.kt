package com.example.beiaappv3

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidUserException

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var emailText: EditText
    private lateinit var passwordText: EditText
    private lateinit var confirmPasswordText: EditText
    private lateinit var registerButton: Button
    private lateinit var nameText: EditText
    private var isPasswordVisible: Boolean=false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register)
        auth = FirebaseAuth.getInstance()
        emailText = findViewById(R.id.email_text)
        passwordText = findViewById(R.id.password_text)
        confirmPasswordText = findViewById(R.id.confirm_password_text)
        registerButton = findViewById(R.id.register_btn)
        nameText = findViewById(R.id.name_text)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        passwordText.setOnTouchListener { _, event ->
            val DRAWABLE_END = 2
            if (event.action == MotionEvent.ACTION_UP) {
                if (event.rawX >= (passwordText.right - passwordText.compoundDrawables[DRAWABLE_END].bounds.width() - passwordText.paddingRight)) {
                    togglePasswordVisibility()
                    return@setOnTouchListener true
                }
            }
            false
        }

        registerButton.setOnClickListener {
            val email = emailText.text.toString().trim()
            val password = passwordText.text.toString().trim()
            val confirmPassword = confirmPasswordText.text.toString().trim()
            val name = nameText.text.toString().trim()

            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || name.isEmpty()) {
                Toast.makeText(this, "Please enter all the fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Toast.makeText(this, "Password doesn't match !", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!email.contains("@")) {
                Toast.makeText(this, "Invalid email address", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            checkIfEmailExistsAndRegister(email, password)
        }
    }

    private fun checkIfEmailExistsAndRegister(email: String, password: String) {
        auth.fetchSignInMethodsForEmail(email).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val signInMethods = task.result?.signInMethods ?: emptyList()
                if (signInMethods.isNotEmpty()) {
                    Toast.makeText(this, "The email is already in use", Toast.LENGTH_SHORT).show()
                } else {
                    registerUser(email, password)
                }
            } else {
                Toast.makeText(this, "Error checking email: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun registerUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Registration successful", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(this, "Registration failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }

        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide Password
            passwordText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            passwordText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.visibility_off, 0) // Change to your visibility icon
        } else {
            // Show Password
            passwordText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            passwordText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.visibility_on, 0) // Change to your visibility off icon
        }
        // Move the cursor to the end of the text
        passwordText.setSelection(passwordText.text.length)
        isPasswordVisible = !isPasswordVisible
    }



}




