package dev.mlzzen.backend.service

import dev.mlzzen.backend.dto.MessageDto
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class MessageCacheService(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    companion object {
        private const val RECENT_MESSAGES_PREFIX = "chat:recent:"
        private const val MESSAGE_CACHE_DURATION = Duration.ofHours(24)
        private const val MAX_CACHED_MESSAGES = 100
    }

    // Cache recent messages between two users
    fun cacheRecentMessage(user1Id: Long, user2Id: Long, message: MessageDto) {
        val key = getChatKey(user1Id, user2Id)
        val list = redisTemplate.opsForList()

        // Add message to the beginning of the list
        list.leftPush(key, message)
        list.trim(key, 0, MAX_CACHED_MESSAGES.toLong() - 1)
        redisTemplate.expire(key, MESSAGE_CACHE_DURATION)
    }

    // Get cached recent messages
    fun getRecentMessages(user1Id: Long, user2Id: Long): List<MessageDto> {
        val key = getChatKey(user1Id, user2Id)
        val list = redisTemplate.opsForList()
        val messages = list.range(key, 0, MAX_CACHED_MESSAGES.toLong() - 1)
        return messages?.map { it as? MessageDto }?.filterNotNull()?.reversed() ?: emptyList()
    }

    // Invalidate cache for a chat
    fun invalidateCache(user1Id: Long, user2Id: Long) {
        val key = getChatKey(user1Id, user2Id)
        redisTemplate.delete(key)
    }

    private fun getChatKey(user1Id: Long, user2Id: Long): String {
        val sortedIds = listOf(user1Id, user2Id).sorted()
        return "$RECENT_MESSAGES_PREFIX${sortedIds[0]}:${sortedIds[1]}"
    }
}
