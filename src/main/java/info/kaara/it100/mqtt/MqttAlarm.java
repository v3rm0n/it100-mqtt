package info.kaara.it100.mqtt;

import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class MqttAlarm {

	private static final Logger log = LoggerFactory.getLogger(MqttAlarm.class);

	private int qos = 0;
	private MqttClient mqttClient;
	private MqttAlarmCommandListener mqttAlarmCommandListener;

	public MqttAlarm(String broker, String username, char[] password) throws MqttException {
		this.mqttClient = createMqttClient(broker, username, password);
		mqttClient.setCallback(new MqttCallback() {
			@Override
			public void connectionLost(Throwable cause) {
				log.error("Connection lost", cause);
			}

			@Override
			public void messageArrived(String t, MqttMessage message) throws Exception {
				Topic topic = Topic.toTopic(t);
				if (Topic.COMMAND.equals(topic) && mqttAlarmCommandListener != null) {
					mqttAlarmCommandListener.onCommand(Command.valueOf(message.toString().toUpperCase()));
				}
			}

			@Override
			public void deliveryComplete(IMqttDeliveryToken token) {
				log.info("Delivery complete {}", token);
			}
		});
		mqttClient.subscribe(Topic.COMMAND.getTopic(), qos);
	}

	public void publishStateChange(State state) {
		MqttMessage message = new MqttMessage(state.getBytes());
		message.setQos(qos);
		try {
			mqttClient.publish(Topic.STATE.getTopic(), message);
		} catch (MqttException e) {
			throw new RuntimeException(e);
		}
	}

	public void subscribeCommand(MqttAlarmCommandListener listener) {
		this.mqttAlarmCommandListener = listener;
	}

	public void disconnect() throws MqttException {
		mqttClient.disconnect();
	}

	private MqttClient createMqttClient(String broker, String username, char[] password) throws MqttException {
		MemoryPersistence persistence = new MemoryPersistence();
		MqttClient mqttClient = new MqttClient(broker, UUID.randomUUID().toString(), persistence);
		MqttConnectOptions connOpts = new MqttConnectOptions();
		connOpts.setUserName(username);
		connOpts.setPassword(password);
		connOpts.setCleanSession(true);
		mqttClient.connect(connOpts);
		return mqttClient;
	}
}
