package io.maido.it100.mqtt

import com.github.kmbulebu.dsc.it100.IT100
import com.github.kmbulebu.dsc.it100.commands.read.EntryDelayInProgressCommand
import com.github.kmbulebu.dsc.it100.commands.read.ExitDelayInProgressCommand
import com.github.kmbulebu.dsc.it100.commands.read.PartitionArmedCommand
import com.github.kmbulebu.dsc.it100.commands.read.PartitionDisarmedCommand
import com.github.kmbulebu.dsc.it100.commands.read.PartitionInAlarmCommand
import io.maido.it100.mqtt.alarm.MqttAlarm
import io.maido.it100.mqtt.alarm.State
import org.slf4j.LoggerFactory

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
