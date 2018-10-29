package io.maido.it100.mqtt.alarm

enum class Availability {
  ONLINE,
  OFFLINE;

  val bytes: ByteArray = name.toLowerCase().toByteArray()
}
