package com.example.gastospersonales

import android.content.Context
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gastospersonales.data.AppDatabase
import com.example.gastospersonales.data.entities.Category
import com.example.gastospersonales.databinding.ActivityCategoriesBinding
import com.example.gastospersonales.databinding.DialogCategoryOptionsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
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

        loadUserId()
        setupToolbar()
        setupRecyclerView()
        observeCategories()
    }

    private fun setupToolbar() {
        setSupportActionBar(binding.toolbar)
        binding.toolbar.setNavigationOnClickListener { finish() }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Categorías"
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
        if (userId == -1) finish()
    }

    private fun observeCategories() {
        lifecycleScope.launch {
            // El Flow notificará cualquier cambio (inserción manual o default)
            db.categoryDao().getCategoriesByUser(userId).collectLatest { categories ->
                if (categories.isEmpty()) {
                    // Si no existen categorías para el usuario, las creamos
                    insertDefaultCategories()
                } else {
                    binding.rvCategories.visibility = View.VISIBLE
                    binding.tvEmptyState.visibility = View.GONE
                    // El DAO ya devuelve la lista en orden DESC
                    adapter.submitList(categories)
                }
            }
        }
    }

    private suspend fun insertDefaultCategories() {
        val defaults = listOf(
            Category(userId = userId, description = "Transporte", iconResource = R.drawable.ic_movements),
            Category(userId = userId, description = "Salud", iconResource = R.drawable.ic_info),
            Category(userId = userId, description = "Ropa", iconResource = R.drawable.ic_person),
            Category(userId = userId, description = "Otros", iconResource = R.drawable.ic_wallet),
            Category(userId = userId, description = "Hogar", iconResource = R.drawable.ic_home),
            Category(userId = userId, description = "Entretenimiento", iconResource = R.drawable.ic_calendar),
            Category(userId = userId, description = "Comida", iconResource = R.drawable.ic_categories)
        )
        defaults.forEach { db.categoryDao().insert(it) }
    }

    private fun showOptionsDialog(category: Category) {
        val dialogBinding = DialogCategoryOptionsBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(this, R.style.Theme_GastosPersonales_Dialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvOptionTitle.text = category.description
        
        lifecycleScope.launch {
            // Regla: No eliminar si tiene movimientos
            val movementCount = db.movementDao().countMovementsByCategory(category.description, userId)
            val canDelete = movementCount == 0
            
            dialogBinding.btnOptionDelete.visibility = if (canDelete) View.VISIBLE else View.GONE
            
            dialogBinding.btnOptionEdit.setOnClickListener {
                dialog.dismiss()
                openEditDialog(category)
            }

            dialogBinding.btnOptionDelete.setOnClickListener {
                dialog.dismiss()
                showDeleteConfirmation(category)
            }

            dialogBinding.btnOptionCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
            
            // Tamaño grande y centrado
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun showDeleteConfirmation(category: Category) {
        MaterialAlertDialogBuilder(this)
            .setTitle("¿Eliminar categoría?")
            .setMessage("¿Estás seguro de que deseas eliminar '${category.description}'?")
            .setPositiveButton("Eliminar") { _, _ -> deleteCategory(category) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun openEditDialog(category: Category) {
        CategoryDialogFragment.newInstance(category).show(supportFragmentManager, "CategoryDialog")
    }

    private fun deleteCategory(category: Category) {
        lifecycleScope.launch {
            db.categoryDao().delete(category)
            Snackbar.make(binding.root, "Categoría eliminada", Snackbar.LENGTH_LONG)
                .setAction("Deshacer") {
                    lifecycleScope.launch { db.categoryDao().insert(category) }
                }.show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_categories, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_add -> {
                CategoryDialogFragment.newInstance().show(supportFragmentManager, "CategoryDialog")
                true
            }
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
