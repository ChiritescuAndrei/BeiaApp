package com.example.beiaappv3

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.os.CountDownTimer
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import android.view.Gravity
import android.view.WindowManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.NumberPicker
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import org.eclipse.paho.client.mqttv3.MqttClient
import org.json.JSONObject

@SuppressLint("UseSwitchCompatOrMaterialCode")
class PumpControl : AppCompatActivity() {

    private val brokerUrl = "tcp://mqtt.beia-telemetrie.ro:1883"
    //private var savedMillis: Long = 0
    private val clientId = MqttClient.generateClientId()
    private val topic = "meshlium3d4c/Gabi/TC"
    private lateinit var switchButton: Switch
    private lateinit var waterDrop: ImageView
    private lateinit var mqttHandler: MqttHandler
    private lateinit var textSwitch: TextView
    private lateinit var text_timer: TextView
    private var countDownTimer: CountDownTimer? = null
    private lateinit var dropAnimation: Animation
    private lateinit var alphaAnimation: Animation
    private lateinit var sharedPreferences: SharedPreferences
    private val PREFS_NAME = "PumpControlPrefs"
    private val TIMER_KEY = "timer_duration"
    private lateinit var back: ImageButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pump_control)

        // Initialize shared preferences
        sharedPreferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)

        text_timer = findViewById(R.id.text_timer)
        dropAnimation = AnimationUtils.loadAnimation(this, R.anim.drop_animation)
        waterDrop = findViewById(R.id.water_drop)

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
//        setSpannableText(textSwitch, "Press to START the pump", "START", orangeColor)

        switchButton = findViewById(R.id.switch_button)
        mqttHandler.connect(brokerUrl, clientId)

        back = findViewById(R.id.back_button)


        switchButton.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                setSpannableText(textSwitch, "Press to STOP the pump", "STOP", orangeColor)
                // Switch is on, show timer picker dialog
                showTimerPickerDialog()

            } else {
                // Switch is off, reset timer and UI
                resetTimerUI()
            }
        }

        back.setOnClickListener(){
            if(switchButton.isChecked){
                Toast.makeText(this, "Please stop the pump before going back", Toast.LENGTH_SHORT).show()

            }
            else {
                val intent = Intent(this, SelectActivity::class.java)
                startActivity(intent)
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
                resetTimerUI()
            } else {
                // Start the countdown timer
                startCountdownTimer(hours, minutes, seconds)
                val orangeColor = ContextCompat.getColor(this, R.color.beia_orange)
                setSpannableText(textSwitch, "Press to STOP the pump", "STOP", orangeColor)
            }

            // Dismiss the dialog
            dialog.dismiss()
        }

        // Set dialog properties
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.setCancelable(false) // Make dialog dismissible by tapping outside or using the back button

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

        // Initialize and start CountDownTimer
        countDownTimer = object : CountDownTimer(totalMillis, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val remainingSeconds = millisUntilFinished / 1000
                val hrs = remainingSeconds / 3600
                val mins = (remainingSeconds % 3600) / 60
                val secs = remainingSeconds % 60
                text_timer.text = String.format("%02d:%02d:%02d", hrs, mins, secs)

                // Save timer state
                with(sharedPreferences.edit()) {
                    putLong(TIMER_KEY, millisUntilFinished)
                    apply()
                }

            }

            override fun onFinish() {
                resetTimerUI()
            }
        }.start()
    }

    private fun resetTimerUI() {
        switchButton.isChecked = false
        countDownTimer?.cancel()
        countDownTimer = null

//        switchButton.isChecked = false
        text_timer.text = "00:00:00"
        stopWaterDropAnimations()

        val orangeColor = ContextCompat.getColor(this, R.color.beia_orange)
        setSpannableText(textSwitch, "Press to START the pump", "START", orangeColor)


        // Clear timer state
        with(sharedPreferences.edit()) {
            putLong(TIMER_KEY, 0)
            apply()

            // Verify if the timer is removed
            println("TIMER1 REMOVED " + sharedPreferences.getLong(TIMER_KEY, 0L))
        }


        // Send "OFF" as JSON object to the topic
        val json = JSONObject()
        json.put("Pump", "OFF")
        mqttHandler.publish(topic, json.toString())
    }

    override fun onBackPressed() {
        // Check if the switch is on
        if (switchButton.isChecked) {
            // Show a message or do nothing to prevent going back
            Toast.makeText(this, "Please stop the pump before going back", Toast.LENGTH_SHORT).show()
        } else {
            // Call the super method to handle the back press normally
            super.onBackPressed()
        }
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