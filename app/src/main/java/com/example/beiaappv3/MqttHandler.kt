package com.example.beiaappv3

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence

class MqttHandler(private val messageListener: (String) -> Unit) : MqttCallback {

    private var client: MqttClient? = null

    fun connect(brokerUrl: String, clientId: String) {
        try {
            // Set up the persistence layer
            val persistence = MemoryPersistence()

            // Initialize the MQTT client
            client = MqttClient(brokerUrl, clientId, persistence)
            client?.setCallback(this)

            // Set up the connection options
            val connectOptions = MqttConnectOptions()
            connectOptions.isCleanSession = true

            // Connect to the broker
            client?.connect(connectOptions)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun disconnect() {
        try {
            client?.disconnect()
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun publish(topic: String, message: String) {
        try {
            val mqttMessage = MqttMessage(message.toByteArray())
            client?.publish(topic, mqttMessage)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    fun subscribe(topic: String) {
        try {
            client?.subscribe(topic)
        } catch (e: MqttException) {
            e.printStackTrace()
        }
    }

    override fun connectionLost(cause: Throwable?) {
        cause?.printStackTrace()
    }

    override fun messageArrived(topic: String?, message: MqttMessage?) {
        message?.let {
            messageListener(it.toString())
        }
    }

    override fun deliveryComplete(token: IMqttDeliveryToken?) {
        // Not needed for subscribing
    }
}
