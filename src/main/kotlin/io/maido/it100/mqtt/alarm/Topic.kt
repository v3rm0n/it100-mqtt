package io.maido.it100.mqtt.alarm

enum class Topic(val topic: String) {

  STATE("home/alarm"),
  COMMAND("home/alarm/set"),
  OTHER("home/alarm/other"),
  AVAILABILITY("home/alarm/availability");

  companion object {
    fun toTopic(topic: String): Topic {
      return values().first { t -> t.topic == topic }
    }
  }
}
