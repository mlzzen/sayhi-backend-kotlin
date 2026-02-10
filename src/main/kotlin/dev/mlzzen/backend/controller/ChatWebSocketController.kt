package dev.mlzzen.backend.controller

import dev.mlzzen.backend.dto.MessageDto
import dev.mlzzen.backend.entity.MessageType
import dev.mlzzen.backend.security.JwtUtil
import dev.mlzzen.backend.service.MessageService
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Controller
import java.security.Principal

@Controller
class ChatWebSocketController(
    private val messageService: MessageService,
    private val jwtUtil: JwtUtil
) {

    // Handle chat messages
    @MessageMapping("/chat/{userId}")
    @SendTo("/topic/messages/{userId}")
    fun handleMessage(
        @DestinationVariable userId: Long,
        payload: Map<String, Any>,
        principal: Principal?
    ): MessageDto {
        val currentUserId = principal?.name?.toLongOrNull()
            ?: throw IllegalArgumentException("User not authenticated")

        val receiverId = (payload["receiverId"] as Number).toLong()
        val content = payload["content"] as String
        val messageType = try {
            MessageType.valueOf((payload["messageType"] as? String) ?: "TEXT")
        } catch (e: Exception) {
            MessageType.TEXT
        }

        val dto = CreateWebSocketMessageDto(
            receiverId = receiverId,
            content = content,
            messageType = messageType
        )

        return messageService.sendMessage(currentUserId, dto)
    }

    // Subscribe to user's personal messages
    @SubscribeMapping("/messages/{userId}")
    fun subscribeMessages(@DestinationVariable userId: Long): Map<String, Any> {
        return mapOf("status" to "connected", "userId" to userId)
    }
}

data class CreateWebSocketMessageDto(
    val receiverId: Long,
    val content: String,
    val messageType: MessageType = MessageType.TEXT
)
