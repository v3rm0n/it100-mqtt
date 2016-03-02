package info.kaara.it100.mqtt;

import java.util.Arrays;

public enum Topic {

	STATE("home/alarm"),
	COMMAND("home/alarm/set");

	private final String topic;

	Topic(String topic) {
		this.topic = topic;
	}

	public String getTopic() {
		return topic;
	}

	public static Topic toTopic(String topic) {
		return Arrays.stream(values()).filter(topic1 -> topic1.topic.equalsIgnoreCase(topic)).findFirst().orElse(null);
	}


}
