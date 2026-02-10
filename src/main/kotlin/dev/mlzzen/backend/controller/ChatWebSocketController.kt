package dev.mlzzen.backend.controller

import dev.mlzzen.backend.dto.CreateMessageDto
import dev.mlzzen.backend.dto.MessageDto
import dev.mlzzen.backend.entity.MessageType
import dev.mlzzen.backend.service.MessageService
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.messaging.simp.annotation.SubscribeMapping
import org.springframework.messaging.simp.stomp.StompHeaderAccessor
import org.springframework.stereotype.Controller

@Controller
class ChatWebSocketController(
    private val messageService: MessageService
) {

    // Handle chat messages
    @MessageMapping("/chat/{userId}")
    @SendTo("/topic/messages/{userId}")
    fun handleMessage(
        @DestinationVariable userId: Long,
        payload: Map<String, Any>,
        accessor: StompHeaderAccessor
    ): MessageDto {
        val currentUserId = accessor.user?.name?.toLongOrNull()
            ?: throw IllegalArgumentException("User not authenticated")

        val receiverId = (payload["receiverId"] as Number).toLong()
        val content = payload["content"] as String
        val messageType = try {
            MessageType.valueOf((payload["messageType"] as? String) ?: "TEXT")
        } catch (e: Exception) {
            MessageType.TEXT
        }

        val dto = CreateMessageDto(
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
