package com.example.beiaappv3

import android.app.AlertDialog
import android.widget.NumberPicker;
import android.view.WindowManager
import android.app.Dialog
import android.view.Gravity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.Toast
import android.view.LayoutInflater


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
                showTimerPickerDialog()
            } else {
                // Switch is off, show "Press to START the pump"
                setSpannableText(textSwitch, "Press to START the pump", "START", orangeColor)
            }
        }
    }

    private fun showTimerPickerDialog() {
        // Create a new dialog
        val dialog = Dialog(this)

        // Set the custom layout for the dialog
        dialog.setContentView(R.layout.activity_timer)

        // Find the NumberPicker and Button views in the dialog
        val hourPicker: NumberPicker = dialog.findViewById(R.id.hour_picker)
        val minutePicker: NumberPicker = dialog.findViewById(R.id.minute_picker)
        val secondPicker: NumberPicker = dialog.findViewById(R.id.second_picker)
        val doneButton: Button = dialog.findViewById(R.id.done_button)

        // Set min and max values for NumberPickers
        hourPicker.minValue = 0
        hourPicker.maxValue = 23
        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        secondPicker.minValue = 0
        secondPicker.maxValue = 59

        // Set the Done button click listener
        doneButton.setOnClickListener {
            // Get the selected values from NumberPickers
            val hours = hourPicker.value
            val minutes = minutePicker.value
            val seconds = secondPicker.value

            // Do something with the selected values
            // For example, you could display them in a Toast or update some UI
            Toast.makeText(this, "Time set: $hours:$minutes:$seconds", Toast.LENGTH_SHORT).show()

            // Dismiss the dialog
            dialog.dismiss()
            val orangeColor = ContextCompat.getColor(this, R.color.beia_orange)
            setSpannableText(textSwitch, "Press to STOP the pump", "STOP", orangeColor)
        }

        // Set dialog properties
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true) // Make dialog dismissible by tapping outside or using the back button

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes) // Copy existing attributes
        layoutParams.width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        layoutParams.gravity = Gravity.CENTER// Set width to match parent
        layoutParams.height = (resources.displayMetrics.heightPixels * 0.5).toInt()// Set height to wrap content
        dialog.window?.attributes = layoutParams

        // Show the dialog
        dialog.show()
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