package info.kaara.it100.serial

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties("it100")
open class IT100Properties {
    lateinit var serialPort: String
    var baudRate: Int = 115200
    var serial: Boolean = false
    lateinit var host: String
    var port: Int = 2000
}