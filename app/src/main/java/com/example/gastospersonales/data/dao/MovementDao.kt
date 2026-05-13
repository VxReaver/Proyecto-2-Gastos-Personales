package com.example.gastospersonales.data.dao

import androidx.room.*
import com.example.gastospersonales.data.entities.Movement
import kotlinx.coroutines.flow.Flow

@Dao
interface MovementDao {
    @Insert
    suspend fun insert(movement: Movement)

    @Update
    suspend fun update(movement: Movement)

    @Delete
    suspend fun delete(movement: Movement)

    @Query("SELECT * FROM movements WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Movement?

    @Query("SELECT * FROM movements ORDER BY fecha DESC")
    fun getAll(): Flow<List<Movement>>

    @Query("SELECT * FROM movements ORDER BY fecha DESC LIMIT :limit")
    fun getRecent(limit: Int): Flow<List<Movement>>

    @Query("SELECT SUM(cantidad) FROM movements WHERE tipo = 'Ingreso'")
    fun getTotalIncome(): Flow<Double?>

    @Query("SELECT SUM(cantidad) FROM movements WHERE tipo = 'Gasto'")
    fun getTotalExpense(): Flow<Double?>

    @Query("SELECT (SELECT TOTAL(cantidad) FROM movements WHERE tipo = 'Ingreso') - (SELECT TOTAL(cantidad) FROM movements WHERE tipo = 'Gasto')")
    fun getBalance(): Flow<Double?>

    @Query("SELECT * FROM movements WHERE categoria = :category ORDER BY fecha DESC")
    fun getByCategory(category: String): Flow<List<Movement>>

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
