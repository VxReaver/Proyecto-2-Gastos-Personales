package com.example.gastospersonales.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

@Entity(
    tableName = "group_members",
    foreignKeys = [
        ForeignKey(entity = Group::class, parentColumns = ["id"], childColumns = ["groupId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = User::class, parentColumns = ["id"], childColumns = ["userId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [
        Index(value = ["groupId"]),
        Index(value = ["userId"])
    ]
)
data class GroupMember(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val groupId: Int, // FK to groups
    val userId: Int, // FK to users
    val fechaUnion: Date
)
