package com.example.gastospersonales

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gastospersonales.data.AppDatabase
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class GroupPanelActivity : AppCompatActivity() {

    private var groupId = -1
    private var currentUserId = -1
    private lateinit var adapter: SharedExpenseAdapter
    private var selectedDate = Date()
    private val categories = arrayOf("Comida", "Transporte", "Ropa", "Salud", "Educación", "Hogar", "Otros")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_panel)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        groupId = intent.getIntExtra("GROUP_ID", -1)
        val sharedPref = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        currentUserId = sharedPref.getInt("user_id", -1)

        if (groupId == -1 || currentUserId == -1) {
            Toast.makeText(this, "Error: Datos de grupo o usuario inválidos", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // Componentes
        val tvGroupName = findViewById<TextView>(R.id.tvGroupNamePanel)
        val tvBalance = findViewById<TextView>(R.id.tvGroupBalance)
        val tvMemberCount = findViewById<TextView>(R.id.tvMemberCount)
        val rvExpenses = findViewById<RecyclerView>(R.id.rvSharedExpenses)
        val spinnerCategory = findViewById<AutoCompleteTextView>(R.id.spinnerExpenseCategory)
        val etAmount = findViewById<TextInputEditText>(R.id.etExpenseAmount)
        val etDescription = findViewById<TextInputEditText>(R.id.etExpenseDescription)
        val etDate = findViewById<TextInputEditText>(R.id.etExpenseDate)
        val btnAddExpense = findViewById<Button>(R.id.btnAddSharedExpense)

        // Configurar RecyclerView
        adapter = SharedExpenseAdapter(emptyList(), this) { }
        rvExpenses.layoutManager = LinearLayoutManager(this)
        rvExpenses.adapter = adapter

        // Configurar spinner de categorías
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        spinnerCategory.setAdapter(categoryAdapter)

        // Configurar date picker
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        etDate.setText(sdf.format(selectedDate))
        etDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker().build()
            picker.show(supportFragmentManager, "DATE_PICKER")
            picker.addOnPositiveButtonClickListener {
                selectedDate = Date(it)
                etDate.setText(sdf.format(selectedDate))
            }
        }

        // Botón para agregar gasto
        btnAddExpense.setOnClickListener {
            addExpense(etAmount, spinnerCategory, etDescription)
        }

        // Cargar datos del grupo
        observeGroupData(tvGroupName, tvBalance, tvMemberCount)
        observeExpenses()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun observeGroupData(tvName: TextView, tvBalance: TextView, tvMemberCount: TextView) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@GroupPanelActivity)

            // Obtener datos del grupo
            val group = db.groupDao().getById(groupId)
            group?.let {
                supportActionBar?.title = it.nombre
                tvName.text = it.nombre
            }

            // Observar balance
            db.sharedExpenseDao().getGroupBalance(groupId).collectLatest { balance ->
                tvBalance.text = "Balance: $ ${String.format("%.2f", balance ?: 0.0)}"
            }

            // Observar cantidad de miembros
            db.groupMemberDao().getMemberCount(groupId).collectLatest { count ->
                tvMemberCount.text = "Miembros: $count"
            }
        }
    }

    private fun observeExpenses() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@GroupPanelActivity)
            db.sharedExpenseDao().getExpensesByGroup(groupId).collectLatest { expenses ->
                adapter.updateData(expenses)
            }
        }
    }

    private fun addExpense(
        etAmount: TextInputEditText,
        spinnerCategory: AutoCompleteTextView,
        etDescription: TextInputEditText
    ) {
        val amountStr = etAmount.text.toString()
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa la cantidad", Toast.LENGTH_SHORT).show()
            return
        }

        val category = spinnerCategory.text.toString()
        if (category.isEmpty()) {
            Toast.makeText(this, "Por favor selecciona una categoría", Toast.LENGTH_SHORT).show()
            return
        }

        val amount = amountStr.toDoubleOrNull() ?: 0.0
        val description = etDescription.text.toString().ifEmpty { category }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@GroupPanelActivity)
            val expense = com.example.gastospersonales.data.entities.SharedExpense(
                groupId = groupId,
                usuarioPagador = currentUserId,
                cantidad = amount,
                descripcion = description,
                categoria = category,
                fecha = selectedDate
            )
            db.sharedExpenseDao().insert(expense)

            Toast.makeText(this@GroupPanelActivity, "Gasto agregado", Toast.LENGTH_SHORT).show()
            etAmount.text?.clear()
            etDescription.text?.clear()
            spinnerCategory.text.clear()
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
