package com.example.gastospersonales.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.gastospersonales.data.GroupRepository
import com.example.gastospersonales.data.entities.ExpenseFirestore
import com.example.gastospersonales.data.entities.GroupFirestore
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.UUID

class GroupViewModel(private val repository: GroupRepository = GroupRepository()) : ViewModel() {

    private val _userGroups = MutableStateFlow<List<GroupFirestore>>(emptyList())
    val userGroups: StateFlow<List<GroupFirestore>> = _userGroups.asStateFlow()

    private val _selectedGroupExpenses = MutableStateFlow<List<ExpenseFirestore>>(emptyList())
    val selectedGroupExpenses: StateFlow<List<ExpenseFirestore>> = _selectedGroupExpenses.asStateFlow()

    fun loadUserGroups(userId: String) {
        viewModelScope.launch {
            repository.getGroupsForUser(userId).collect { groups ->
                _userGroups.value = groups
            }
        }
    }

    fun selectGroup(groupId: String) {
        viewModelScope.launch {
            repository.getExpensesForGroup(groupId).collect { expenses ->
                _selectedGroupExpenses.value = expenses
            }
        }
    }

    fun createGroup(name: String, userId: String, onComplete: () -> Unit) {
        viewModelScope.launch {
            val inviteCode = UUID.randomUUID().toString().substring(0, 6).uppercase()
            repository.createGroup(name, inviteCode, userId)
            onComplete()
        }
    }

    fun joinGroup(inviteCode: String, userId: String, onResult: (Boolean) -> Unit) {
        viewModelScope.launch {
            val success = repository.joinGroup(inviteCode, userId)
            onResult(success)
        }
    }

    fun addExpense(groupId: String, description: String, amount: Double, paidBy: String) {
        viewModelScope.launch {
            val expense = ExpenseFirestore(
                description = description,
                amount = amount,
                paidBy = paidBy
            )
            repository.addExpense(groupId, expense)
        }
    }
}
