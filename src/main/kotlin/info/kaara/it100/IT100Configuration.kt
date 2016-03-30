package info.kaara.it100

import com.github.kmbulebu.dsc.it100.ConfigurationBuilder
import com.github.kmbulebu.dsc.it100.IT100
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.config.AbstractFactoryBean
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Profile
import org.springframework.core.Ordered
import org.springframework.stereotype.Component
import javax.annotation.Priority

@Component
@Profile("!mock")
@Priority(Ordered.HIGHEST_PRECEDENCE)
@ConfigurationProperties("it100")
open class IT100FactoryBean : AbstractFactoryBean<IT100>() {

    lateinit var serialPort: String
    var baudRate: Int = 115200
    var serial: Boolean = false
    lateinit var host: String
    var port: Int = 2000

    companion object {
        private val log = LoggerFactory.getLogger(IT100FactoryBean::class.java)
    }

    @Throws(Exception::class)
    override fun destroyInstance(it100: IT100) {
        log.info("Disconnecting from IT100")
        it100.disconnect()
    }

    override fun getObjectType(): Class<*> {
        return IT100::class.java
    }

    override fun createInstance(): IT100 {
        val conf: ConfigurationBuilder = ConfigurationBuilder()
        if (serial) {
            log.info("Using serial port {}", serialPort)
            conf.withSerialPort(serialPort, baudRate)
        } else {
            log.info("Using remote socket on host {}", host)
            conf.withRemoteSocket(host, port)
        }
        val it100: IT100 = IT100(conf.build())
        log.info("Connecting to IT100")
        it100.connect()
        return it100
    }

}