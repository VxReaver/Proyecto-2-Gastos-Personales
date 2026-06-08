package com.example.gastospersonales

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gastospersonales.data.entities.Movement
import java.text.SimpleDateFormat
import java.util.Locale

class MovementAdapter(
    private var movements: List<Movement>,
    private val onItemClick: (Movement) -> Unit
) : RecyclerView.Adapter<MovementAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivAccountIcon: ImageView = view.findViewById(R.id.ivAccountIcon)
        val tvDate: TextView = view.findViewById(R.id.tvDate)
        val tvCategoryName: TextView = view.findViewById(R.id.tvCategoryName)
        val tvAccountName: TextView = view.findViewById(R.id.tvAccountName)
        val tvDescription: TextView = view.findViewById(R.id.tvDescription)
        val tvAmount: TextView = view.findViewById(R.id.tvAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movement, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val movement = movements[position]
        holder.tvDate.text = dateFormat.format(movement.fecha)
        holder.tvCategoryName.text = movement.categoria
        holder.tvAccountName.text = movement.cuentaOrigen
        holder.tvDescription.text = movement.descripcion
        
        val isExpense = movement.tipo == "Gasto"
        val amountText = if (isExpense) "-$ ${String.format("%.2f", movement.cantidad)}" 
                        else "+$ ${String.format("%.2f", movement.cantidad)}"
        holder.tvAmount.text = amountText
        holder.tvAmount.setTextColor(if (isExpense) Color.parseColor("#C62828") else Color.parseColor("#2E7D32"))

        holder.itemView.setOnClickListener { onItemClick(movement) }
    }

    override fun getItemCount() = movements.size

    fun updateData(newMovements: List<Movement>) {
        movements = newMovements
        notifyDataSetChanged()
    }
}