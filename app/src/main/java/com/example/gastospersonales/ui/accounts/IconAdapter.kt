package com.example.gastospersonales.ui.accounts

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.gastospersonales.databinding.ItemIconSelectorBinding

class IconAdapter(
    private val icons: List<Int>,
    private var selectedIcon: Int,
    private val onIconSelected: (Int) -> Unit
) : RecyclerView.Adapter<IconAdapter.IconViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): IconViewHolder {
        val binding = ItemIconSelectorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return IconViewHolder(binding)
    }

    override fun onBindViewHolder(holder: IconViewHolder, position: Int) {
        holder.bind(icons[position])
    }

    override fun getItemCount(): Int = icons.size

    inner class IconViewHolder(private val binding: ItemIconSelectorBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(iconRes: Int) {
            binding.ivIcon.setImageResource(iconRes)
            binding.ivSelected.visibility = if (iconRes == selectedIcon) View.VISIBLE else View.GONE
            
            binding.root.setOnClickListener {
                val oldSelected = selectedIcon
                selectedIcon = iconRes
                onIconSelected(iconRes)
                
                // Actualizar visualmente la selección
                notifyItemChanged(icons.indexOf(oldSelected))
                notifyItemChanged(icons.indexOf(selectedIcon))
            }
        }
    }
}
