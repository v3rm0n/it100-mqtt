package info.kaara.it100.mqtt

import com.github.kmbulebu.dsc.it100.IT100
import com.github.kmbulebu.dsc.it100.commands.write.PartitionArmAwayCommand
import com.github.kmbulebu.dsc.it100.commands.write.PartitionArmStayCommand
import com.github.kmbulebu.dsc.it100.commands.write.PartitionDisarmCommand
import info.kaara.it100.mqtt.alarm.Command
import info.kaara.it100.mqtt.alarm.MqttAlarm
import info.kaara.it100.mqtt.alarm.MqttAlarmCommandListener
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty("mqtt.enabled")
@EnableConfigurationProperties(IT100MqttProperties::class)
open class IT100MqttConfiguration(val mqttProperties: IT100MqttProperties) {

    companion object {
        private val log = LoggerFactory.getLogger(IT100MqttConfiguration::class.java)
    }

    @Bean
    open fun it100MqttPublisher(it100: IT100): IT100MqttPublisher {
        log.info("IT100 MQTT publisher is enabled")
        val mqttAlarmCommandListener: MqttAlarmCommandListener = object : MqttAlarmCommandListener {
            override fun onCommand(command: Command) {
                log.info("Command {}", command)
                when (command) {
                //Currently supports only one partition
                    Command.ARM_HOME -> it100.send(PartitionArmStayCommand(1))
                    Command.ARM_AWAY -> it100.send(PartitionArmAwayCommand(1))
                    Command.DISARM -> it100.send(PartitionDisarmCommand(1, mqttProperties.code))
                }
            }

        }
        val mqttAlarm: MqttAlarm = MqttAlarm(mqttProperties.broker, mqttProperties.username, mqttProperties.password.toCharArray(), mqttProperties.qos, mqttAlarmCommandListener)
        return IT100MqttPublisher(it100, mqttAlarm)
    }
}