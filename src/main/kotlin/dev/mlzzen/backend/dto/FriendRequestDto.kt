package dev.mlzzen.backend.dto

import dev.mlzzen.backend.entity.FriendshipStatus

data class FriendRequestDto(
    val id: Long,
    val userId: Long,
    val username: String,
    val avatarUrl: String?,
    val status: FriendshipStatus,
    val createdAt: String
)

data class FriendDto(
    val id: Long,
    val username: String,
    val avatarUrl: String?,
    val status: FriendshipStatus,
    val createdAt: String
)

data class CreateFriendRequestDto(
    val userId: Long
)

data class UpdateFriendRequestDto(
    val accept: Boolean
)
