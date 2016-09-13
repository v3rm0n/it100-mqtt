package info.kaara.it100.mqtt.alarm

import com.github.kmbulebu.dsc.it100.commands.ICommand
import org.eclipse.paho.client.mqttv3.*
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence
import org.slf4j.LoggerFactory
import java.util.*

class MqttAlarm(val broker: String, val username: String, val password: CharArray, val qos: Int, val mqttAlarmCommandListener: MqttAlarmCommandListener) {

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

    fun publishCommand(command: ICommand) {
        publish(Topic.OTHER, "${command.commandCode} ${command.data}".toByteArray())
    }

    fun publishStateChange(state: State) {
        publish(Topic.STATE, state.bytes)
    }

    fun publish(topic: Topic, bytes: ByteArray) {
        val message = MqttMessage(bytes)
        message.qos = qos
        message.isRetained = true
        try {
            mqttClient.publish(topic.topic, message)
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
        mqttClient.connect(connOpts)
        return mqttClient
    }

}

