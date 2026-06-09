package com.example.gastospersonales.ui.accounts

import android.app.Application
import androidx.lifecycle.*
import com.example.gastospersonales.data.AppDatabase
import com.example.gastospersonales.data.entities.Account
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AccountsViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val accountDao = database.accountDao()
    private val movementDao = database.movementDao()
    
    private val _userId = MutableStateFlow(-1)
    
    val allAccounts: LiveData<List<Account>> = _userId.flatMapLatest { id ->
        accountDao.getAllByUser(id)
    }.asLiveData()

    private var lastDeletedAccount: Account? = null

    fun setUserId(id: Int) {
        _userId.value = id
    }

    fun insert(account: Account) = viewModelScope.launch {
        accountDao.insert(account)
    }

    fun update(account: Account) = viewModelScope.launch {
        accountDao.update(account)
    }

    fun delete(account: Account) = viewModelScope.launch {
        lastDeletedAccount = account
        accountDao.delete(account)
    }

    fun undoDelete() = viewModelScope.launch {
        lastDeletedAccount?.let {
            accountDao.insert(it)
            lastDeletedAccount = null
        }
    }

    suspend fun getAccountById(id: Int): Account? {
        return accountDao.getById(id)
    }

    suspend fun canDeleteAccount(accountName: String): Boolean {
        val count = movementDao.countMovementsByAccount(accountName, _userId.value)
        return count == 0
    }
}
