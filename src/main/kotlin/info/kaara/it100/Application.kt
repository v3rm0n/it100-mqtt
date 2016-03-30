package info.kaara.it100

import org.springframework.boot.SpringApplication
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.web.socket.config.annotation.EnableWebSocket

@SpringBootApplication
@EnableWebSocket
open class Application

fun main(args: Array<String>) {
    SpringApplication.run(Application::class.java, *args)
}
