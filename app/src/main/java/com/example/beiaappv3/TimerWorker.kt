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
        val durationMillis = inputData.getLong("DURATION_MILLIS", 0L)

        try {
            val mqttClient = MqttClient(brokerUrl, clientId, null)
            mqttClient.connect()
            val json = JSONObject().put("Pump", "OFF")
            mqttClient.publish(topic, json.toString().toByteArray(), 0, false)
            mqttClient.disconnect()
            return Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            return Result.failure()
        }
    }
}