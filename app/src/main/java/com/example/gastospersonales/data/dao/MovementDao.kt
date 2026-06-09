package com.example.gastospersonales.data.dao

import androidx.room.*
import com.example.gastospersonales.data.entities.Movement
import kotlinx.coroutines.flow.Flow

@Dao
interface MovementDao {
    @Insert
    suspend fun insert(movement: Movement): Long

    @Update
    suspend fun update(movement: Movement)

    @Delete
    suspend fun delete(movement: Movement)

    @Query("SELECT * FROM movements WHERE id = :id LIMIT 1")
    suspend fun getById(id: Int): Movement?

    @Query("SELECT * FROM movements WHERE userId = :userId ORDER BY fecha DESC")
    fun getAll(userId: Int): Flow<List<Movement>>

    @Query("SELECT * FROM movements WHERE userId = :userId ORDER BY fecha DESC LIMIT :limit")
    fun getRecent(userId: Int, limit: Int): Flow<List<Movement>>

    @Query("""
        SELECT SUM(cantidad) FROM movements 
        WHERE userId = :userId AND tipo = 'Ingreso'
        AND (:cuenta = 'Todas' OR cuentaOrigen = :cuenta)
        AND CAST(strftime('%Y', fecha / 1000, 'unixepoch') AS INTEGER) = :year
        AND CAST(strftime('%m', fecha / 1000, 'unixepoch') AS INTEGER) = :month
    """)
    fun getFilteredIncome(userId: Int, cuenta: String, year: Int, month: Int): Flow<Double?>

    @Query("""
        SELECT SUM(cantidad) FROM movements 
        WHERE userId = :userId AND tipo = 'Gasto'
        AND (:cuenta = 'Todas' OR cuentaOrigen = :cuenta)
        AND CAST(strftime('%Y', fecha / 1000, 'unixepoch') AS INTEGER) = :year
        AND CAST(strftime('%m', fecha / 1000, 'unixepoch') AS INTEGER) = :month
    """)
    fun getFilteredExpense(userId: Int, cuenta: String, year: Int, month: Int): Flow<Double?>

    @Query("SELECT SUM(cantidad) FROM movements WHERE userId = :userId AND tipo = 'Ingreso'")
    fun getTotalIncome(userId: Int): Flow<Double?>

    @Query("SELECT SUM(cantidad) FROM movements WHERE userId = :userId AND tipo = 'Gasto'")
    fun getTotalExpense(userId: Int): Flow<Double?>

    @Query("SELECT (SELECT TOTAL(cantidad) FROM movements WHERE userId = :userId AND tipo = 'Ingreso') - (SELECT TOTAL(cantidad) FROM movements WHERE userId = :userId AND tipo = 'Gasto')")
    fun getBalance(userId: Int): Flow<Double?>

    @Query("SELECT * FROM movements WHERE userId = :userId AND categoria = :category ORDER BY fecha DESC")
    fun getByCategory(userId: Int, category: String): Flow<List<Movement>>

    @Query("""
        SELECT categoria, SUM(cantidad) as total 
        FROM movements 
        WHERE userId = :userId 
        AND (:cuenta = 'Todas' OR cuentaOrigen = :cuenta OR cuentaDestino = :cuenta)
        AND CAST(strftime('%Y', fecha / 1000, 'unixepoch') AS INTEGER) = :year
        AND CAST(strftime('%m', fecha / 1000, 'unixepoch') AS INTEGER) = :month
        GROUP BY categoria 
        ORDER BY total DESC
    """)
    fun getCategoryReport(userId: Int, cuenta: String, year: Int, month: Int): Flow<List<CategorySum>>

    @Query("SELECT COUNT(*) FROM movements WHERE (cuentaOrigen = :accountName OR cuentaDestino = :accountName) AND userId = :userId")
    suspend fun countMovementsByAccount(accountName: String, userId: Int): Int

    @Query("SELECT COUNT(*) FROM movements WHERE categoria = :categoryName AND userId = :userId")
    suspend fun countMovementsByCategory(categoryName: String, userId: Int): Int
}

data class CategorySum(
    val categoria: String,
    val total: Double
)
