package info.kaara.it100.mqtt;

public enum State {
	ARMED,
	ARMED_HOME,
	ARMED_AWAY,
	PENDING,
	TRIGGERED;

	public byte[] getBytes() {
		return name().toLowerCase().getBytes();
	}
}
