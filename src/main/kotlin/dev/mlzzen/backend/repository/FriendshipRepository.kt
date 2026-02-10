package dev.mlzzen.backend.repository

import dev.mlzzen.backend.entity.Friendship
import dev.mlzzen.backend.entity.FriendshipStatus
import dev.mlzzen.backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import java.util.Optional

@Repository
interface FriendshipRepository : JpaRepository<Friendship, Long> {

    // Check if a friendship exists between two users (in either direction)
    @Query("SELECT f FROM Friendship f WHERE (f.requester = :user1 AND f.addressee = :user2) OR (f.requester = :user2 AND f.addressee = :user1)")
    fun findByUsers(@Param("user1") user1: User, @Param("user2") user2: User): Optional<Friendship>

    // Check if a friendship exists with a specific status
    @Query("SELECT f FROM Friendship f WHERE (f.requester = :user1 AND f.addressee = :user2 AND f.status = :status) OR (f.requester = :user2 AND f.addressee = :user1 AND f.status = :status)")
    fun findByUsersAndStatus(
        @Param("user1") user1: User,
        @Param("user2") user2: User,
        @Param("status") status: FriendshipStatus
    ): Optional<Friendship>

    // Find all friends (accepted) for a user
    @Query("""
        SELECT CASE WHEN f.requester = :user THEN f.addressee ELSE f.requester END
        FROM Friendship f
        WHERE (f.requester = :user OR f.addressee = :user) AND f.status = :status
    """)
    fun findFriendsByUser(@Param("user") user: User, @Param("status") status: FriendshipStatus): List<User>

    // Find all pending requests received by a user
    @Query("SELECT f FROM Friendship f WHERE f.addressee = :user AND f.status = :status")
    fun findPendingRequestsByUser(@Param("user") user: User, @Param("status") status: FriendshipStatus): List<Friendship>

    // Find all pending requests sent by a user
    @Query("SELECT f FROM Friendship f WHERE f.requester = :user AND f.status = :status")
    fun findPendingSentByUser(@Param("user") user: User, @Param("status") status: FriendshipStatus): List<Friendship>

    // Check if users are friends
    @Query("""
        SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END
        FROM Friendship f
        WHERE ((f.requester = :user1 AND f.addressee = :user2) OR (f.requester = :user2 AND f.addressee = :user1))
        AND f.status = :status
    """)
    fun areFriends(
        @Param("user1") user1: User,
        @Param("user2") user2: User,
        @Param("status") status: FriendshipStatus
    ): Boolean

    // Check if a pending request exists from user1 to user2
    @Query("""
        SELECT CASE WHEN COUNT(f) > 0 THEN true ELSE false END
        FROM Friendship f
        WHERE f.requester = :requester AND f.addressee = :addressee AND f.status = :status
    """)
    fun existsPendingRequest(
        @Param("requester") requester: User,
        @Param("addressee") addressee: User,
        @Param("status") status: FriendshipStatus
    ): Boolean
}
