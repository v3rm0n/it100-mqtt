package io.maido.it100.mqtt

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken
import org.eclipse.paho.client.mqttv3.MqttCallback
import org.eclipse.paho.client.mqttv3.MqttClient
import org.eclipse.paho.client.mqttv3.MqttConnectOptions
import org.eclipse.paho.client.mqttv3.MqttException
import org.eclipse.paho.client.mqttv3.MqttMessage
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.slf4j.LoggerFactory
import java.util.UUID

enum class Topic(val topic: String) {

  STATE("home/alarm"),
  AVAILABILITY("home/alarm/availability"),
  COMMAND("home/alarm/set");

  companion object {
    fun toTopic(topic: String): Topic {
      return values().first { t -> t.topic == topic }
    }
  }
}

enum class State {
  ARMED_HOME,
  ARMED_AWAY,
  PENDING,
  DISARMED,
  TRIGGERED;

  val bytes: ByteArray = name.toLowerCase().toByteArray()
}

enum class Command {
  DISARM,
  ARM_HOME,
  ARM_AWAY
}

enum class Availability {
  ONLINE,
  OFFLINE;

  val bytes: ByteArray = name.toLowerCase().toByteArray()
}

class MqttAlarm constructor(val broker: String, val username: String, val password: CharArray, val qos: Int, val mqttAlarmCommandListener: MqttAlarmCommandListener) {

  companion object {
    private val log = LoggerFactory.getLogger(MqttAlarm::class.java)
  }

  private val mqttClient: MqttClient

  init {
    this.mqttClient = createMqttClient()
    mqttClient.setCallback(object : MqttCallback {
      override fun connectionLost(cause: Throwable) {
        log.error("Connection lost", cause)
      }

      @Throws(Exception::class)
      override fun messageArrived(t: String, message: MqttMessage) {
        if (Topic.COMMAND == Topic.toTopic(t)) {
          mqttAlarmCommandListener.onCommand(Command.valueOf(message.toString().toUpperCase()))
        }
      }

      override fun deliveryComplete(token: IMqttDeliveryToken) {
        log.info("Delivery complete {}", token)
      }
    })
    mqttClient.subscribe(Topic.COMMAND.topic, qos)
  }

  fun publishStateChange(state: State) {
    val message = MqttMessage(state.bytes)
    message.qos = qos
    message.isRetained = true
    try {
      mqttClient.publish(Topic.STATE.topic, message)
    } catch (e: MqttException) {
      throw RuntimeException(e)
    }
  }

  @Throws(MqttException::class)
  fun disconnect() {
    mqttClient.disconnect()
  }

  @Throws(MqttException::class)
  private fun createMqttClient(): MqttClient {
    val persistence = MemoryPersistence()
    val mqttClient = MqttClient(broker, UUID.randomUUID().toString(), persistence)
    val connOpts = MqttConnectOptions()
    connOpts.userName = username
    connOpts.password = password
    connOpts.isCleanSession = true
    connOpts.setWill(Topic.AVAILABILITY.topic, Availability.OFFLINE.bytes, qos, true)
    mqttClient.connect(connOpts)
    mqttClient.publish(Topic.AVAILABILITY.topic, Availability.ONLINE.bytes, qos, true)
    return mqttClient
  }
}

interface MqttAlarmCommandListener {
  fun onCommand(command: Command)
}
