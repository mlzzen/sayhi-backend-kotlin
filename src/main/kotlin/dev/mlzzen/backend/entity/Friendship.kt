package dev.mlzzen.backend.entity

import jakarta.persistence.*

@Entity
@Table(
    name = "friendships",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_friendship_pair",
            columnNames = ["requester_id", "addressee_id"]
        )
    ]
)
data class Friendship(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", nullable = false)
    val requester: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "addressee_id", nullable = false)
    val addressee: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: FriendshipStatus = FriendshipStatus.PENDING,

    @Column(name = "created_at", nullable = false, updatable = false)
    val createdAt: java.time.LocalDateTime = java.time.LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: java.time.LocalDateTime = java.time.LocalDateTime.now()
)

enum class FriendshipStatus {
    PENDING,
    ACCEPTED,
    REJECTED
}
