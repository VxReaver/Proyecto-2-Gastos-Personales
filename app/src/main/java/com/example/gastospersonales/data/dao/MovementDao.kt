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

    @Query("SELECT * FROM movements WHERE id = :id")
    suspend fun getById(id: Int): Movement?

    @Query("SELECT * FROM movements ORDER BY fecha DESC")
    fun getAll(): Flow<List<Movement>>

    @Query("SELECT * FROM movements WHERE categoria = :category ORDER BY fecha DESC")
    fun getByCategory(category: String): Flow<List<Movement>>

    // Ingresos del mes y cuenta específicos
    @Query("""
        SELECT SUM(cantidad) FROM movements 
        WHERE tipo = 'Ingreso' 
        AND (:cuenta = 'Todas' OR cuentaOrigen = :cuenta)
        AND CAST(strftime('%Y', fecha / 1000, 'unixepoch') AS INTEGER) = :year
        AND CAST(strftime('%m', fecha / 1000, 'unixepoch') AS INTEGER) = :month
    """)
    fun getMonthlyIncome(cuenta: String, year: Int, month: Int): Flow<Double?>

    // Gastos del mes y cuenta específicos
    @Query("""
        SELECT SUM(cantidad) FROM movements 
        WHERE tipo = 'Gasto' 
        AND (:cuenta = 'Todas' OR cuentaOrigen = :cuenta)
        AND CAST(strftime('%Y', fecha / 1000, 'unixepoch') AS INTEGER) = :year
        AND CAST(strftime('%m', fecha / 1000, 'unixepoch') AS INTEGER) = :month
    """)
    fun getMonthlyExpense(cuenta: String, year: Int, month: Int): Flow<Double?>

    // Saldo anterior (Ingresos - Gastos antes del inicio del mes seleccionado)
    @Query("""
        SELECT 
            (SELECT COALESCE(SUM(cantidad), 0.0) FROM movements WHERE tipo = 'Ingreso' AND (:cuenta = 'Todas' OR cuentaOrigen = :cuenta) AND fecha < :startDate) -
            (SELECT COALESCE(SUM(cantidad), 0.0) FROM movements WHERE tipo = 'Gasto' AND (:cuenta = 'Todas' OR cuentaOrigen = :cuenta) AND fecha < :startDate)
    """)
    fun getPreviousBalance(cuenta: String, startDate: Long): Flow<Double?>

    @Query("""
        SELECT categoria, SUM(cantidad) as total 
        FROM movements 
        WHERE (:cuenta = 'Todas' OR cuentaOrigen = :cuenta OR cuentaDestino = :cuenta)
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
