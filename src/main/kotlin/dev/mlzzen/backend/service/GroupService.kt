package dev.mlzzen.backend.service

import dev.mlzzen.backend.dto.*
import dev.mlzzen.backend.entity.Group
import dev.mlzzen.backend.entity.GroupMember
import dev.mlzzen.backend.entity.GroupMemberRole
import dev.mlzzen.backend.entity.Message
import dev.mlzzen.backend.entity.MessageType
import dev.mlzzen.backend.repository.GroupMemberRepository
import dev.mlzzen.backend.repository.GroupRepository
import dev.mlzzen.backend.repository.MessageRepository
import dev.mlzzen.backend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class GroupService(
    private val groupRepository: GroupRepository,
    private val groupMemberRepository: GroupMemberRepository,
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository
) {

    // Create a new group
    @Transactional
    fun createGroup(ownerId: Long, dto: CreateGroupDto): GroupDto {
        val owner = userRepository.findById(ownerId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val group = Group(
            name = dto.name,
            description = dto.description,
            owner = owner
        )
        val savedGroup = groupRepository.save(group)

        // Add owner as admin
        val ownerMember = GroupMember(
            group = savedGroup,
            user = owner,
            role = GroupMemberRole.OWNER
        )
        groupMemberRepository.save(ownerMember)

        // Add initial members (if any)
        dto.memberIds.forEach { userId ->
            if (userId != ownerId) {
                addMember(savedGroup, userId)
            }
        }

        return toGroupDto(savedGroup)
    }

    // Get groups for a user
    @Transactional(readOnly = true)
    fun getGroups(userId: Long): List<GroupDto> {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        return groupMemberRepository.findByUser(user).map { it.group }.map { toGroupDto(it) }
    }

    // Get group details
    @Transactional(readOnly = true)
    fun getGroup(groupId: Long, userId: Long): GroupDto {
        val group = groupRepository.findById(groupId)
            .orElseThrow { IllegalArgumentException("Group not found") }

        // Verify user is a member
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }
        if (!groupMemberRepository.isMember(group, user)) {
            throw IllegalArgumentException("You are not a member of this group")
        }

        return toGroupDto(group)
    }

    // Get group members
    @Transactional(readOnly = true)
    fun getGroupMembers(groupId: Long, userId: Long): List<GroupMemberDto> {
        val group = groupRepository.findById(groupId)
            .orElseThrow { IllegalArgumentException("Group not found") }

        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }
        if (!groupMemberRepository.isMember(group, user)) {
            throw IllegalArgumentException("You are not a member of this group")
        }

        return groupMemberRepository.findByGroup(group).map { toGroupMemberDto(it) }
    }

    // Invite members to a group
    @Transactional
    fun inviteMembers(groupId: Long, inviterId: Long, dto: InviteMemberDto): List<GroupMemberDto> {
        val group = groupRepository.findById(groupId)
            .orElseThrow { IllegalArgumentException("Group not found") }

        val inviter = userRepository.findById(inviterId)
            .orElseThrow { IllegalArgumentException("User not found") }

        // Check if inviter is a member
        if (!groupMemberRepository.isMember(group, inviter)) {
            throw IllegalArgumentException("You are not a member of this group")
        }

        val members = mutableListOf<GroupMemberDto>()
        dto.userIds.forEach { userId ->
            try {
                val member = addMember(group, userId)
                members.add(toGroupMemberDto(member))
            } catch (e: Exception) {
                // Skip if already a member
            }
        }

        return members
    }

    // Remove member from group
    @Transactional
    fun removeMember(groupId: Long, removerId: Long, userIdToRemove: Long) {
        val group = groupRepository.findById(groupId)
            .orElseThrow { IllegalArgumentException("Group not found") }

        val remover = userRepository.findById(removerId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val userToRemove = userRepository.findById(userIdToRemove)
            .orElseThrow { IllegalArgumentException("User not found") }

        // Check if remover is admin or owner
        val removerMember = groupMemberRepository.findByGroupAndUser(group, remover)
            ?: throw IllegalArgumentException("You are not a member of this group")

        if (removerMember.role == GroupMemberRole.MEMBER) {
            throw IllegalArgumentException("Only admins can remove members")
        }

        // Cannot remove owner
        if (userToRemove.id == group.owner.id) {
            throw IllegalArgumentException("Cannot remove the owner")
        }

        val memberToRemove = groupMemberRepository.findByGroupAndUser(group, userToRemove)
            ?: throw IllegalArgumentException("User is not a member of this group")

        groupMemberRepository.delete(memberToRemove)
    }

    // Get group messages
    @Transactional(readOnly = true)
    fun getGroupMessages(groupId: Long, userId: Long, page: Int = 0, size: Int = 50): List<GroupMessageDto> {
        val group = groupRepository.findById(groupId)
            .orElseThrow { IllegalArgumentException("Group not found") }

        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }
        if (!groupMemberRepository.isMember(group, user)) {
            throw IllegalArgumentException("You are not a member of this group")
        }

        val messages = messageRepository.findByGroup(group)
            .sortedByDescending { it.createdAt }
            .drop(page * size)
            .take(size)
            .reversed()

        return messages.map { toGroupMessageDto(it) }
    }

    // Send group message
    @Transactional
    fun sendGroupMessage(senderId: Long, dto: CreateGroupMessageDto): GroupMessageDto {
        val sender = userRepository.findById(senderId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val group = groupRepository.findById(dto.groupId)
            .orElseThrow { IllegalArgumentException("Group not found") }

        // Verify sender is a member
        if (!groupMemberRepository.isMember(group, sender)) {
            throw IllegalArgumentException("You are not a member of this group")
        }

        val message = Message(
            sender = sender,
            group = group,
            content = dto.content,
            messageType = dto.messageType
        )
        val saved = messageRepository.save(message)

        // Update group's updatedAt
        group.updatedAt = java.time.LocalDateTime.now()
        groupRepository.save(group)

        return toGroupMessageDto(saved)
    }

    // Leave group
    @Transactional
    fun leaveGroup(groupId: Long, userId: Long) {
        val group = groupRepository.findById(groupId)
            .orElseThrow { IllegalArgumentException("Group not found") }

        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        // Owner cannot leave, must transfer ownership first
        if (group.owner.id == userId) {
            throw IllegalArgumentException("Owner cannot leave the group. Transfer ownership first.")
        }

        val member = groupMemberRepository.findByGroupAndUser(group, user)
            ?: throw IllegalArgumentException("You are not a member of this group")

        groupMemberRepository.delete(member)
    }

    private fun addMember(group: Group, userId: Long): GroupMember {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        // Check if already a member
        groupMemberRepository.findByGroupAndUser(group, user)?.let {
            throw IllegalArgumentException("User is already a member")
        }

        val member = GroupMember(
            group = group,
            user = user,
            role = GroupMemberRole.MEMBER
        )
        return groupMemberRepository.save(member)
    }

    private fun toGroupDto(group: Group): GroupDto {
        val memberCount = groupMemberRepository.countByGroup(group).toInt()
        return GroupDto(
            id = group.id,
            name = group.name,
            description = group.description,
            avatarUrl = group.avatarUrl,
            ownerId = group.owner.id,
            ownerUsername = group.owner.username,
            memberCount = memberCount,
            createdAt = group.createdAt
        )
    }

    private fun toGroupMemberDto(member: GroupMember): GroupMemberDto {
        return GroupMemberDto(
            id = member.id,
            userId = member.user.id,
            username = member.user.username,
            avatarUrl = member.user.avatarUrl,
            role = member.role.name,
            joinedAt = member.joinedAt
        )
    }

    private fun toGroupMessageDto(message: Message): GroupMessageDto {
        return GroupMessageDto(
            id = message.id,
            senderId = message.sender.id,
            senderUsername = message.sender.username,
            groupId = message.group!!.id,
            content = message.content,
            messageType = message.messageType,
            createdAt = message.createdAt
        )
    }
}
