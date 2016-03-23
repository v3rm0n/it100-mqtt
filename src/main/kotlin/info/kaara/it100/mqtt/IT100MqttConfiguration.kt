package info.kaara.it100.mqtt

import com.github.kmbulebu.dsc.it100.IT100
import com.github.kmbulebu.dsc.it100.commands.read.*
import com.github.kmbulebu.dsc.it100.commands.write.PartitionArmAwayCommand
import com.github.kmbulebu.dsc.it100.commands.write.PartitionArmStayCommand
import com.github.kmbulebu.dsc.it100.commands.write.PartitionDisarmCommand
import com.github.kmbulebu.dsc.it100.commands.write.StatusRequestCommand
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("mqtt")
class MqttConfig {
    lateinit var broker: String
    lateinit var username: String
    lateinit var password: String
    var qos: Int = 0
    lateinit var code: String
}

@Configuration
@ConditionalOnProperty("mqtt.enabled")
open class IT100MqttConfiguration {

    companion object {
        private val log = LoggerFactory.getLogger(IT100MqttConfiguration::class.java)
    }

    @Autowired
    lateinit var mqttConfig: MqttConfig

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
                    Command.DISARM -> it100.send(PartitionDisarmCommand(1, mqttConfig.code))
                }
            }

        }
        val mqttAlarm: MqttAlarm = MqttAlarm(mqttConfig.broker, mqttConfig.username, mqttConfig.password.toCharArray(), mqttConfig.qos, mqttAlarmCommandListener)
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

        readObservable.ofType(PartitionArmedCommand::class.java).subscribe { partitionArmedCommand ->
            log.info("Partition is armed {}", partitionArmedCommand)
            when (partitionArmedCommand.mode) {
                PartitionArmedCommand.ArmedMode.AWAY, PartitionArmedCommand.ArmedMode.AWAY_NO_DELAY -> mqttAlarm.publishStateChange(State.ARMED_AWAY)
                PartitionArmedCommand.ArmedMode.STAY, PartitionArmedCommand.ArmedMode.STAY_NO_DELAY -> mqttAlarm.publishStateChange(State.ARMED_HOME)
            }
        }

        readObservable.ofType(PartitionDisarmedCommand::class.java).subscribe { partitionDisarmedCommand ->
            log.info("Partition is disarmed {}", partitionDisarmedCommand)
            mqttAlarm.publishStateChange(State.DISARMED)
        }

        readObservable.ofType(PartitionInAlarmCommand::class.java).subscribe { partitionInAlarmCommand ->
            log.info("Partition is triggered {}", partitionInAlarmCommand)
            mqttAlarm.publishStateChange(State.TRIGGERED)
        }

        readObservable.ofType(ExitDelayInProgressCommand::class.java).subscribe { exitDelayInProgress ->
            log.info("Exit delay in progress {}", exitDelayInProgress)
            mqttAlarm.publishStateChange(State.PENDING)
        }

        readObservable.ofType(EntryDelayInProgressCommand::class.java).subscribe { entryDelayInProgress ->
            log.info("Entry delay in progress {}", entryDelayInProgress)
            mqttAlarm.publishStateChange(State.PENDING)
        }

        readObservable.subscribe { command ->
            log.debug("Got command {}", command);
        }

        log.info("Initialized")
    }
}