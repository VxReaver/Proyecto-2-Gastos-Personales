package com.example.gastospersonales

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.gastospersonales.data.entities.Movement
import com.example.gastospersonales.databinding.ItemMovementBinding
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Locale

class MovementAdapter(
    private var movements: List<Movement>,
    private val onItemClick: (Movement) -> Unit // Añadido para manejar el clic en cada movimiento
) : RecyclerView.Adapter<MovementAdapter.ViewHolder>() {

    private val currencyFormatter = NumberFormat.getCurrencyInstance(Locale.US)
    private val dateFormatter = SimpleDateFormat("dd MMMM yyyy", Locale("es", "MX"))

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemMovementBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(movements[position])
    }

    override fun getItemCount() = movements.size

    fun updateData(newMovements: List<Movement>) {
        this.movements = newMovements
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemMovementBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        
        fun bind(movement: Movement) {
            binding.tvCategoryName.text = movement.categoria
            binding.tvAccountName.text = movement.cuentaOrigen
            binding.tvDate.text = dateFormatter.format(movement.fecha)
            binding.tvDescription.text = movement.descripcion
            
            val amountText = currencyFormatter.format(movement.cantidad)
            binding.tvAmount.text = amountText
            
            val colorRes = when (movement.tipo) {
                "Ingreso" -> android.R.color.holo_green_dark
                "Gasto" -> android.R.color.holo_red_dark
                else -> android.R.color.darker_gray
            }
            binding.tvAmount.setTextColor(ContextCompat.getColor(binding.root.context, colorRes))

            // Listener para cumplir con la opción de Modificar/Eliminar al hacer tap
            binding.root.setOnClickListener {
                onItemClick(movement)
            }
        }
    }
}
