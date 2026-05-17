package com.example.gastospersonales.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "movements")
data class Movement(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int, // Vinculación con el usuario
    val tipo: String, // "Ingreso", "Gasto", "Transferencia"
    val cantidad: Double,
    val cuentaOrigen: String,
    val cuentaDestino: String? = null,
    val categoria: String,
    val descripcion: String,
    val fecha: Date
)
