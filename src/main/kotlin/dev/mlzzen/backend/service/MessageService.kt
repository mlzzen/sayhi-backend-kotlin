package dev.mlzzen.backend.service

import dev.mlzzen.backend.dto.ChatHistoryDto
import dev.mlzzen.backend.dto.CreateMessageDto
import dev.mlzzen.backend.dto.MessageDto
import dev.mlzzen.backend.entity.Message
import dev.mlzzen.backend.entity.User
import dev.mlzzen.backend.repository.MessageRepository
import dev.mlzzen.backend.repository.UserRepository
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class MessageService(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository
) {

    // Send a message
    @Transactional
    fun sendMessage(senderId: Long, dto: CreateMessageDto): MessageDto {
        val sender = userRepository.findById(senderId)
            .orElseThrow { IllegalArgumentException("User not found") }
        val receiver = userRepository.findById(dto.receiverId)
            .orElseThrow { IllegalArgumentException("Receiver not found") }

        val message = Message(
            sender = sender,
            receiver = receiver,
            content = dto.content,
            messageType = dto.messageType
        )
        val saved = messageRepository.save(message)

        return toMessageDto(saved)
    }

    // Get chat history with a user
    @Transactional(readOnly = true)
    fun getChatHistory(userId: Long, otherUserId: Long, page: Int = 0, size: Int = 50): List<MessageDto> {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }
        val otherUser = userRepository.findById(otherUserId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val pageable: Pageable = PageRequest.of(page, size)
        val messages = messageRepository.findMessagesBetweenUsers(user, otherUser, pageable)
        return messages.reversed().map { toMessageDto(it) }
    }

    // Get chat list with last message from each friend
    @Transactional(readOnly = true)
    fun getChatList(userId: Long): List<ChatHistoryDto> {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val latestMessages = messageRepository.findLatestMessagesByUser(user)

        // Group by conversation partner
        val chatMap = mutableMapOf<Long, MutableList<Message>>()
        latestMessages.forEach { message ->
            val partnerId = if (message.sender.id == userId) message.receiver.id else message.sender.id
            chatMap.getOrPut(partnerId) { mutableListOf() }.add(message)
        }

        return chatMap.map { (partnerId, messages) ->
            val partner = userRepository.findById(partnerId).orElse(null)
            val lastMessage = messages.firstOrNull()
            val unreadCount = messageRepository.countUnreadFrom(user, partner!!).toInt()

            ChatHistoryDto(
                friendId = partnerId,
                friendUsername = partner!!.username,
                friendAvatarUrl = partner.avatarUrl,
                lastMessage = lastMessage?.let { toMessageDto(it) },
                unreadCount = unreadCount
            )
        }.sortedByDescending { it.lastMessage?.createdAt }
    }

    // Mark messages as read
    @Transactional
    fun markAsRead(userId: Long, fromUserId: Long) {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }
        val fromUser = userRepository.findById(fromUserId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val messages = messageRepository.findMessagesBetweenUsers(user, fromUser, PageRequest.of(0, Int.MAX_VALUE))
        messages.filter { !it.isRead && it.receiver.id == userId }
            .forEach { it.isRead = true }
        messageRepository.saveAll(messages)
    }

    private fun toMessageDto(message: Message): MessageDto {
        return MessageDto(
            id = message.id,
            senderId = message.sender.id,
            receiverId = message.receiver.id,
            senderUsername = message.sender.username,
            receiverUsername = message.receiver.username,
            content = message.content,
            messageType = message.messageType,
            isRead = message.isRead,
            createdAt = message.createdAt
        )
    }
}
