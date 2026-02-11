package dev.mlzzen.backend.repository

import dev.mlzzen.backend.entity.Group
import dev.mlzzen.backend.entity.GroupMember
import dev.mlzzen.backend.entity.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface GroupMemberRepository : JpaRepository<GroupMember, Long> {

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group = :group")
    fun findByGroup(@Param("group") group: Group): List<GroupMember>

    @Query("SELECT gm FROM GroupMember gm WHERE gm.user = :user")
    fun findByUser(@Param("user") user: User): List<GroupMember>

    @Query("SELECT gm FROM GroupMember gm WHERE gm.group = :group AND gm.user = :user")
    fun findByGroupAndUser(
        @Param("group") group: Group,
        @Param("user") user: User
    ): GroupMember?

    @Query("SELECT COUNT(gm) FROM GroupMember gm WHERE gm.group = :group")
    fun countByGroup(@Param("group") group: Group): Long

    @Query("SELECT gm.user FROM GroupMember gm WHERE gm.group = :group")
    fun findUserIdsByGroup(@Param("group") group: Group): List<User>

    @Query("SELECT CASE WHEN COUNT(gm) > 0 THEN true ELSE false END FROM GroupMember gm WHERE gm.group = :group AND gm.user = :user")
    fun isMember(@Param("group") group: Group, @Param("user") user: User): Boolean
}
