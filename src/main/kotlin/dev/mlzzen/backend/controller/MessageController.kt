package dev.mlzzen.backend.controller

import dev.mlzzen.backend.dto.ChatHistoryDto
import dev.mlzzen.backend.dto.CreateMessageDto
import dev.mlzzen.backend.dto.MessageDto
import dev.mlzzen.backend.security.JwtUtil
import dev.mlzzen.backend.service.MessageService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/messages")
class MessageController(
    private val messageService: MessageService,
    private val jwtUtil: JwtUtil
) {

    // Send a message
    @PostMapping
    fun sendMessage(
        @RequestHeader("Authorization") authHeader: String,
        @RequestBody dto: CreateMessageDto
    ): ResponseEntity<MessageDto> {
        val userId = extractUserId(authHeader)
        val message = messageService.sendMessage(userId, dto)
        return ResponseEntity.ok(message)
    }

    // Get chat history with a user
    @GetMapping("/history/{userId}")
    fun getChatHistory(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable userId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<List<MessageDto>> {
        val currentUserId = extractUserId(authHeader)
        val messages = messageService.getChatHistory(currentUserId, userId, page, size)
        return ResponseEntity.ok(messages)
    }

    // Get chat list
    @GetMapping
    fun getChatList(@RequestHeader("Authorization") authHeader: String): ResponseEntity<List<ChatHistoryDto>> {
        val userId = extractUserId(authHeader)
        val chats = messageService.getChatList(userId)
        return ResponseEntity.ok(chats)
    }

    // Mark messages as read
    @PutMapping("/read/{fromUserId}")
    fun markAsRead(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable fromUserId: Long
    ): ResponseEntity<Unit> {
        val userId = extractUserId(authHeader)
        messageService.markAsRead(userId, fromUserId)
        return ResponseEntity.ok().build()
    }

    private fun extractUserId(authHeader: String): Long {
        val token = authHeader.removePrefix("Bearer ")
        return jwtUtil.getUserIdFromToken(token)
            ?: throw IllegalArgumentException("Invalid token")
    }
}
