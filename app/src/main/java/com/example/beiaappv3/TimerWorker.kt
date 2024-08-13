package com.example.beiaappv3
import android.content.Context
import androidx.work.Worker
import androidx.work.WorkerParameters
import org.eclipse.paho.client.mqttv3.MqttClient
import org.json.JSONObject

class TimerWorker(context: Context, workerParams: WorkerParameters) : Worker(context, workerParams) {

    private val brokerUrl = "tcp://mqtt.beia-telemetrie.ro:1883"
    private val clientId = MqttClient.generateClientId()
    private val topic = "meshlium3d4c/Gabi/TC"

    override fun doWork(): Result {
        val remainingMillis = inputData.getLong("REMAINING_MILLIS", 0L)

        if (remainingMillis == 0L) {
            turnOffPump()
        }
        return Result.success()

    }

    private fun turnOffPump() {
        try {
            val mqttClient = MqttClient(brokerUrl, clientId, null)
            mqttClient.connect()
            val json = JSONObject().put("Pump", "OFF")
            mqttClient.publish(topic, json.toString().toByteArray(), 0, false)
            mqttClient.disconnect()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
