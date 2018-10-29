package io.maido.it100.panel

import com.github.kmbulebu.dsc.it100.IT100
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry

@Component
class WebSocketConfig @Autowired constructor(val it100: IT100) : WebSocketConfigurer {

  override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
    registry.addHandler(IT100WebSocketHandler(it100), "/it100")
  }

}
