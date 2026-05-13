package com.example.gastospersonales

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
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
        observeMovements()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_movements, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.sort_account -> {
                currentSortMode = "account"
                updateList()
                true
            }
            R.id.sort_date -> {
                currentSortMode = "date"
                updateList()
                true
            }
            R.id.sort_amount -> {
                currentSortMode = "amount"
                updateList()
                true
            }
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
                    0 -> Toast.makeText(this, "Ir a Pantalla 8 (Modificar)", Toast.LENGTH_SHORT).show()
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
        lifecycleScope.launch {
            AppDatabase.getDatabase(this@CategoryDetailActivity)
                .movementDao()
                .getByCategory(categoryName)
                .collect { movements ->
                    movementsList = movements
                    updateList()
                }
        }
    }

    private fun updateList() {
        val sorted = when (currentSortMode) {
            "account" -> movementsList.sortedBy { it.cuentaOrigen }
            "amount" -> movementsList.sortedByDescending { it.cantidad }
            else -> movementsList.sortedByDescending { it.fecha }
        }

        adapter.updateData(sorted)
        
        val total = sorted.sumOf { it.cantidad }
        val formatter = NumberFormat.getCurrencyInstance(Locale.US)
        binding.tvTotalAmount.text = formatter.format(total)
    }
}
