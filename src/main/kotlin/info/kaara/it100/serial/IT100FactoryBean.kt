package info.kaara.it100.serial

import com.github.kmbulebu.dsc.it100.ConfigurationBuilder
import com.github.kmbulebu.dsc.it100.IT100
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.config.AbstractFactoryBean
import org.springframework.context.annotation.Profile
import org.springframework.core.Ordered.HIGHEST_PRECEDENCE
import org.springframework.stereotype.Component
import javax.annotation.Priority

@Component
@Profile("!mock")
@Priority(HIGHEST_PRECEDENCE)
open class IT100FactoryBean : AbstractFactoryBean<IT100>() {

    @Autowired
    lateinit var properties: IT100Properties

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
        if (properties.serial) {
            log.info("Using serial port {}", properties.serialPort)
            conf.withSerialPort(properties.serialPort, properties.baudRate)
        } else {
            log.info("Using remote socket on host {}", properties.host)
            conf.withRemoteSocket(properties.host, properties.port)
        }
        val it100: IT100 = IT100(conf.build())
        log.info("Connecting to IT100")
        it100.connect()
        return it100
    }

}