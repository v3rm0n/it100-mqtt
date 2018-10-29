package io.maido.it100.panel

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketMessage

sealed class Message {

    companion object {
        val mapper = ObjectMapper().registerModule(KotlinModule())

        fun from(message: TextMessage): Button {
            return Companion.mapper.readValue(message.payload, Button::class.java)
        }
    }

    fun toMessage(): WebSocketMessage<String> {
        return TextMessage(Companion.mapper.writeValueAsString(this))
    }

    class LCDUpdate(val line: Int, val column: Int, val text: String) : Message()
    class LCDCursor(val line: Int, val column: Int, val cursor: String) : Message()
    class LEDStatus(val led: Int, val status: Int) : Message()
    class Button(val button: Char) : Message()
}

