package dev.mlzzen.backend.controller

import dev.mlzzen.backend.dto.*
import dev.mlzzen.backend.security.JwtUtil
import dev.mlzzen.backend.service.FriendshipService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/friends")
class FriendController(
    private val friendshipService: FriendshipService,
    private val jwtUtil: JwtUtil
) {

    // Get current user's friends
    @GetMapping
    fun getFriends(@RequestHeader("Authorization") authHeader: String): ResponseEntity<List<FriendDto>> {
        val userId = extractUserId(authHeader)
        val friends = friendshipService.getFriends(userId)
        return ResponseEntity.ok(friends)
    }

    // Get pending friend requests
    @GetMapping("/requests")
    fun getPendingRequests(@RequestHeader("Authorization") authHeader: String): ResponseEntity<List<FriendRequestDto>> {
        val userId = extractUserId(authHeader)
        val requests = friendshipService.getPendingRequests(userId)
        return ResponseEntity.ok(requests)
    }

    // Send friend request
    @PostMapping("/request")
    fun sendFriendRequest(
        @RequestHeader("Authorization") authHeader: String,
        @RequestBody dto: CreateFriendRequestDto
    ): ResponseEntity<FriendRequestDto> {
        val userId = extractUserId(authHeader)
        val request = friendshipService.sendFriendRequest(userId, dto)
        return ResponseEntity.ok(request)
    }

    // Accept or reject friend request
    @PutMapping("/request/{requestId}")
    fun handleFriendRequest(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable requestId: Long,
        @RequestBody dto: UpdateFriendRequestDto
    ): ResponseEntity<FriendRequestDto> {
        val userId = extractUserId(authHeader)
        val request = friendshipService.handleFriendRequest(requestId, userId, dto)
        return ResponseEntity.ok(request)
    }

    // Delete friend or cancel request
    @DeleteMapping("/{friendId}")
    fun deleteFriend(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable friendId: Long
    ): ResponseEntity<Unit> {
        val userId = extractUserId(authHeader)
        friendshipService.deleteFriend(userId, friendId)
        return ResponseEntity.noContent().build()
    }

    private fun extractUserId(authHeader: String): Long {
        val token = authHeader.removePrefix("Bearer ")
        return jwtUtil.getUserIdFromToken(token)
            ?: throw IllegalArgumentException("Invalid token")
    }
}
