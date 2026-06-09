package com.example.gastospersonales

import android.content.Context
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
import com.example.gastospersonales.utils.FirebaseSyncHelper
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddMovementActivity : AppCompatActivity() {
    private var selectedDate = Date()
    private var userId: Int = -1

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

        // Cargar usuario
        val sharedPref = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        userId = sharedPref.getInt("user_id", -1)
        if (userId == -1) {
            Toast.makeText(this, "Error: Inicia sesión de nuevo", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

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

        setupDataFromDatabase()
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

    private fun setupDataFromDatabase() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@AddMovementActivity)
            
            // Cargar Cuentas Reales
            db.accountDao().getAllByUser(userId).collect { accountList ->
                val accountNames = accountList.map { it.name }
                val accountAdapter = ArrayAdapter(this@AddMovementActivity, android.R.layout.simple_list_item_1, accountNames)
                spinnerAccountOrigin.setAdapter(accountAdapter)
                spinnerAccountDest.setAdapter(accountAdapter)
            }
        }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@AddMovementActivity)
            
            // Cargar Categorías Reales
            db.categoryDao().getCategoriesByUser(userId).collect { categoryList ->
                val categoryNames = categoryList.map { it.description }
                val categoryAdapter = ArrayAdapter(this@AddMovementActivity, android.R.layout.simple_list_item_1, categoryNames)
                spinnerCategory.setAdapter(categoryAdapter)
            }
        }
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

        val accountOrigin = spinnerAccountOrigin.text.toString()
        if (accountOrigin.isEmpty()) {
            Toast.makeText(this, "Selecciona una cuenta de origen", Toast.LENGTH_SHORT).show()
            return
        }

        val movement = Movement(
            userId = userId,
            tipo = type,
            cantidad = amount,
            cuentaOrigen = accountOrigin,
            cuentaDestino = if (type == "Transferencia") spinnerAccountDest.text.toString() else null,
            categoria = if (type == "Transferencia") "Transferencia" else spinnerCategory.text.toString(),
            descripcion = etDescription.text.toString(),
            fecha = selectedDate
        )

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@AddMovementActivity)
            val id = db.movementDao().insert(movement)
            val movementWithId = movement.copy(id = id.toInt())
            
            // Sincronizar con Firebase
            FirebaseSyncHelper.syncMovement(movementWithId)

            Toast.makeText(this@AddMovementActivity, "Guardado con éxito", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
