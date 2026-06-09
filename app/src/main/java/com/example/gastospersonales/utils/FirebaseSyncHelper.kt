package com.example.gastospersonales.utils

import com.example.gastospersonales.data.entities.*
import com.google.firebase.database.FirebaseDatabase

object FirebaseSyncHelper {
    private val db = FirebaseDatabase.getInstance().reference

    fun syncUser(user: User) {
        db.child("users").child(user.id.toString()).setValue(user)
    }

    fun syncMovement(movement: Movement) {
        // Usamos una ruta por usuario para mayor orden
        db.child("movements")
            .child(movement.userId.toString())
            .child(movement.id.toString())
            .setValue(movement)
    }

    fun deleteMovement(movement: Movement) {
        db.child("movements")
            .child(movement.userId.toString())
            .child(movement.id.toString())
            .removeValue()
    }

    fun syncAccount(account: Account) {
        db.child("accounts")
            .child(account.userId.toString())
            .child(account.id.toString())
            .setValue(account)
    }

    fun syncCategory(category: Category) {
        db.child("categories")
            .child(category.userId.toString())
            .child(category.id.toString())
            .setValue(category)
    }

    fun syncGroup(group: Group) {
        db.child("groups").child(group.codigoUnico).setValue(group)
    }
}
