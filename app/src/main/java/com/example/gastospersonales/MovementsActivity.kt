package com.example.gastospersonales

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gastospersonales.data.AppDatabase
import com.example.gastospersonales.data.entities.Movement
import com.example.gastospersonales.databinding.ActivityMovementsBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.util.Calendar

class MovementsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMovementsBinding
    private lateinit var adapter: MovementAdapter
    
    private var currentAccount = "Todas"
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    
    private var allMovementsList = listOf<Movement>()
    private var currentSortMode = "date"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovementsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupFilters()
        observeMovements()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_movements, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sort_account -> { currentSortMode = "account"; updateList(); true }
            R.id.sort_date -> { currentSortMode = "date"; updateList(); true }
            R.id.sort_amount -> { currentSortMode = "amount"; updateList(); true }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun setupRecyclerView() {
        adapter = MovementAdapter(emptyList()) { movement ->
            showMovementOptions(movement)
        }
        binding.rvMovements.layoutManager = LinearLayoutManager(this)
        binding.rvMovements.adapter = adapter
    }

    private fun showMovementOptions(movement: Movement) {
        val options = arrayOf("Modificar", "Eliminar")
        AlertDialog.Builder(this)
            .setTitle("Opciones de movimiento")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> {
                        val intent = Intent(this, EditMovementActivity::class.java)
                        intent.putExtra("MOVEMENT_ID", movement.id)
                        startActivity(intent)
                    }
                    1 -> deleteMovement(movement)
                }
            }
            .show()
    }

    private fun deleteMovement(movement: Movement) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@MovementsActivity)
            db.movementDao().delete(movement)
            
            Snackbar.make(binding.root, "Movimiento eliminado", Snackbar.LENGTH_LONG)
                .setAction("Deshacer") {
                    lifecycleScope.launch {
                        db.movementDao().insert(movement)
                    }
                }.show()
        }
    }

    private fun setupFilters() {
        val accounts = arrayOf("Todas", "Efectivo", "T. Débito", "T. Crédito", "Vales")
        val accountAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, accounts)
        binding.spinnerAccountFilter.setAdapter(accountAdapter)
        binding.spinnerAccountFilter.setText(currentAccount, false)
        binding.spinnerAccountFilter.setOnItemClickListener { _, _, position, _ ->
            currentAccount = accounts[position]
            updateList()
        }

        val years = (2020..2030).map { it.toString() }.toTypedArray()
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, years)
        binding.spinnerYear.setAdapter(yearAdapter)
        binding.spinnerYear.setText(currentYear.toString(), false)
        binding.spinnerYear.setOnItemClickListener { _, _, position, _ ->
            currentYear = years[position].toInt()
            updateList()
        }

        val months = arrayOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                             "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, months)
        binding.spinnerMonth.setAdapter(monthAdapter)
        binding.spinnerMonth.setText(months[currentMonth - 1], false)
        binding.spinnerMonth.setOnItemClickListener { _, _, position, _ ->
            currentMonth = position + 1
            updateList()
        }
    }

    private fun observeMovements() {
        lifecycleScope.launch {
            AppDatabase.getDatabase(this@MovementsActivity)
                .movementDao()
                .getAll()
                .collect { movements ->
                    allMovementsList = movements
                    updateList()
                }
        }
    }

    private fun updateList() {
        var filtered = allMovementsList.filter { mov ->
            val cal = Calendar.getInstance().apply { time = mov.fecha }
            val matchAccount = currentAccount == "Todas" || mov.cuentaOrigen == currentAccount || mov.cuentaDestino == currentAccount
            val matchYear = cal.get(Calendar.YEAR) == currentYear
            val matchMonth = (cal.get(Calendar.MONTH) + 1) == currentMonth
            matchAccount && matchYear && matchMonth
        }

        filtered = when (currentSortMode) {
            "account" -> filtered.sortedBy { it.cuentaOrigen }
            "amount" -> filtered.sortedByDescending { it.cantidad }
            else -> filtered.sortedByDescending { it.fecha }
        }

        adapter.updateData(filtered)
    }
}
