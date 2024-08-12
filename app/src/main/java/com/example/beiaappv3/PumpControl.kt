package com.example.beiaappv3

import android.os.CountDownTimer
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.app.Dialog
import android.view.Gravity
import android.view.WindowManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.widget.NumberPicker
import android.widget.Toast
import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.content.Context
import android.content.Intent
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.widget.Button
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.core.content.ContextCompat
import org.eclipse.paho.client.mqttv3.MqttClient
import org.json.JSONObject

@SuppressLint("UseSwitchCompatOrMaterialCode")
class PumpControl : AppCompatActivity() {
    private val brokerUrl = "tcp://mqtt.beia-telemetrie.ro:1883"
    private val clientId = MqttClient.generateClientId()
    private val topic = "meshlium3d4c/Gabi/TC"
    private lateinit var switchButton: Switch
    private lateinit var waterDrop: ImageView
    private lateinit var mqttHandler: MqttHandler
    private lateinit var back: ImageView
    private lateinit var textSwitch: TextView
    private lateinit var text_timer: TextView
    private var countDownTimer: CountDownTimer? = null
    private lateinit var dropAnimation: Animation
    private lateinit var alphaAnimation: Animation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pump_control)
        text_timer = findViewById(R.id.text_timer)
        dropAnimation = AnimationUtils.loadAnimation(this, R.anim.drop_animation)
        waterDrop = findViewById(R.id.water_drop)

        //use back the image button to go back to the main activity
        back = findViewById(R.id.back_button)
        back.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize mqttHandler
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

//        // When I press the button, the publish function is called
//        switchButton.setOnClickListener {
//            // Send "ON" as JSON object to the topic
//            val json = JSONObject()
//            json.put("Pump", "ON")
//            json.put("Duration", countDownTimer)
//            mqttHandler.publish(topic, json.toString())
//        }

        switchButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                // Switch is on, show "Press to STOP the pump"
                showTimerPickerDialog()
            } else {
                // Send "OFF" as JSON object to the topic
                val json = JSONObject()
                json.put("Pump", "OFF")
                mqttHandler.publish(topic, json.toString())

                // Switch is off, show "Press to START the pump"
                countDownTimer?.cancel()
                text_timer.text = "00:00:00"
                stopWaterDropAnimations()
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

            if (hours == 0 && minutes == 0 && seconds == 0) {
                // Timer is set to 0, turn off the switch
                turnOffSwitch()
            } else {
                // Start the countdown timer
                startCountdownTimer(hours, minutes, seconds)

                // Send "ON" and the duration as JSON object to the topic
                val json = JSONObject()
                json.put("Pump", "ON")
                json.put("Duration", (hours * 3600 + minutes * 60 + seconds))
                mqttHandler.publish(topic, json.toString())

                val orangeColor = ContextCompat.getColor(this, R.color.beia_orange)
                setSpannableText(textSwitch, "Press to STOP the pump", "STOP", orangeColor)
            }

            // Dismiss the dialog
            dialog.dismiss()
        }

        // Set dialog properties
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(true) // Make dialog dismissible by tapping outside or using the back button

        val layoutParams = WindowManager.LayoutParams()
        layoutParams.copyFrom(dialog.window?.attributes) // Copy existing attributes
        layoutParams.width = (resources.displayMetrics.widthPixels * 0.85).toInt()
        layoutParams.gravity = Gravity.CENTER // Set width to match parent
        layoutParams.height = (resources.displayMetrics.heightPixels * 0.5).toInt() // Set height to wrap content
        dialog.window?.attributes = layoutParams

        // Show the dialog
        dialog.show()
    }

    private fun startCountdownTimer(hours: Int, minutes: Int, seconds: Int) {
        val totalMillis = (hours * 3600 + minutes * 60 + seconds) * 1000L
        startWaterDropAnimations(dropAnimation)
        countDownTimer?.cancel()
        countDownTimer = object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // Update UI with the remaining time (optional)
                val remainingSeconds = millisUntilFinished / 1000
                val hrs = remainingSeconds / 3600
                val mins = (remainingSeconds % 3600) / 60
                val secs = remainingSeconds % 60
                val orangeColor = ContextCompat.getColor(this@PumpControl, R.color.beia_orange)
                setSpannableText(textSwitch, "Press to STOP the pump", "STOP", orangeColor)
                text_timer.text = String.format("%02d:%02d:%02d", hrs, mins, secs)

            }

            override fun onFinish() {
                // Time is up, turn off the switch
                stopWaterDropAnimations()
                waterDrop.visibility = ImageView.GONE

                turnOffSwitch()
            }
        }.start()
    }

    private fun turnOffSwitch() {
        switchButton.isChecked = false
        val orangeColor = ContextCompat.getColor(this, R.color.beia_orange)
        setSpannableText(textSwitch, "Press to START the pump", "START", orangeColor)
    }

    private fun handleMessage(message: String) {
        val jsonObject = JSONObject(message)
        val tcArray = jsonObject.getJSONArray("TC: 11")
        val tc = tcArray.getDouble(0)
    }
    private fun startWaterDropAnimations(vararg animations: Animation) {
        // Make the water drop visible and start animations
        waterDrop.visibility = ImageView.VISIBLE
        for (animation in animations) {
            waterDrop.startAnimation(animation)
        }
    }

    private fun stopWaterDropAnimations() {
        // Stop any ongoing animations
        waterDrop.clearAnimation()
        waterDrop.visibility = ImageView.GONE
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
