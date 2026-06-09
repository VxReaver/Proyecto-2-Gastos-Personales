package com.example.gastospersonales.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int = 0,
    val name: String,
    val description: String = "", // Nueva descripción
    val initialBalance: Double = 0.0,
    val iconRes: Int = 0,
    val hasMovements: Boolean = false
)
