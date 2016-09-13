package info.kaara.it100.panel;

import com.github.kmbulebu.dsc.it100.IT100
import org.springframework.stereotype.Component
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Component
open class WebSocketConfig(val it100: IT100) : WebSocketConfigurer {

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(IT100WebSocketHandler(it100), "/it100");
    }

}
