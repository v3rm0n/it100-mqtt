package info.kaara.it100.mqtt.alarm

interface MqttAlarmCommandListener {
    fun onCommand(command: Command)
}