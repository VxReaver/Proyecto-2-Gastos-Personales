package com.example.gastospersonales

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gastospersonales.data.AppDatabase
import com.example.gastospersonales.data.entities.Category
import com.example.gastospersonales.databinding.ActivityCategoriesBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class CategoriesActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCategoriesBinding
    private lateinit var adapter: CategoryAdapter
    private val db by lazy { AppDatabase.getDatabase(this) }
    private var userId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoriesBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        loadUserId()
        observeCategories()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
    }

    private fun setupRecyclerView() {
        adapter = CategoryAdapter(
            onItemClick = { category -> showOptionsDialog(category) }
        )
        binding.rvCategories.layoutManager = LinearLayoutManager(this)
        binding.rvCategories.adapter = adapter
    }

    private fun loadUserId() {
        val sharedPref = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("user_id", -1)
        if (userId == -1) {
            finish() // O redirigir a login
        }
    }

    private fun observeCategories() {
        lifecycleScope.launch {
            db.categoryDao().getCategoriesByUser(userId).collectLatest { categories ->
                if (categories.isEmpty()) {
                    binding.rvCategories.visibility = View.GONE
                    binding.tvEmptyState.visibility = View.VISIBLE
                } else {
                    binding.rvCategories.visibility = View.VISIBLE
                    binding.tvEmptyState.visibility = View.GONE
                    adapter.submitList(categories)
                }
            }
        }
    }

    private fun showOptionsDialog(category: Category) {
        val options = arrayOf("Modificar", "Eliminar")
        
        lifecycleScope.launch {
            val movementCount = db.movementDao().countMovementsByCategory(category.description, userId)
            
            val filteredOptions = if (movementCount > 0) {
                arrayOf("Modificar") // Ocultar eliminar si tiene movimientos
            } else {
                options
            }

            AlertDialog.Builder(this@CategoriesActivity)
                .setTitle(category.description)
                .setItems(filteredOptions) { _, which ->
                    when (filteredOptions[which]) {
                        "Modificar" -> openEditDialog(category)
                        "Eliminar" -> deleteCategory(category)
                    }
                }
                .show()
        }
    }

    private fun openEditDialog(category: Category) {
        val dialog = CategoryDialogFragment.newInstance(category)
        dialog.show(supportFragmentManager, "CategoryDialog")
    }

    private fun deleteCategory(category: Category) {
        lifecycleScope.launch {
            db.categoryDao().delete(category)
            
            Snackbar.make(binding.root, "Categoría eliminada", Snackbar.LENGTH_LONG)
                .setAction("Deshacer") {
                    lifecycleScope.launch {
                        db.categoryDao().insert(category)
                    }
                }
                .show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_categories, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                val dialog = CategoryDialogFragment.newInstance()
                dialog.show(supportFragmentManager, "CategoryDialog")
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
