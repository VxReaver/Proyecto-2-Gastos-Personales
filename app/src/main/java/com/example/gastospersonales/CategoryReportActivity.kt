package com.example.gastospersonales

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gastospersonales.data.AppDatabase
import com.example.gastospersonales.databinding.ActivityCategoryReportBinding
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import java.util.Calendar

class CategoryReportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityCategoryReportBinding
    private lateinit var adapter: CategoryReportAdapter
    
    private var currentAccount = "Efectivo"
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1 // 1-indexed
    private var userId: Int = -1
    
    private var observationJob: Job? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCategoryReportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtener el ID del usuario logueado
        val sharedPref = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(this, "Error de sesión", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        setupRecyclerView()
        setupFilters()
        observeData()
    }

    private fun setupRecyclerView() {
        // Al hacer clic en una categoría, abrimos la Pantalla 6 (Detalle)
        adapter = CategoryReportAdapter(emptyList()) { categoryName ->
            val intent = Intent(this, CategoryDetailActivity::class.java)
            intent.putExtra("CATEGORY_NAME", categoryName)
            startActivity(intent)
        }
        binding.rvCategoryReport.layoutManager = LinearLayoutManager(this)
        binding.rvCategoryReport.adapter = adapter
    }

    private fun setupFilters() {
        // Accounts Filter sincronizados con el resto de la app
        val accounts = arrayOf("Efectivo", "T. Débito", "T. Crédito", "Vales")
        val accountAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, accounts)
        binding.spinnerAccountFilter.setAdapter(accountAdapter)
        binding.spinnerAccountFilter.setText(currentAccount, false)
        binding.spinnerAccountFilter.setOnItemClickListener { _, _, position, _ ->
            currentAccount = accounts[position]
            observeData()
        }

        // Year Filter
        val years = (2020..2030).map { it.toString() }.toTypedArray()
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, years)
        binding.spinnerYear.setAdapter(yearAdapter)
        binding.spinnerYear.setText(currentYear.toString(), false)
        binding.spinnerYear.setOnItemClickListener { _, _, position, _ ->
            currentYear = years[position].toInt()
            observeData()
        }

        // Month Filter
        val months = arrayOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                             "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, months)
        binding.spinnerMonth.setAdapter(monthAdapter)
        binding.spinnerMonth.setText(months[currentMonth - 1], false)
        binding.spinnerMonth.setOnItemClickListener { _, _, position, _ ->
            currentMonth = position + 1
            observeData()
        }
    }

    private fun observeData() {
        observationJob?.cancel()
        observationJob = lifecycleScope.launch {
            AppDatabase.getDatabase(this@CategoryReportActivity)
                .movementDao()
                .getCategoryReport(userId, currentAccount, currentYear, currentMonth)
                .collect { report ->
                    adapter.updateData(report)
                }
        }
    }
}
