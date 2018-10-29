package io.maido.it100.mqtt

import com.github.kmbulebu.dsc.it100.IT100
import com.github.kmbulebu.dsc.it100.commands.write.PartitionArmAwayCommand
import com.github.kmbulebu.dsc.it100.commands.write.PartitionArmStayCommand
import com.github.kmbulebu.dsc.it100.commands.write.PartitionDisarmCommand
import io.maido.it100.mqtt.alarm.Command
import io.maido.it100.mqtt.alarm.MqttAlarm
import io.maido.it100.mqtt.alarm.MqttAlarmCommandListener
import org.slf4j.LoggerFactory
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
@ConditionalOnProperty("mqtt.enabled")
@ConfigurationProperties("mqtt")
class IT100MqttConfiguration {

  companion object {
    private val log = LoggerFactory.getLogger(IT100MqttConfiguration::class.java)
  }

  lateinit var broker: String
  lateinit var username: String
  lateinit var password: String
  var qos: Int = 0
  lateinit var code: String

  @Bean
  fun it100MqttPublisher(it100: IT100): IT100MqttPublisher {
    log.info("IT100 MQTT publisher is enabled")
    val mqttAlarmCommandListener: MqttAlarmCommandListener = object : MqttAlarmCommandListener {
      override fun onCommand(command: Command) {
        log.info("Command {}", command)
        when (command) {
          //Currently supports only one partition
          Command.ARM_HOME -> it100.send(PartitionArmStayCommand(1))
          Command.ARM_AWAY -> it100.send(PartitionArmAwayCommand(1))
          Command.DISARM -> it100.send(PartitionDisarmCommand(1, code))
        }
      }
    }
    val mqttAlarm = MqttAlarm(broker, username, password.toCharArray(), qos, mqttAlarmCommandListener)
    return IT100MqttPublisher(it100, mqttAlarm)
  }
}

