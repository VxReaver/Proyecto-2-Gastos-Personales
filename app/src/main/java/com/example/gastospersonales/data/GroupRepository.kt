package com.example.gastospersonales.data

import com.example.gastospersonales.data.entities.ExpenseFirestore
import com.example.gastospersonales.data.entities.GroupFirestore
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class GroupRepository(private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()) {

    fun getGroupsForUser(userId: String): Flow<List<GroupFirestore>> = callbackFlow {
        val subscription = firestore.collection("groups")
            .whereArrayContains("members", userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val groups = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(GroupFirestore::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(groups)
            }
        awaitClose { subscription.remove() }
    }

    fun getExpensesForGroup(groupId: String): Flow<List<ExpenseFirestore>> = callbackFlow {
        val subscription = firestore.collection("groups")
            .document(groupId)
            .collection("expenses")
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                val expenses = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(ExpenseFirestore::class.java)?.copy(id = doc.id)
                } ?: emptyList()
                trySend(expenses)
            }
        awaitClose { subscription.remove() }
    }

    suspend fun createGroup(name: String, inviteCode: String, userId: String) {
        val group = GroupFirestore(
            name = name,
            inviteCode = inviteCode,
            members = listOf(userId)
        )
        firestore.collection("groups").add(group).await()
    }

    suspend fun joinGroup(inviteCode: String, userId: String): Boolean {
        val query = firestore.collection("groups")
            .whereEqualTo("inviteCode", inviteCode)
            .get()
            .await()

        if (query.isEmpty) return false

        val doc = query.documents.first()
        val members = doc.get("members") as? MutableList<String> ?: mutableListOf()
        if (!members.contains(userId)) {
            members.add(userId)
            doc.reference.update("members", members).await()
        }
        return true
    }

    suspend fun addExpense(groupId: String, expense: ExpenseFirestore) {
        firestore.collection("groups")
            .document(groupId)
            .collection("expenses")
            .add(expense)
            .await()
    }
}
