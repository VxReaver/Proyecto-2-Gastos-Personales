package com.example.gastospersonales

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gastospersonales.data.AppDatabase
import com.example.gastospersonales.data.entities.SharedExpense
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale

class SharedExpenseAdapter(
    private var expenses: List<SharedExpense>,
    private val context: Context,
    private val onItemClick: (SharedExpense) -> Unit
) : RecyclerView.Adapter<SharedExpenseAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))
    private val userNames = mutableMapOf<Int, String>()

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDate: TextView = view.findViewById(R.id.tvSharedDate)
        val tvDescription: TextView = view.findViewById(R.id.tvSharedDescription)
        val tvPaidBy: TextView = view.findViewById(R.id.tvSharedPaidBy)
        val tvAmount: TextView = view.findViewById(R.id.tvSharedAmount)
        val tvCategory: TextView = view.findViewById(R.id.tvSharedCategory)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_shared_expense, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = expenses[position]
        holder.tvDate.text = dateFormat.format(expense.fecha)
        holder.tvDescription.text = expense.descripcion
        holder.tvCategory.text = expense.categoria
        holder.tvAmount.text = "$ ${String.format("%.2f", expense.cantidad)}"

        // Obtener nombre del usuario que pagó
        if (userNames.containsKey(expense.usuarioPagador)) {
            holder.tvPaidBy.text = "Pagado por ${userNames[expense.usuarioPagador]}"
        } else {
            // Cargar el nombre de forma asincrónica
            CoroutineScope(Dispatchers.Main).launch {
                val db = AppDatabase.getDatabase(context)
                val user = db.userDao().getUserById(expense.usuarioPagador)
                user?.let {
                    userNames[expense.usuarioPagador] = it.nombre
                    holder.tvPaidBy.text = "Pagado por ${it.nombre}"
                }
            }
        }

        holder.itemView.setOnClickListener { onItemClick(expense) }
    }

    override fun getItemCount() = expenses.size

    fun updateData(newExpenses: List<SharedExpense>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}
