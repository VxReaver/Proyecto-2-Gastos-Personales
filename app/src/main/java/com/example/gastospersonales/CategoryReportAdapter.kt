package com.example.gastospersonales

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gastospersonales.data.dao.CategorySum
import com.example.gastospersonales.databinding.ItemCategoryReportBinding
import java.text.NumberFormat
import java.util.Locale

class CategoryReportAdapter(
    private var items: List<CategorySum>,
    private val onItemClick: (String) -> Unit // Añadimos el click listener
) : RecyclerView.Adapter<CategoryReportAdapter.ViewHolder>() {

    private val formatter = NumberFormat.getCurrencyInstance(Locale.US)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemCategoryReportBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val totalGlobal = items.sumOf { it.total }
        holder.bind(items[position], totalGlobal)
    }

    override fun getItemCount() = items.size

    fun updateData(newItems: List<CategorySum>) {
        this.items = newItems
        notifyDataSetChanged()
    }

    inner class ViewHolder(private val binding: ItemCategoryReportBinding) : 
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CategorySum, totalGlobal: Double) {
            binding.tvCategoryName.text = item.categoria
            binding.tvCategoryAmount.text = formatter.format(item.total)
            
            val percentage = if (totalGlobal > 0) (item.total / totalGlobal * 100).toInt() else 0
            binding.progressCategory.progress = percentage
            binding.tvPercentage.text = "$percentage%"

            // Al hacer clic, enviamos el nombre de la categoría
            binding.root.setOnClickListener { onItemClick(item.categoria) }
        }
    }
}
