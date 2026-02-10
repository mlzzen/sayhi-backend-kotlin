package dev.mlzzen.backend.controller

import dev.mlzzen.backend.dto.*
import dev.mlzzen.backend.security.JwtUtil
import dev.mlzzen.backend.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/users")
class UserController(
    private val userService: UserService,
    private val jwtUtil: JwtUtil
) {

    @GetMapping("/me")
    fun getCurrentUser(@RequestHeader("Authorization") authHeader: String): ResponseEntity<UserDto> {
        val token = authHeader.substring(7)
        val userId = jwtUtil.getUserIdFromToken(token)
            ?: return ResponseEntity.status(401).build()

        val user = userService.getUserById(userId)
        return ResponseEntity.ok(user)
    }

    @PutMapping("/me")
    fun updateCurrentUser(
        @RequestHeader("Authorization") authHeader: String,
        @RequestBody request: UpdateUserRequest
    ): ResponseEntity<UserDto> {
        val token = authHeader.substring(7)
        val userId = jwtUtil.getUserIdFromToken(token)
            ?: return ResponseEntity.status(401).build()

        val updatedUser = userService.updateUser(userId, request)
        return ResponseEntity.ok(updatedUser)
    }

    @GetMapping("/search")
    fun searchUsers(@RequestParam("q") query: String): ResponseEntity<List<UserDto>> {
        val users = userService.searchUsers(query)
        return ResponseEntity.ok(users)
    }

    @GetMapping("/{id}")
    fun getUserById(@PathVariable id: Long): ResponseEntity<UserDto> {
        val user = userService.getUserById(id)
        return ResponseEntity.ok(user)
    }
}
