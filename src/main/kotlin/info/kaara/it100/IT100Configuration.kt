package info.kaara.it100

import com.github.kmbulebu.dsc.it100.ConfigurationBuilder
import com.github.kmbulebu.dsc.it100.IT100
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.context.annotation.Profile
import org.springframework.stereotype.Component
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
@ConfigurationProperties("it100")
open class IT100Conf {
    lateinit var serialPort: String
    var baudRate: Int = 115200
    var serial: Boolean = false
    lateinit var host: String
    var port: Int = 2000
}

@Configuration
@Profile("!mock")
open class IT100Configuration {

    companion object {
        private val log = LoggerFactory.getLogger(IT100Configuration::class.java)
    }

    @Autowired
    lateinit var it100Conf: IT100Conf

    @Autowired
    lateinit var it100: IT100

    @PostConstruct
    fun init() {
        log.info("Connecting to IT100")
        it100.connect()
    }

    @Bean
    open fun it100(): IT100 {
        val conf: ConfigurationBuilder = ConfigurationBuilder()
        if (it100Conf.serial) {
            log.info("Using serial port {}", it100Conf.serialPort)
            conf.withSerialPort(it100Conf.serialPort, it100Conf.baudRate)
        } else {
            log.info("Using remote socket on host {}", it100Conf.host)
            conf.withRemoteSocket(it100Conf.host, it100Conf.port)
        }
        val it100: IT100 = IT100(conf.build())
        return it100
    }

    @PreDestroy
    fun destroy() {
        log.info("Disconnecting from IT100")
        it100.disconnect()
    }
}