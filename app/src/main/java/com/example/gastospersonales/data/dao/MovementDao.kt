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
        WHERE (cuentaOrigen = :cuenta OR cuentaDestino = :cuenta)
        AND CAST(strftime('%Y', fecha / 1000, 'unixepoch') AS INTEGER) = :year
        AND CAST(strftime('%m', fecha / 1000, 'unixepoch') AS INTEGER) = :month
        GROUP BY categoria 
        ORDER BY total DESC
    """)
    fun getCategoryReport(cuenta: String, year: Int, month: Int): Flow<List<CategorySum>>
}

data class CategorySum(
    val categoria: String,
    val total: Double
)
