package dev.mlzzen.backend.service

import dev.mlzzen.backend.dto.*
import dev.mlzzen.backend.entity.User
import dev.mlzzen.backend.repository.UserRepository
import dev.mlzzen.backend.security.JwtUtil
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val jwtUtil: JwtUtil
) {

    @Transactional
    fun register(request: RegisterRequest): AuthResponse {
        // Validate username
        if (userRepository.existsByUsername(request.username)) {
            throw IllegalArgumentException("Username is already taken")
        }

        // Validate email
        if (userRepository.existsByEmail(request.email)) {
            throw IllegalArgumentException("Email is already registered")
        }

        // Create user
        val user = User(
            username = request.username,
            email = request.email,
            passwordHash = passwordEncoder.encode(request.password)
        )

        val savedUser = userRepository.save(user)

        // Generate JWT
        val token = jwtUtil.generateToken(savedUser.id, savedUser.email)

        return AuthResponse(
            token = token,
            user = UserDto.fromEntity(savedUser)
        )
    }

    fun login(request: LoginRequest): AuthResponse {
        // Find user by email
        val user = userRepository.findByEmail(request.email)
            .orElseThrow { IllegalArgumentException("Invalid email or password") }

        // Verify password
        if (!passwordEncoder.matches(request.password, user.passwordHash)) {
            throw IllegalArgumentException("Invalid email or password")
        }

        // Generate JWT
        val token = jwtUtil.generateToken(user.id, user.email)

        return AuthResponse(
            token = token,
            user = UserDto.fromEntity(user)
        )
    }

    fun getUserById(id: Long): UserDto {
        val user = userRepository.findById(id)
            .orElseThrow { IllegalArgumentException("User not found") }
        return UserDto.fromEntity(user)
    }
}
