package com.example.beiaappv3

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.eclipse.paho.client.mqttv3.MqttClient
import org.json.JSONObject

class DataPlotting : AppCompatActivity() {
    private val brokerUrl = "tcp://mqtt.beia-telemetrie.ro:1883"
    private val clientId = MqttClient.generateClientId()
    private val topic = "meshlium3d4c/TomatoGabiMatei/"

    private lateinit var mqttHandler: MqttHandler
    private lateinit var solarRadiationText: TextView
    private lateinit var luminosityText: TextView
    private lateinit var humidityText: TextView
    private lateinit var temperatureText: TextView
    private lateinit var back: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_plotting)

        solarRadiationText = findViewById(R.id.solarRadiationText)
        luminosityText = findViewById(R.id.luminosityText)
        humidityText = findViewById(R.id.humidityText)
        temperatureText = findViewById(R.id.temperatureText)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        mqttHandler = MqttHandler { message ->
            runOnUiThread {
                handleMessage(message)
            }
        }
        mqttHandler.connect(brokerUrl, clientId)
        mqttHandler.subscribe(topic)

        // Retrieve and display saved values from SharedPreferences
        loadSavedValues()

        //use back the image button to go back to the main activity
        back = findViewById(R.id.back_button)
        back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun handleMessage(message: String) {
        val jsonObject = JSONObject(message)

        val humArray = jsonObject.getJSONArray("HUM: 3")
        val luxArray = jsonObject.getJSONArray("LUX: 4")
        val parArray = jsonObject.getJSONArray("PAR: 5")
        val tcArray = jsonObject.getJSONArray("TC: 11")

        val lastHumValue = humArray.getJSONObject(humArray.length() - 1).getDouble("value")
        val lastLuxValue = luxArray.getJSONObject(luxArray.length() - 1).getDouble("value")
        val lastParValue = parArray.getJSONObject(parArray.length() - 1).getDouble("value")
        val lastTcValue = tcArray.getJSONObject(tcArray.length() - 1).getDouble("value")

        val roundedHumValue = String.format("%.2f", lastHumValue)
        val roundedLuxValue = String.format("%.2f", lastLuxValue)
        val roundedParValue = String.format("%.2f", lastParValue)
        val roundedTcValue = String.format("%.2f", lastTcValue)

        humidityText.setText(roundedHumValue)
        luminosityText.setText(roundedLuxValue)
        solarRadiationText.setText(roundedParValue)
        temperatureText.setText(roundedTcValue)

        // Save values to SharedPreferences
        saveValues(roundedHumValue, roundedLuxValue, roundedParValue, roundedTcValue)
    }

    private fun saveValues(hum: String, lux: String, par: String, tc: String) {
        val sharedPreferences = getSharedPreferences("DataPlottingPrefs", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("HUM", hum)
            putString("LUX", lux)
            putString("PAR", par)
            putString("TC", tc)
            apply()
        }
    }

    private fun loadSavedValues() {
        val sharedPreferences = getSharedPreferences("DataPlottingPrefs", Context.MODE_PRIVATE)
        val savedHum = sharedPreferences.getString("HUM", null)
        val savedLux = sharedPreferences.getString("LUX", null)
        val savedPar = sharedPreferences.getString("PAR", null)
        val savedTc = sharedPreferences.getString("TC", null)

        if (savedHum != null) humidityText.setText(savedHum)
        if (savedLux != null) luminosityText.setText(savedLux)
        if (savedPar != null) solarRadiationText.setText(savedPar)
        if (savedTc != null) temperatureText.setText(savedTc)
    }

    override fun onDestroy() {
        super.onDestroy()
        mqttHandler.disconnect()
    }
}
