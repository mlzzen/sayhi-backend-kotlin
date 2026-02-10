package dev.mlzzen.backend.listener

import dev.mlzzen.backend.service.OnlineStatusService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.context.event.EventListener
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Component
import org.springframework.web.socket.messaging.SessionConnectedEvent
import org.springframework.web.socket.messaging.SessionDisconnectEvent

@Component
class WebSocketEventListener(
    private val onlineStatusService: OnlineStatusService
) {
    private val logger: Logger = LoggerFactory.getLogger(WebSocketEventListener::class.java)

    @EventListener
    fun handleWebSocketConnectListener(event: SessionConnectedEvent) {
        val accessor = StompHeaderAccessor.getAccessor(event.message, StompHeaderAccessor::class.java)
        val userId = accessor?.user?.name

        if (userId != null) {
            onlineStatusService.setOnline(userId.toLong())
            logger.info("User connected: $userId")
        }
    }

    @EventListener
    fun handleWebSocketDisconnectListener(event: SessionDisconnectEvent) {
        val accessor = StompHeaderAccessor.getAccessor(event.message, StompHeaderAccessor::class.java)
        val userId = accessor?.user?.name

        if (userId != null) {
            onlineStatusService.setOffline(userId.toLong())
            logger.info("User disconnected: $userId")
        }
    }
}
