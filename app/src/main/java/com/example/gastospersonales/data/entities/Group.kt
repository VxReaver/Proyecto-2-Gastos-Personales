package com.example.gastospersonales.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "groups")
data class Group(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val nombre: String,
    val codigoUnico: String, // UNIQUE
    val usuarioCreador: Int, // FK to users
    val fechaCreacion: Date,
    val descripcion: String = ""
)
