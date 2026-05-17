package com.example.gastospersonales.data.dao

import androidx.room.*
import com.example.gastospersonales.data.entities.Account
import kotlinx.coroutines.flow.Flow

@Dao
interface AccountDao {
    @Insert
    suspend fun insert(account: Account)

    @Update
    suspend fun update(account: Account)

    @Delete
    suspend fun delete(account: Account)

    @Query("SELECT * FROM accounts WHERE userId = :userId ORDER BY name ASC")
    fun getAllByUser(userId: Int): Flow<List<Account>>

    @Query("SELECT * FROM accounts WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Account?
}
