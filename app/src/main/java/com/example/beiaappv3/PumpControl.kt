package com.example.beiaappv3

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Context
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import org.eclipse.paho.client.mqttv3.MqttClient
import org.json.JSONObject

class PumpControl : AppCompatActivity() {
    private val brokerUrl = "tcp://mqtt.beia-telemetrie.ro:1883"
    private val clientId = MqttClient.generateClientId()
    private val topic = "meshlium3d4c/Gabi/TC"
    private lateinit var button: ImageButton
    private lateinit var mqttHandler: MqttHandler

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pump_control)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //intialize mqttHandler
        mqttHandler = MqttHandler { message ->
            runOnUiThread {
                handleMessage(message)
            }
        }
        button = findViewById(R.id.toggleButton   )
        mqttHandler.connect(brokerUrl, clientId)
        //when I press the button, the publish function is called
        button.setOnClickListener {
            //send "ON" as json object to the topic
            val json = JSONObject()
            json.put("Pump", "ON")
            mqttHandler.publish(topic, json.toString())
        }
    }
    private fun handleMessage(message: String) {
        val jsonObject = JSONObject(message)
        val tcArray = jsonObject.getJSONArray("TC: 11")
        val tc = tcArray.getDouble(0)
    }
}