package com.example.gastospersonales

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gastospersonales.data.AppDatabase
import com.example.gastospersonales.data.entities.Movement
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddMovementActivity : AppCompatActivity() {
    private var selectedDate = Date()
    private val accounts = arrayOf("Efectivo", "Tarjeta Débito", "Tarjeta Crédito", "Ahorros")
    private val categories = arrayOf("Comida", "Transporte", "Ropa", "Salud", "Educación", "Hogar", "Otros")

    private lateinit var etAmount: TextInputEditText
    private lateinit var spinnerAccountOrigin: AutoCompleteTextView
    private lateinit var spinnerAccountDest: AutoCompleteTextView
    private lateinit var spinnerCategory: AutoCompleteTextView
    private lateinit var etDescription: TextInputEditText
    private lateinit var etDate: TextInputEditText
    private lateinit var toggleGroupType: MaterialButtonToggleGroup
    private lateinit var layoutAccountDest: TextInputLayout
    private lateinit var layoutCategory: TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_movement)

        // Inicializar vistas
        etAmount = findViewById(R.id.et_amount)
        spinnerAccountOrigin = findViewById(R.id.spinner_account_origin)
        spinnerAccountDest = findViewById(R.id.spinner_account_dest)
        spinnerCategory = findViewById(R.id.spinner_category)
        etDescription = findViewById(R.id.et_description)
        etDate = findViewById(R.id.et_date)
        toggleGroupType = findViewById(R.id.toggle_group_type)
        layoutAccountDest = findViewById(R.id.layout_account_dest)
        layoutCategory = findViewById(R.id.layout_category)

        setupSpinners()
        setupDatePicker()
        setupToggleLogic()

        findViewById<Button>(R.id.btn_save).setOnClickListener { saveMovement() }
    }

    private fun setupToggleLogic() {
        toggleGroupType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                if (checkedId == R.id.btn_transfer) {
                    layoutAccountDest.visibility = View.VISIBLE
                    layoutCategory.visibility = View.GONE
                } else {
                    layoutAccountDest.visibility = View.GONE
                    layoutCategory.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setupSpinners() {
        val accountAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, accounts)
        spinnerAccountOrigin.setAdapter(accountAdapter)
        spinnerAccountDest.setAdapter(accountAdapter)
        
        val categoryAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, categories)
        spinnerCategory.setAdapter(categoryAdapter)
    }

    private fun setupDatePicker() {
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
    }

    private fun saveMovement() {
        val amountStr = etAmount.text.toString()
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa una cantidad", Toast.LENGTH_SHORT).show()
            return
        }
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        
        val type = when(toggleGroupType.checkedButtonId) {
            R.id.btn_ingreso -> "Ingreso"
            R.id.btn_transfer -> "Transferencia"
            else -> "Gasto"
        }

        val movement = Movement(
            tipo = type,
            cantidad = amount,
            cuentaOrigen = spinnerAccountOrigin.text.toString(),
            cuentaDestino = if (type == "Transferencia") spinnerAccountDest.text.toString() else null,
            categoria = if (type == "Transferencia") "Transferencia" else spinnerCategory.text.toString(),
            descripcion = etDescription.text.toString(),
            fecha = selectedDate
        )

        lifecycleScope.launch {
            AppDatabase.getDatabase(this@AddMovementActivity).movementDao().insert(movement)
            Toast.makeText(this@AddMovementActivity, "Guardado con éxito", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}