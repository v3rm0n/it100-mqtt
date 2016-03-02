package info.kaara.it100;

import com.github.kmbulebu.dsc.it100.ConfigurationBuilder;
import com.github.kmbulebu.dsc.it100.IT100;
import com.github.kmbulebu.dsc.it100.commands.read.*;
import com.github.kmbulebu.dsc.it100.commands.write.PartitionArmAwayCommand;
import com.github.kmbulebu.dsc.it100.commands.write.PartitionArmStayCommand;
import com.github.kmbulebu.dsc.it100.commands.write.PartitionDisarmCommand;
import com.github.kmbulebu.dsc.it100.commands.write.StatusRequestCommand;
import info.kaara.it100.mqtt.MqttAlarm;
import info.kaara.it100.mqtt.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rx.Observable;

public class App {

	private static final Logger log = LoggerFactory.getLogger(App.class);

	public static void main(String... args) throws Exception {

		String alarmCode = args[0];
		String broker = args[1];
		log.info("Connecting to broker {}", broker);
		MqttAlarm mqttAlarm = new MqttAlarm(broker, args[2], args[3].toCharArray());
		mqttAlarm.subscribeCommand(command -> log.info("Command {}", command));
		final IT100 it100 = new IT100(new ConfigurationBuilder().withSerialPort("/dev/ttyUSB0", 115200).build());
		it100.connect();

		mqttAlarm.subscribeCommand(command -> {
			log.info("Command {}", command);
			switch (command) {
				case ARM_HOME:
					it100.send(new PartitionArmStayCommand(1));
					break;
				case ARM_AWAY:
					it100.send(new PartitionArmAwayCommand(1));
					break;
				case DISARM:
					it100.send(new PartitionDisarmCommand(1, alarmCode));
			}
		});

		final Observable<ReadCommand> readObservable = it100.getReadObservable();

		readObservable.ofType(PartitionArmedCommand.class).subscribe(partitionArmedCommand -> {
			log.info("Partition is armed {}", partitionArmedCommand);
			switch (partitionArmedCommand.getMode()) {
				case AWAY:
				case AWAY_NO_DELAY:
					mqttAlarm.publishStateChange(State.ARMED_AWAY);
					break;
				case STAY:
				case STAY_NO_DELAY:
					mqttAlarm.publishStateChange(State.ARMED_HOME);
			}
		});

		readObservable.ofType(PartitionDisarmedCommand.class).subscribe(partitionDisarmedCommand -> {
			log.info("Partition is disarmed {}", partitionDisarmedCommand);
			mqttAlarm.publishStateChange(State.DISARMED);
		});

		readObservable.ofType(PartitionReadyCommand.class).subscribe(partitionReadyCommand -> {
			log.info("Partition is ready {}", partitionReadyCommand);
			mqttAlarm.publishStateChange(State.DISARMED);
		});

		readObservable.ofType(PartitionNotReadyCommand.class).subscribe(partitionNotReadyCommand -> {
			log.info("Partition is not ready {}", partitionNotReadyCommand);
			mqttAlarm.publishStateChange(State.PENDING);
		});

		readObservable.ofType(PartitionInAlarmCommand.class).subscribe(partitionInAlarmCommand -> {
			log.info("Partition is triggered {}", partitionInAlarmCommand);
			mqttAlarm.publishStateChange(State.TRIGGERED);
		});


		//Get initial status
		it100.send(new StatusRequestCommand());

		log.info("Initialized");

	}
}
