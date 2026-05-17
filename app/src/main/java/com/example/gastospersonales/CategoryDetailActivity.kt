package com.example.gastospersonales

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gastospersonales.data.AppDatabase
import com.example.gastospersonales.data.entities.Movement
import com.example.gastospersonales.databinding.ActivityCategoryDetailBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

class CategoryDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryDetailBinding
    private lateinit var adapter: MovementAdapter
    private var categoryName: String = ""
    private var movementsList = listOf<Movement>()
    private var currentSortMode = "date"
    private var currentTypeFilter = "Todos"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        categoryName = intent.getStringExtra("CATEGORY_NAME") ?: ""
        binding.tvCategoryTitle.text = categoryName

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        binding.toolbar.setNavigationOnClickListener { finish() }

        setupRecyclerView()
        setupTypeFilter()
        observeMovements()
    }

    private fun setupTypeFilter() {
        val types = arrayOf("Todos", "Gasto", "Ingreso")
        val typeAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, types)
        binding.spinnerTypeFilter.setAdapter(typeAdapter)
        binding.spinnerTypeFilter.setText(types[0], false)
        binding.spinnerTypeFilter.setOnItemClickListener { _, _, position, _ ->
            currentTypeFilter = types[position]
            updateList()
        }
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
            val db = AppDatabase.getDatabase(this@CategoryDetailActivity)
            db.movementDao().delete(movement)
            
            Snackbar.make(binding.root, "Movimiento eliminado", Snackbar.LENGTH_LONG)
                .setAction("Deshacer") {
                    lifecycleScope.launch {
                        db.movementDao().insert(movement)
                    }
                }.show()
        }
    }

    private fun observeMovements() {
        // Obtenemos el userId como entero explícitamente
        val sharedPref = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        val userId: Int = sharedPref.getInt("user_id", -1)

        lifecycleScope.launch {
            AppDatabase.getDatabase(this@CategoryDetailActivity)
                .movementDao()
                .getByCategory(userId, categoryName) // Pasamos Int y luego String
                .collect { movements ->
                    movementsList = movements
                    updateList()
                }
        }
    }

    private fun updateList() {
        var filtered = if (currentTypeFilter == "Todos") {
            movementsList
        } else {
            movementsList.filter { it.tipo == currentTypeFilter }
        }

        filtered = when (currentSortMode) {
            "account" -> filtered.sortedBy { it.cuentaOrigen }
            "amount" -> filtered.sortedByDescending { it.cantidad }
            else -> filtered.sortedByDescending { it.fecha }
        }

        adapter.updateData(filtered)
        
        val total = filtered.sumOf { it.cantidad }
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        binding.tvTotalAmount.text = formatter.format(total)
    }
}