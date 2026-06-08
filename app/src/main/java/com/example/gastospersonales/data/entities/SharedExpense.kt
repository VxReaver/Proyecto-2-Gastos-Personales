package com.example.gastospersonales.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "shared_expenses",
    foreignKeys = [
        ForeignKey(entity = Group::class, parentColumns = ["id"], childColumns = ["groupId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["usuarioPagador"], onDelete = ForeignKey.CASCADE)
    ]
)
data class SharedExpense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int, // FK to groups
    val usuarioPagador: Int, // FK to users (quien pagó)
    val cantidad: Double,
    val descripcion: String,
    val categoria: String,
    val fecha: Date
)
