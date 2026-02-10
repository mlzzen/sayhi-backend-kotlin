package dev.mlzzen.backend.service

import dev.mlzzen.backend.dto.UpdateUserRequest
import dev.mlzzen.backend.dto.UserDto
import dev.mlzzen.backend.entity.User
import dev.mlzzen.backend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UserService(
    private val userRepository: UserRepository
) {

    fun getUserById(id: Long): UserDto {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("User not found") }
        return UserDto.fromEntity(user)
    }

    @Transactional
    fun updateUser(id: Long, request: UpdateUserRequest): UserDto {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("User not found") }

        // Check if username is being changed and if it's taken
        request.username?.let { newUsername ->
            if (newUsername != user.username && userRepository.existsByUsername(newUsername)) {
                throw IllegalArgumentException("Username is already taken")
            }
            user.username = newUsername
        }

        request.avatarUrl?.let { user.avatarUrl = it }

        val updatedUser = userRepository.save(user)
        return UserDto.fromEntity(updatedUser)
    }

    fun searchUsers(query: String): List<UserDto> {
        return userRepository.findByUsernameContainingIgnoreCase(query)
            .map { UserDto.fromEntity(it) }
    }

    private fun UserRepository.findByUsernameContainingIgnoreCase(query: String): List<User> {
        return findAll().filter { it.username.contains(query, ignoreCase = true) }
    }
}
