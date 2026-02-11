package dev.mlzzen.backend.dto

import java.time.LocalDateTime

data class GroupDto(
    val id: Long,
    val name: String,
    val description: String?,
    val avatarUrl: String?,
    val ownerId: Long,
    val ownerUsername: String,
    val memberCount: Int,
    val createdAt: LocalDateTime
)

data class CreateGroupDto(
    val name: String,
    val description: String? = null,
    val memberIds: List<Long> = emptyList()
)

data class GroupMemberDto(
    val id: Long,
    val userId: Long,
    val username: String,
    val avatarUrl: String?,
    val role: String,
    val joinedAt: LocalDateTime
)

data class InviteMemberDto(
    val userIds: List<Long>
)

data class GroupMessageDto(
    val id: Long,
    val senderId: Long,
    val senderUsername: String,
    val groupId: Long,
    val content: String,
    val messageType: dev.mlzzen.backend.entity.MessageType,
    val createdAt: LocalDateTime
)

data class CreateGroupMessageDto(
    val groupId: Long,
    val content: String,
    val messageType: dev.mlzzen.backend.entity.MessageType = dev.mlzzen.backend.entity.MessageType.TEXT
)
