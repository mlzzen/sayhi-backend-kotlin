package dev.mlzzen.backend.repository

import dev.mlzzen.backend.entity.Group
import dev.mlzzen.backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface GroupRepository : JpaRepository<Group, Long> {

    @Query("SELECT g FROM Group g WHERE g.owner = :user")
    fun findByOwner(@Param("user") user: User): List<Group>

    @Query("""
        SELECT g FROM Group g
        JOIN GroupMember gm ON gm.group = g
        WHERE gm.user = :user
        ORDER BY g.updatedAt DESC
    """)
    fun findByMember(@Param("user") user: User): List<Group>
}
