package com.example.gastospersonales

import android.app.DatePickerDialog
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.gastospersonales.data.AppDatabase
import com.example.gastospersonales.data.entities.Movement
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditMovementActivity : AppCompatActivity() {

    private val calendar = Calendar.getInstance()
    private var movementId: Int = -1
    private lateinit var etAmount: TextInputEditText
    private lateinit var actvAccount: AutoCompleteTextView
    private lateinit var actvCategory: AutoCompleteTextView
    private lateinit var etDescription: TextInputEditText
    private lateinit var etDate: TextInputEditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_movement)

        movementId = intent.getIntExtra("MOVEMENT_ID", -1)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Inicializar vistas
        etAmount = findViewById(R.id.etAmount)
        actvAccount = findViewById(R.id.actvAccount)
        actvCategory = findViewById(R.id.actvCategory)
        etDescription = findViewById(R.id.etDescription)
        etDate = findViewById(R.id.etDate)

        setupDropdowns()
        setupDatePicker()

        if (movementId != -1) {
            loadMovementData()
        }

        findViewById<Button>(R.id.btnSave).setOnClickListener {
            saveChanges()
        }
    }

    private fun loadMovementData() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@EditMovementActivity)
            val movement = db.movementDao().getById(movementId)
            movement?.let {
                etAmount.setText(it.cantidad.toString())
                actvAccount.setText(it.cuentaOrigen, false)
                actvCategory.setText(it.categoria, false)
                etDescription.setText(it.descripcion)
                calendar.time = it.fecha
                updateLabel(etDate)
            }
        }
    }

    private fun setupDropdowns() {
        val accounts = arrayOf("Efectivo", "T. Débito", "T. Crédito", "Ahorros")
        val adapterAccounts = ArrayAdapter(this, android.R.layout.simple_list_item_1, accounts)
        actvAccount.setAdapter(adapterAccounts)

        val categories = arrayOf("Hogar", "Comida", "Transporte", "Salud", "Entretenimiento")
        val adapterCategories = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        actvCategory.setAdapter(adapterCategories)
    }

    private fun setupDatePicker() {
        val dateSetListener = DatePickerDialog.OnDateSetListener { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            updateLabel(etDate)
        }

        etDate.setOnClickListener {
            DatePickerDialog(
                this,
                dateSetListener,
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            ).show()
        }
    }

    private fun updateLabel(editText: TextInputEditText) {
        val myFormat = "dd MMM yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale("es", "ES"))
        editText.setText(sdf.format(calendar.time))
    }

    private fun saveChanges() {
        val amount = etAmount.text.toString().toDoubleOrNull() ?: 0.0
        val account = actvAccount.text.toString()
        val category = actvCategory.text.toString()
        val description = etDescription.text.toString()

        if (amount <= 0) {
            Toast.makeText(this, "Ingresa una cantidad válida", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@EditMovementActivity)
            val movement = db.movementDao().getById(movementId)
            
            if (movement != null) {
                val updatedMovement = movement.copy(
                    cantidad = amount,
                    cuentaOrigen = account,
                    categoria = category,
                    descripcion = description,
                    fecha = calendar.time
                )
                db.movementDao().update(updatedMovement)
                Toast.makeText(this@EditMovementActivity, "Movimiento actualizado", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}