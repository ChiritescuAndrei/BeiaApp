package com.example.beiaappv3

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class SelectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_select)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Find the ImageButton and set an OnClickListener
        val innerImageButton1: ImageButton = findViewById(R.id.dataVisuals_btn)
        innerImageButton1.setOnClickListener {
            // Display a Toast message
            Toast.makeText(this, "Data", Toast.LENGTH_SHORT).show()
            val intent = Intent(this, DataPlotting::class.java)
            startActivity(intent)
        }

        val innerImageButton2: ImageButton = findViewById(R.id.pumpControl_btn)
        innerImageButton2.setOnClickListener {
            // Display a Toast message
            Toast.makeText(this, "Pump", Toast.LENGTH_SHORT).show()
        }

        val innerImageButton3: ImageButton = findViewById(R.id.logout_btn)
        innerImageButton3.setOnClickListener {
            // Display a Toast message
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }
}
