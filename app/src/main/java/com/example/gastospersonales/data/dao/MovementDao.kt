package com.example.gastospersonales.data.dao

import androidx.room.*
import com.example.gastospersonales.data.entities.Movement
import kotlinx.coroutines.flow.Flow

@Dao
interface MovementDao {
    @Insert
    suspend fun insert(movement: Movement)

    @Delete
    suspend fun delete(movement: Movement)

    @Query("SELECT * FROM movements ORDER BY fecha DESC")
    fun getAll(): Flow<List<Movement>>

    @Query("""
        SELECT categoria, SUM(cantidad) as total 
        FROM movements 
        WHERE cuentaOrigen = :cuenta OR cuentaDestino = :cuenta
        GROUP BY categoria 
        ORDER BY total DESC
    """)
    fun getCategoryReport(cuenta: String): Flow<List<CategorySum>>
}

data class CategorySum(
    val categoria: String,
    val total: Double
)
