package com.example.gastospersonales.ui.accounts

import android.app.Application
import androidx.lifecycle.*
import com.example.gastospersonales.data.AppDatabase
import com.example.gastospersonales.data.entities.Account
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class AccountsViewModel(application: Application) : AndroidViewModel(application) {
    private val accountDao = AppDatabase.getDatabase(application).accountDao()
    
    // Lista ordenada descendentemente por nombre (descripción)
    val allAccounts: LiveData<List<Account>> = accountDao.getAllByUser(0) // Asumiendo userId 0 por ahora
        .map { list -> list.sortedByDescending { it.name } }
        .asLiveData()

    private var lastDeletedAccount: Account? = null

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
}
