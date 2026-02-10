package dev.mlzzen.backend.repository

import dev.mlzzen.backend.entity.Message
import dev.mlzzen.backend.entity.User
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface MessageRepository : JpaRepository<Message, Long> {

    // Get conversation between two users (ordered by time)
    @Query("""
        SELECT m FROM Message m
        WHERE (m.sender = :user1 AND m.receiver = :user2)
           OR (m.sender = :user2 AND m.receiver = :user1)
        ORDER BY m.createdAt DESC
    """)
    fun findConversation(
        @Param("user1") user1: User,
        @Param("user2") user2: User,
        pageable: Pageable
    ): Page<Message>

    // Get conversation with pagination (newest first)
    @Query("""
        SELECT m FROM Message m
        WHERE (m.sender = :currentUser AND m.receiver = :otherUser)
           OR (m.sender = :otherUser AND m.receiver = :currentUser)
        ORDER BY m.createdAt DESC
    """)
    fun findMessagesBetweenUsers(
        @Param("currentUser") currentUser: User,
        @Param("otherUser") otherUser: User,
        pageable: Pageable
    ): List<Message>

    // Count unread messages from a specific user
    @Query("""
        SELECT COUNT(m) FROM Message m
        WHERE m.receiver = :user AND m.sender = :fromUser AND m.isRead = false
    """)
    fun countUnreadFrom(
        @Param("user") user: User,
        @Param("fromUser") fromUser: User
    ): Long

    // Count total unread messages for a user
    @Query("""
        SELECT COUNT(m) FROM Message m
        WHERE m.receiver = :user AND m.isRead = false
    """)
    fun countTotalUnread(@Param("user") user: User): Long

    // Get last message with each friend for chat list
    @Query("""
        SELECT m FROM Message m
        WHERE m.sender = :user OR m.receiver = :user
        ORDER BY m.createdAt DESC
    """)
    fun findLatestMessagesByUser(@Param("user") user: User): List<Message>
}
