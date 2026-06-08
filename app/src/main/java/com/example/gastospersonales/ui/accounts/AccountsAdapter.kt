package com.example.gastospersonales.ui.accounts

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.gastospersonales.data.entities.Account
import com.example.gastospersonales.databinding.ItemAccountBinding

class AccountsAdapter(private val onItemClick: (Account) -> Unit) :
    ListAdapter<Account, AccountsAdapter.AccountViewHolder>(AccountDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AccountViewHolder {
        val binding = ItemAccountBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return AccountViewHolder(binding)
    }

    override fun onBindViewHolder(holder: AccountViewHolder, position: Int) {
        val account = getItem(position)
        holder.bind(account)
    }

    inner class AccountViewHolder(private val binding: ItemAccountBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(account: Account) {
            binding.tvAccountName.text = account.name
            if (account.iconRes != 0) {
                binding.ivAccountIcon.setImageResource(account.iconRes)
            } else {
                // Icono por defecto si no tiene uno asignado
                binding.ivAccountIcon.setImageResource(android.R.drawable.ic_menu_agenda)
            }
            binding.root.setOnClickListener { onItemClick(account) }
        }
    }

    class AccountDiffCallback : DiffUtil.ItemCallback<Account>() {
        override fun areItemsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Account, newItem: Account): Boolean {
            return oldItem == newItem
        }
    }
}
