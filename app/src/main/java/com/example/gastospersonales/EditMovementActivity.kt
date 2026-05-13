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
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class EditMovementActivity : AppCompatActivity() {

    private val calendar = Calendar.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_edit_movement)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        toolbar.setNavigationOnClickListener { finish() }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Configurar Spinners (Dropdowns)
        setupDropdowns()

        // Configurar DatePicker
        setupDatePicker()

        // Botón Guardar
        findViewById<Button>(R.id.btnSave).setOnClickListener {
            Toast.makeText(this, "Cambios guardados", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun setupDropdowns() {
        // Cuentas de ejemplo
        val accounts = arrayOf("Efectivo", "T. Débito", "T. Crédito", "Ahorros")
        val adapterAccounts = ArrayAdapter(this, android.R.layout.simple_list_item_1, accounts)
        findViewById<AutoCompleteTextView>(R.id.actvAccount).setAdapter(adapterAccounts)

        // Categorías de ejemplo
        val categories = arrayOf("Hogar", "Comida", "Transporte", "Salud", "Entretenimiento")
        val adapterCategories = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        findViewById<AutoCompleteTextView>(R.id.actvCategory).setAdapter(adapterCategories)
    }

    private fun setupDatePicker() {
        val etDate = findViewById<TextInputEditText>(R.id.etDate)
        
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
}