package dev.mlzzen.backend.service

import dev.mlzzen.backend.dto.CreateFriendRequestDto
import dev.mlzzen.backend.dto.FriendDto
import dev.mlzzen.backend.dto.FriendRequestDto
import dev.mlzzen.backend.dto.UpdateFriendRequestDto
import dev.mlzzen.backend.entity.Friendship
import dev.mlzzen.backend.entity.FriendshipStatus
import dev.mlzzen.backend.entity.User
import dev.mlzzen.backend.repository.FriendshipRepository
import dev.mlzzen.backend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FriendshipService(
    private val friendshipRepository: FriendshipRepository,
    private val userRepository: UserRepository
) {

    // Send a friend request
    @Transactional
    fun sendFriendRequest(requesterId: Long, dto: CreateFriendRequestDto): FriendRequestDto {
        val requester = userRepository.findById(requesterId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val addressee = userRepository.findById(dto.userId)
            .orElseThrow { IllegalArgumentException("Target user not found") }

        if (requesterId == dto.userId) {
            throw IllegalArgumentException("Cannot add yourself as friend")
        }

        // Check if friendship already exists
        friendshipRepository.findByUsers(requester, addressee).ifPresent { existing ->
            throw IllegalArgumentException("Friendship already exists with status: ${existing.status}")
        }

        // Check if there's already a pending request in either direction
        if (friendshipRepository.existsPendingRequest(requester, addressee, FriendshipStatus.PENDING)) {
            throw IllegalArgumentException("Friend request already sent")
        }
        if (friendshipRepository.existsPendingRequest(addressee, requester, FriendshipStatus.PENDING)) {
            throw IllegalArgumentException("You have a pending request from this user. Please accept or reject it first.")
        }

        val friendship = Friendship(
            requester = requester,
            addressee = addressee,
            status = FriendshipStatus.PENDING
        )
        val saved = friendshipRepository.save(friendship)

        return toFriendRequestDto(saved)
    }

    // Accept or reject a friend request
    @Transactional
    fun handleFriendRequest(requestId: Long, userId: Long, dto: UpdateFriendRequestDto): FriendRequestDto {
        val friendship = friendshipRepository.findById(requestId)
            .orElseThrow { IllegalArgumentException("Friend request not found") }

        if (friendship.addressee.id != userId) {
            throw IllegalArgumentException("Not authorized to handle this request")
        }

        if (friendship.status != FriendshipStatus.PENDING) {
            throw IllegalArgumentException("Request has already been handled")
        }

        friendship.status = if (dto.accept) FriendshipStatus.ACCEPTED else FriendshipStatus.REJECTED
        friendship.updatedAt = java.time.LocalDateTime.now()
        val saved = friendshipRepository.save(friendship)

        return toFriendRequestDto(saved)
    }

    // Get all friends (accepted) for a user
    @Transactional(readOnly = true)
    fun getFriends(userId: Long): List<FriendDto> {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        val friendships = friendshipRepository.findAcceptedFriendshipsByUser(user, FriendshipStatus.ACCEPTED)
        return friendships.map { friendship ->
            val friend = if (friendship.requester.id == userId) friendship.addressee else friendship.requester
            FriendDto(
                id = friend.id,
                username = friend.username,
                avatarUrl = friend.avatarUrl,
                status = FriendshipStatus.ACCEPTED,
                createdAt = friendship.createdAt.toString()
            )
        }
    }

    // Get pending friend requests received by a user
    @Transactional(readOnly = true)
    fun getPendingRequests(userId: Long): List<FriendRequestDto> {
        val user = userRepository.findById(userId)
            .orElseThrow { IllegalArgumentException("User not found") }

        return friendshipRepository.findPendingRequestsByUser(user, FriendshipStatus.PENDING)
            .map { toFriendRequestDto(it) }
    }

    // Delete a friend or cancel a request
    @Transactional
    fun deleteFriend(requesterId: Long, friendId: Long) {
        val user1 = userRepository.findById(requesterId)
            .orElseThrow { IllegalArgumentException("User not found") }
        val user2 = userRepository.findById(friendId)
            .orElseThrow { IllegalArgumentException("Friend not found") }

        val friendship = friendshipRepository.findByUsers(user1, user2)
            .orElseThrow { IllegalArgumentException("Friendship not found") }

        friendshipRepository.delete(friendship)
    }

    private fun toFriendRequestDto(friendship: Friendship): FriendRequestDto {
        val otherUser = if (friendship.requester.id != friendship.addressee.id) {
            if (friendship.status == FriendshipStatus.PENDING) {
                // For pending requests, show requester info
                friendship.requester
            } else {
                friendship.addressee
            }
        } else {
            friendship.requester
        }

        return FriendRequestDto(
            id = friendship.id,
            userId = otherUser.id,
            username = otherUser.username,
            avatarUrl = otherUser.avatarUrl,
            status = friendship.status,
            createdAt = friendship.createdAt.toString()
        )
    }
}
