package dev.mlzzen.backend.dto

import dev.mlzzen.backend.entity.MessageType
import java.time.LocalDateTime

data class MessageDto(
    val id: Long,
    val senderId: Long,
    val receiverId: Long,
    val senderUsername: String,
    val receiverUsername: String,
    val content: String,
    val messageType: MessageType,
    val isRead: Boolean,
    val createdAt: LocalDateTime
)

data class CreateMessageDto(
    val receiverId: Long,
    val content: String,
    val messageType: MessageType = MessageType.TEXT
)

data class ChatHistoryDto(
    val friendId: Long,
    val friendUsername: String,
    val friendAvatarUrl: String?,
    val lastMessage: MessageDto?,
    val unreadCount: Int
)

data class ChatListDto(
    val chats: List<ChatHistoryDto>
)
