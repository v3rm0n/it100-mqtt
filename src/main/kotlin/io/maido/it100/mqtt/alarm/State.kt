package io.maido.it100.mqtt.alarm

enum class State {
    ARMED_HOME,
    ARMED_AWAY,
    PENDING,
    DISARMED,
    TRIGGERED;

    val bytes: ByteArray = name.toLowerCase().toByteArray()
}
