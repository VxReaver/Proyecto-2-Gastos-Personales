package com.example.gastospersonales.data.dao

import androidx.room.*
import com.example.gastospersonales.data.entities.Group
import kotlinx.coroutines.flow.Flow

@Dao
interface GroupDao {
    @Insert
    suspend fun insert(group: Group)

    @Update
    suspend fun update(group: Group)

    @Delete
    suspend fun delete(group: Group)

    @Query("SELECT * FROM groups WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Group?

    @Query("SELECT * FROM groups WHERE codigoUnico = :codigo LIMIT 1")
    suspend fun getByCode(codigo: String): Group?

    @Query("SELECT * FROM groups WHERE usuarioCreador = :userId ORDER BY fechaCreacion DESC")
    fun getAllByUser(userId: Int): Flow<List<Group>>

    @Query("""
        SELECT DISTINCT g.* FROM groups g
        INNER JOIN group_members gm ON g.id = gm.groupId
        WHERE gm.userId = :userId
        ORDER BY g.fechaCreacion DESC
    """)
    fun getGroupsOfMember(userId: Int): Flow<List<Group>>
}
