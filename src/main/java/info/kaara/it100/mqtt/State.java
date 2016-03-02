package info.kaara.it100.mqtt;

public enum State {
	ARMED_HOME,
	ARMED_AWAY,
	PENDING,
	DISARMED,
	TRIGGERED;

	public byte[] getBytes() {
		return name().toLowerCase().getBytes();
	}
}
