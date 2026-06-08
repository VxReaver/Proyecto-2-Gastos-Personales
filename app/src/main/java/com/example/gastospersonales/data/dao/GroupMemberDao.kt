package com.example.gastospersonales.data.dao

import androidx.room.*
import com.example.gastospersonales.data.entities.GroupMember
import com.example.gastospersonales.data.entities.User
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupMemberDao {
    @Insert
    suspend fun insert(member: GroupMember)

    @Delete
    suspend fun delete(member: GroupMember)

    @Query("""
        SELECT u.* FROM users u
        INNER JOIN group_members gm ON u.id = gm.userId
        WHERE gm.groupId = :groupId
        ORDER BY gm.fechaUnion DESC
    """)
    fun getMembersOfGroup(groupId: Int): Flow<List<User>>

    @Query("""
        SELECT COUNT(*) FROM group_members
        WHERE groupId = :groupId AND userId = :userId
    """)
    suspend fun isMember(groupId: Int, userId: Int): Int // 0 o 1

    @Query("SELECT COUNT(*) FROM group_members WHERE groupId = :groupId")
    fun getMemberCount(groupId: Int): Flow<Int>
}
