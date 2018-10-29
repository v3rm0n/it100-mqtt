package io.maido.it100.mqtt

import com.github.kmbulebu.dsc.it100.IT100
import com.github.kmbulebu.dsc.it100.commands.read.EntryDelayInProgressCommand
import com.github.kmbulebu.dsc.it100.commands.read.ExitDelayInProgressCommand
import com.github.kmbulebu.dsc.it100.commands.read.PartitionArmedCommand
import com.github.kmbulebu.dsc.it100.commands.read.PartitionDisarmedCommand
import com.github.kmbulebu.dsc.it100.commands.read.PartitionInAlarmCommand
import com.github.kmbulebu.dsc.it100.commands.write.PartitionArmAwayCommand
import com.github.kmbulebu.dsc.it100.commands.write.PartitionArmStayCommand
import com.github.kmbulebu.dsc.it100.commands.write.PartitionDisarmCommand
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

@Suppress("WHEN_ENUM_CAN_BE_NULL_IN_JAVA")
class IT100MqttPublisher(val it100: IT100, val mqttAlarm: MqttAlarm) {

  companion object {
    private val log = LoggerFactory.getLogger(IT100MqttPublisher::class.java)
  }

  init {
    val readObservable = it100.readObservable

    //Assume disarmed state on startup because there is no command to get current status
    mqttAlarm.publishStateChange(State.DISARMED)

    readObservable.subscribe { command ->
      log.debug("Command: {}", command)
      when (command) {
        is PartitionArmedCommand -> {
          when (command.mode) {
            PartitionArmedCommand.ArmedMode.AWAY, PartitionArmedCommand.ArmedMode.AWAY_NO_DELAY -> mqttAlarm.publishStateChange(State.ARMED_AWAY)
            PartitionArmedCommand.ArmedMode.STAY, PartitionArmedCommand.ArmedMode.STAY_NO_DELAY -> mqttAlarm.publishStateChange(State.ARMED_HOME)
          }
        }
        is PartitionDisarmedCommand -> {
          mqttAlarm.publishStateChange(State.DISARMED)
        }
        is PartitionInAlarmCommand -> {
          mqttAlarm.publishStateChange(State.TRIGGERED)
        }
        is ExitDelayInProgressCommand, is EntryDelayInProgressCommand -> {
          mqttAlarm.publishStateChange(State.PENDING)
        }
      }
    }
    log.info("Initialized")
  }
}
