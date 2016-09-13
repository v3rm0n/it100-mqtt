package info.kaara.it100.mqtt.alarm

enum class Topic(val topic: String) {

    STATE("home/alarm"),
    COMMAND("home/alarm/set"),
    OTHER("home/alarm/other");

    companion object {
        fun toTopic(topic: String): Topic {
            return values().filter { t -> t.topic.equals(topic) }.first()
        }
    }
}