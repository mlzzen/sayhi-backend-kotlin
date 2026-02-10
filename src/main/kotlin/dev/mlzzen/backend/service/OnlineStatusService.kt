package dev.mlzzen.backend.service

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class OnlineStatusService(
    private val redisTemplate: RedisTemplate<String, Any>
) {
    companion object {
        private const val ONLINE_KEY_PREFIX = "user:online:"
        private const val LAST_SEEN_KEY_PREFIX = "user:lastseen:"
        private const val ONLINE_DURATION = Duration.ofMinutes(5)
    }

    // Set user as online
    fun setOnline(userId: Long) {
        val key = "$ONLINE_KEY_PREFIX$userId"
        redisTemplate.opsForValue().set(key, "1", ONLINE_DURATION)
        redisTemplate.opsForValue().set("$LAST_SEEN_KEY_PREFIX$userId", System.currentTimeMillis().toString())
    }

    // Set user as offline
    fun setOffline(userId: Long) {
        val key = "$ONLINE_KEY_PREFIX$userId"
        redisTemplate.delete(key)
        redisTemplate.opsForValue().set("$LAST_SEEN_KEY_PREFIX$userId", System.currentTimeMillis().toString())
    }

    // Check if user is online
    fun isOnline(userId: Long): Boolean {
        val key = "$ONLINE_KEY_PREFIX$userId"
        return redisTemplate.hasKey(key) == true
    }

    // Get last seen time
    fun getLastSeen(userId: Long): Long? {
        val key = "$LAST_SEEN_KEY_PREFIX$userId"
        val value = redisTemplate.opsForValue().get(key) as? String ?: return null
        return value.toLongOrNull()
    }

    // Get all online user IDs
    fun getOnlineUsers(): Set<Long> {
        val pattern = "$ONLINE_KEY_PREFIX*"
        val keys = redisTemplate.keys(pattern) ?: return emptySet()
        return keys.mapNotNull { key ->
            key.removePrefix(ONLINE_KEY_PREFIX).toLongOrNull()
        }.toSet()
    }
}
