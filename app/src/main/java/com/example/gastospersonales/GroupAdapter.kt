package com.example.gastospersonales

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.gastospersonales.data.entities.Group
import java.text.SimpleDateFormat
import java.util.Locale

class GroupAdapter(
    private var groups: List<Group>,
    private val onItemClick: (Group) -> Unit
) : RecyclerView.Adapter<GroupAdapter.ViewHolder>() {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("es", "ES"))

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvGroupName: TextView = view.findViewById(R.id.tvGroupName)
        val tvGroupCode: TextView = view.findViewById(R.id.tvGroupCode)
        val tvGroupDate: TextView = view.findViewById(R.id.tvGroupDate)
        val tvGroupDescription: TextView = view.findViewById(R.id.tvGroupDescription)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_group, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = groups[position]
        holder.tvGroupName.text = group.nombre
        holder.tvGroupCode.text = "Código: ${group.codigoUnico}"
        holder.tvGroupDate.text = dateFormat.format(group.fechaCreacion)
        holder.tvGroupDescription.text = group.descripcion.ifEmpty { "Sin descripción" }

        holder.itemView.setOnClickListener { onItemClick(group) }
    }

    override fun getItemCount() = groups.size

    fun updateData(newGroups: List<Group>) {
        groups = newGroups
        notifyDataSetChanged()
    }
}
