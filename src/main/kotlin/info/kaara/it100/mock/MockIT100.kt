package info.kaara.it100.mock

import com.github.kmbulebu.dsc.it100.IT100
import com.github.kmbulebu.dsc.it100.commands.read.*
import com.github.kmbulebu.dsc.it100.commands.util.CommandChecksum
import com.github.kmbulebu.dsc.it100.commands.write.WriteCommand
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Profile
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import rx.Observable
import rx.Observer
import rx.observables.ConnectableObservable
import rx.subjects.PublishSubject
import java.util.*
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import javax.annotation.Priority

@Component
@Profile("mock")
@Priority(Ordered.HIGHEST_PRECEDENCE)
class MockIT100 : IT100(null) {

  companion object {
    private val log = LoggerFactory.getLogger(MockIT100::class.java)
  }

  val w: PublishSubject<WriteCommand> = PublishSubject.create<WriteCommand>()

  private fun randomNumber(min: Int, max: Int): Int {
    if (min >= max) {
      throw IllegalArgumentException("max must be greater than min")
    }
    return Random().nextInt((max - min) + 1) + min
  }

  val executor = Executors.newSingleThreadScheduledExecutor()

  private fun sendRandomLEDUpdates(observer: Observer<in ReadCommand>) {
    val factory = ReadCommandFactory()
    executor.scheduleAtFixedRate({
      val data = "${randomNumber(1, 5)}${randomNumber(0, 2)}"
      val checksum = CommandChecksum.calculateChecksum(LEDStatusCommand.CODE, data)
      observer.onNext(factory.parseCommand("${LEDStatusCommand.CODE}$data$checksum"))
      log.info("Data sent {}", data)
    }, 1, 2, TimeUnit.SECONDS)
  }

  private fun sendRandomZoneOpenUpdates(observer: Observer<in ReadCommand>) {
    val factory = ReadCommandFactory()
    executor.scheduleAtFixedRate({
      val data = "00${randomNumber(1, 8)}"
      val checksum = CommandChecksum.calculateChecksum(ZoneOpenCommand.CODE, data)
      observer.onNext(factory.parseCommand("${ZoneOpenCommand.CODE}$data$checksum"))
      log.info("Data sent {}", data)
    }, 1, 2, TimeUnit.SECONDS)
  }

  private fun sendRandomZoneRestoreUpdates(observer: Observer<in ReadCommand>) {
    val factory = ReadCommandFactory()
    executor.scheduleAtFixedRate({
      val data = "00${randomNumber(1, 8)}"
      val checksum = CommandChecksum.calculateChecksum(ZoneRestoredCommand.CODE, data)
      observer.onNext(factory.parseCommand("${ZoneRestoredCommand.CODE}$data$checksum"))
      log.info("Data sent {}", data)
    }, 1, 2, TimeUnit.SECONDS)
  }

  private fun sendRandomDisplayText(observer: Observer<in ReadCommand>) {
    val factory = ReadCommandFactory()
    executor.scheduleAtFixedRate({
      val column = randomNumber(0, 15)
      val text = "Test m ${Runtime.getRuntime().freeMemory()}"
      val data = "${randomNumber(0, 1)}${if (column > 9) column.toString() else "0" + column}${text.length}$text"
      val checksum = CommandChecksum.calculateChecksum(LCDUpdateCommand.CODE, data)
      observer.onNext(factory.parseCommand("${LCDUpdateCommand.CODE}$data$checksum"))
      log.info("Data sent {}", data)
    }, 1, 5, TimeUnit.SECONDS)
  }

  val r: ConnectableObservable<ReadCommand>

  init {
    r = Observable.create(Observable.OnSubscribe<ReadCommand> { observer ->
      try {
        if (!observer.isUnsubscribed) {
          sendRandomLEDUpdates(observer)
          sendRandomDisplayText(observer)
          sendRandomZoneOpenUpdates(observer)
          sendRandomZoneRestoreUpdates(observer)
        }
      } catch (e: Exception) {
        observer.onError(e)
      }
    }).publish()
    r.connect()
  }

  override fun connect() {
    log.info("Connected")
  }

  override fun disconnect() {
    log.info("Disconnected")
  }

  override fun getReadObservable(): Observable<ReadCommand> {
    return r
  }

  override fun getWriteObservable(): PublishSubject<WriteCommand>? {
    return w
  }

  override fun send(command: WriteCommand) {
    log.info("Command sent to IT100 {}", command)
  }

}
