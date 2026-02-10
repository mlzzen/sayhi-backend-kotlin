package dev.mlzzen.backend.dto

data class AuthResponse(
    val token: String,
    val user: UserDto
)
