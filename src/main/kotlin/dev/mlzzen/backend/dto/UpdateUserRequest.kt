package dev.mlzzen.backend.dto

data class UpdateUserRequest(
    val username: String? = null,
    val avatarUrl: String? = null
)
