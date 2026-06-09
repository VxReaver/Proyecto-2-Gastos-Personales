package com.example.gastospersonales.data.entities

import com.google.firebase.Timestamp

data class GroupFirestore(
    val id: String = "",
    val name: String = "",
    val inviteCode: String = "",
    val members: List<String> = emptyList()
)

data class ExpenseFirestore(
    val id: String = "",
    val description: String = "",
    val amount: Double = 0.0,
    val paidBy: String = "",
    val timestamp: Timestamp = Timestamp.now()
)
