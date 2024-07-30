package com.example.beiaappv3

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.widget.TextView
import org.eclipse.paho.client.mqttv3.MqttClient
import org.json.JSONObject

class DataPlotting : AppCompatActivity() {
//    private val brokerUrl = "tcp://mqtt.beia-telemetrie.ro:1883"
//    private val clientId = MqttClient.generateClientId()
//    private val topic = "meshlium3d4c/SAPRobertFilip/#"
//
//    private lateinit var mqttHandler: MqttHandler
//    private lateinit var messageTextView1: TextView
//    private lateinit var messageTextView2: TextView
//    private lateinit var messageTextView3: TextView
//    private lateinit var messageTextView4: TextView
//
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        enableEdgeToEdge()
        setContentView(R.layout.activity_data_plotting)
//
//        //messageTextView1 = findViewById(R.id.textView1)
//        messageTextView2 = findViewById(R.id.textView2)
//        messageTextView3 = findViewById(R.id.textView3)
//        messageTextView4 = findViewById(R.id.textView4)
//
//        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
//            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
//            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
//            insets
//        }
//
//        mqttHandler = MqttHandler { message ->
//            runOnUiThread {
//                handleMessage(message)
//            }
//        }
//        mqttHandler.connect(brokerUrl, clientId)
//        mqttHandler.subscribe(topic)
//    }
//
//    private fun handleMessage(message: String) {
//        val jsonObject = JSONObject(message)
//        val sensor = jsonObject.getString("sensor")
//        val value = jsonObject.getString("value")
//
//        when (sensor) {
//            "LUX" -> messageTextView1.text = value
//            "HUM" -> messageTextView2.text = value
//            "PAR" -> messageTextView3.text = value
//            "TC" -> messageTextView4.text = value
//        }
    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mqttHandler.disconnect()
//    }
}
