package com.example.gastospersonales

import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.gastospersonales.data.AppDatabase
import com.example.gastospersonales.data.entities.Movement
import com.example.gastospersonales.databinding.ActivityAddMovementBinding
import com.google.android.material.datepicker.MaterialDatePicker
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class AddMovementActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAddMovementBinding
    private var selectedDate = Date()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddMovementBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupSpinners()
        setupDatePicker()
        setupToggleLogic()

        binding.btnSave.setOnClickListener { saveMovement() }
    }

    private fun setupToggleLogic() {
        binding.toggleGroupType.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_transfer -> {
                        binding.layoutAccountDest.visibility = View.VISIBLE
                        binding.layoutCategory.visibility = View.GONE
                    }
                    else -> {
                        binding.layoutAccountDest.visibility = View.GONE
                        binding.layoutCategory.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    private fun setupSpinners() {
        val accounts = arrayOf("Efectivo", "T. Débito", "T. Crédito", "Vales")
        val categories = arrayOf("Comida", "Transporte", "Ropa", "Salud", "Educación", "Diversión", "Casa")
        
        binding.spinnerAccountOrigin.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, accounts))
        binding.spinnerAccountDest.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, accounts))
        binding.spinnerCategory.setAdapter(ArrayAdapter(this, android.R.layout.simple_list_item_1, categories))
    }

    private fun setupDatePicker() {
        val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        binding.etDate.setText(sdf.format(selectedDate))
        
        binding.etDate.setOnClickListener {
            val picker = MaterialDatePicker.Builder.datePicker().build()
            picker.show(supportFragmentManager, "DATE_PICKER")
            picker.addOnPositiveButtonClickListener {
                selectedDate = Date(it)
                binding.etDate.setText(sdf.format(selectedDate))
            }
        }
    }

    private fun saveMovement() {
        val amountStr = binding.etAmount.text.toString()
        if (amountStr.isEmpty()) {
            Toast.makeText(this, "Por favor ingresa una cantidad", Toast.LENGTH_SHORT).show()
            return
        }
        val amount = amountStr.toDoubleOrNull() ?: 0.0
        
        val type = when(binding.toggleGroupType.checkedButtonId) {
            R.id.btn_ingreso -> "Ingreso"
            R.id.btn_transfer -> "Transferencia"
            else -> "Gasto"
        }

        val movement = Movement(
            tipo = type,
            cantidad = amount,
            cuentaOrigen = binding.spinnerAccountOrigin.text.toString(),
            cuentaDestino = if (type == "Transferencia") binding.spinnerAccountDest.text.toString() else null,
            categoria = if (type == "Transferencia") "Transferencia" else binding.spinnerCategory.text.toString(),
            descripcion = binding.etDescription.text.toString(),
            fecha = selectedDate
        )

        lifecycleScope.launch {
            AppDatabase.getDatabase(this@AddMovementActivity).movementDao().insert(movement)
            Toast.makeText(this@AddMovementActivity, "Guardado con éxito", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
