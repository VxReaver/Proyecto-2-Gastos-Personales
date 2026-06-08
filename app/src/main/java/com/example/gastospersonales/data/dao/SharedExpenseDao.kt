package com.example.gastospersonales.data.dao

import androidx.room.*
import com.example.gastospersonales.data.entities.SharedExpense
import kotlinx.coroutines.flow.Flow

@Dao
interface SharedExpenseDao {
    @Insert
    suspend fun insert(expense: SharedExpense)

    @Delete
    suspend fun delete(expense: SharedExpense)

    @Query("SELECT * FROM shared_expenses WHERE groupId = :groupId ORDER BY fecha DESC")
    fun getExpensesByGroup(groupId: Int): Flow<List<SharedExpense>>

    @Query("""
        SELECT SUM(cantidad) FROM shared_expenses
        WHERE groupId = :groupId
    """)
    fun getTotalExpensesByGroup(groupId: Int): Flow<Double?>

    @Query("""
        SELECT 
            (SELECT COALESCE(SUM(cantidad), 0) FROM shared_expenses WHERE groupId = :groupId AND categoria != 'Transferencia') -
            (SELECT COALESCE(SUM(cantidad), 0) FROM shared_expenses WHERE groupId = :groupId AND categoria = 'Transferencia')
    """)
    fun getGroupBalance(groupId: Int): Flow<Double?>
}
