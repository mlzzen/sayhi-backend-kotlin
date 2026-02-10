package dev.mlzzen.backend.entity

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false, length = 50)
    var username: String,

    @Column(unique = true, nullable = false, length = 100)
    val email: String,

    @Column(nullable = false)
    val passwordHash: String,

    @Column(length = 255)
    var avatarUrl: String? = null,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now()
)
