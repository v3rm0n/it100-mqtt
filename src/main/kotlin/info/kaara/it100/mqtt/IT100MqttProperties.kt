package info.kaara.it100.mqtt;

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties("mqtt")
open class IT100MqttProperties {
    lateinit var broker: String
    lateinit var username: String
    lateinit var password: String
    var qos: Int = 0
    lateinit var code: String
}
