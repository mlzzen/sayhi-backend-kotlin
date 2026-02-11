package dev.mlzzen.backend.controller

import dev.mlzzen.backend.dto.*
import dev.mlzzen.backend.security.JwtUtil
import dev.mlzzen.backend.service.GroupService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/groups")
class GroupController(
    private val groupService: GroupService,
    private val jwtUtil: JwtUtil
) {

    // Create a new group
    @PostMapping
    fun createGroup(
        @RequestHeader("Authorization") authHeader: String,
        @RequestBody dto: CreateGroupDto
    ): ResponseEntity<GroupDto> {
        val userId = extractUserId(authHeader)
        val group = groupService.createGroup(userId, dto)
        return ResponseEntity.ok(group)
    }

    // Get user's groups
    @GetMapping
    fun getGroups(@RequestHeader("Authorization") authHeader: String): ResponseEntity<List<GroupDto>> {
        val userId = extractUserId(authHeader)
        val groups = groupService.getGroups(userId)
        return ResponseEntity.ok(groups)
    }

    // Get group details
    @GetMapping("/{groupId}")
    fun getGroup(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable groupId: Long
    ): ResponseEntity<GroupDto> {
        val userId = extractUserId(authHeader)
        val group = groupService.getGroup(groupId, userId)
        return ResponseEntity.ok(group)
    }

    // Get group members
    @GetMapping("/{groupId}/members")
    fun getGroupMembers(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable groupId: Long
    ): ResponseEntity<List<GroupMemberDto>> {
        val userId = extractUserId(authHeader)
        val members = groupService.getGroupMembers(groupId, userId)
        return ResponseEntity.ok(members)
    }

    // Invite members to a group
    @PostMapping("/{groupId}/members")
    fun inviteMembers(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable groupId: Long,
        @RequestBody dto: InviteMemberDto
    ): ResponseEntity<List<GroupMemberDto>> {
        val userId = extractUserId(authHeader)
        val members = groupService.inviteMembers(groupId, userId, dto)
        return ResponseEntity.ok(members)
    }

    // Remove member from group
    @DeleteMapping("/{groupId}/members/{userId}")
    fun removeMember(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable groupId: Long,
        @PathVariable userId: Long
    ): ResponseEntity<Unit> {
        val currentUserId = extractUserId(authHeader)
        groupService.removeMember(groupId, currentUserId, userId)
        return ResponseEntity.noContent().build()
    }

    // Get group messages
    @GetMapping("/{groupId}/messages")
    fun getGroupMessages(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable groupId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "50") size: Int
    ): ResponseEntity<List<GroupMessageDto>> {
        val userId = extractUserId(authHeader)
        val messages = groupService.getGroupMessages(groupId, userId, page, size)
        return ResponseEntity.ok(messages)
    }

    // Send group message
    @PostMapping("/{groupId}/messages")
    fun sendGroupMessage(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable groupId: Long,
        @RequestBody dto: CreateGroupMessageDto
    ): ResponseEntity<GroupMessageDto> {
        val userId = extractUserId(authHeader)
        val message = groupService.sendGroupMessage(userId, dto.copy(groupId = groupId))
        return ResponseEntity.ok(message)
    }

    // Leave group
    @PostMapping("/{groupId}/leave")
    fun leaveGroup(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable groupId: Long
    ): ResponseEntity<Unit> {
        val userId = extractUserId(authHeader)
        groupService.leaveGroup(groupId, userId)
        return ResponseEntity.ok().build()
    }

    private fun extractUserId(authHeader: String): Long {
        val token = authHeader.removePrefix("Bearer ")
        return jwtUtil.getUserIdFromToken(token)
            ?: throw IllegalArgumentException("Invalid token")
    }
}
