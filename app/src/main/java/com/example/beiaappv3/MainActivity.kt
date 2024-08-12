package com.example.beiaappv3

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var usernameText: EditText
    private lateinit var passwordText: EditText
    private lateinit var loginButton: ImageButton
    private lateinit var registerButton: ImageButton
    private var isPasswordVisible: Boolean = false

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (isUserLoggedIn(this)) {
            // Redirect to SelectActivity
            val intent = Intent(this, SelectActivity::class.java)
            startActivity(intent)
            finish() // Close the login activity
        }

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

    }

    private fun loginUser(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                    saveLoginState(this);
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

    private fun saveLoginState(context: Context) {
        val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean("is_logged_in", true)
        editor.putLong("login_time", System.currentTimeMillis())
        editor.apply()
    }

    private fun isUserLoggedIn(context: Context): Boolean {
        val sharedPreferences = context.getSharedPreferences("login_prefs", Context.MODE_PRIVATE)
        val isLoggedIn = sharedPreferences.getBoolean("is_logged_in", false)
        val loginTime = sharedPreferences.getLong("login_time", 0L)
        val currentTime = System.currentTimeMillis()
        val tenMinutesInMillis = 5 * 60 * 1000

        return isLoggedIn && (currentTime - loginTime < tenMinutesInMillis)
    }

    private fun togglePasswordVisibility() {
        if (isPasswordVisible) {
            // Hide the password
            passwordText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            // Change the drawable to the visibility off icon, keeping the start drawable unchanged
            passwordText.setCompoundDrawablesWithIntrinsicBounds(
                passwordText.compoundDrawables[0], // Start drawable
                null, // Top drawable
                ContextCompat.getDrawable(this, R.drawable.visibility_off), // End drawable
                null // Bottom drawable
            )
        } else {
            // Show the password
            passwordText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            // Change the drawable to the visibility on icon, keeping the start drawable unchanged
            passwordText.setCompoundDrawablesWithIntrinsicBounds(
                passwordText.compoundDrawables[0], // Start drawable
                null, // Top drawable
                ContextCompat.getDrawable(this, R.drawable.visibility_on), // End drawable
                null // Bottom drawable
            )
        }
        // Move the cursor to the end of the text
        passwordText.setSelection(passwordText.text.length)
        // Toggle the isPasswordVisible flag
        isPasswordVisible = !isPasswordVisible
    }
}




