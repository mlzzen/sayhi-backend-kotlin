package dev.mlzzen.backend.dto

import dev.mlzzen.backend.entity.User
import java.time.LocalDateTime

data class UserDto(
    val id: Long,
    val username: String,
    val email: String,
    val avatarUrl: String?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun fromEntity(user: User): UserDto = UserDto(
            id = user.id,
            username = user.username,
            email = user.email,
            avatarUrl = user.avatarUrl,
            createdAt = user.createdAt
        )
    }
}
