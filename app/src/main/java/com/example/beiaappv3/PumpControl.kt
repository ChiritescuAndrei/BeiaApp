package com.example.beiaappv3

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Context
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Switch
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.core.content.ContextCompat
import org.eclipse.paho.client.mqttv3.MqttClient
import org.json.JSONObject

@SuppressLint("UseSwitchCompatOrMaterialCode")
class PumpControl : AppCompatActivity() {
    private val brokerUrl = "tcp://mqtt.beia-telemetrie.ro:1883"
    private val clientId = MqttClient.generateClientId()
    private val topic = "meshlium3d4c/Gabi/TC"
    private lateinit var switchButton: Switch
    private lateinit var mqttHandler: MqttHandler
    private lateinit var textSwitch: TextView

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
        textSwitch = findViewById(R.id.text_switch)
        val orangeColor = ContextCompat.getColor(this, R.color.beia_orange)
        setSpannableText(textSwitch, "Press to START the pump", "START", orangeColor)

        switchButton = findViewById(R.id.switch_button)
        mqttHandler.connect(brokerUrl, clientId)

        //when I press the button, the publish function is called
        switchButton.setOnClickListener {
            //send "ON" as json object to the topic
            val json = JSONObject()
            json.put("Pump", "ON")
            mqttHandler.publish(topic, json.toString())
        }

        switchButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Switch is on, show "Press to STOP the pump"
                setSpannableText(textSwitch, "Press to STOP the pump", "STOP", orangeColor)
            } else {
                // Switch is off, show "Press to START the pump"
                setSpannableText(textSwitch, "Press to START the pump", "START", orangeColor)
            }
        }
    }
    private fun handleMessage(message: String) {
        val jsonObject = JSONObject(message)
        val tcArray = jsonObject.getJSONArray("TC: 11")
        val tc = tcArray.getDouble(0)
    }


    private fun setSpannableText(textView: TextView, fullText: String, wordToColor: String, color: Int) {
        val spannableString = SpannableString(fullText)
        val start = fullText.indexOf(wordToColor)
        val end = start + wordToColor.length

        spannableString.setSpan(
            ForegroundColorSpan(color),
            start,
            end,
            Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        textView.text = spannableString
    }
}