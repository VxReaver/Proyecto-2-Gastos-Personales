package com.example.gastospersonales.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.PopupMenu
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gastospersonales.R
import com.example.gastospersonales.data.entities.Account
import com.example.gastospersonales.databinding.FragmentAccountsBinding
import com.google.android.material.snackbar.Snackbar

class AccountsFragment : Fragment() {

    private var _binding: FragmentAccountsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AccountsViewModel by viewModels()
    private lateinit var adapter: AccountsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAccountsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupToolbar() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            when (menuItem.itemId) {
                R.id.action_add -> {
                    showAccountDialog(null)
                    true
                }
                else -> false
            }
        }
    }

    private fun setupRecyclerView() {
        adapter = AccountsAdapter { account ->
            showPopupMenu(binding.rvAccounts.findViewHolderForItemId(account.id.toLong())?.itemView ?: binding.root, account)
        }
        binding.rvAccounts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAccounts.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.allAccounts.observe(viewLifecycleOwner) { accounts ->
            adapter.submitList(accounts)
        }
    }

    private fun showPopupMenu(view: View, account: Account) {
        val popup = PopupMenu(requireContext(), view)
        popup.menu.add("Modificar")
        
        // Regla de negocio: Solo mostrar eliminar si no tiene movimientos
        if (!account.hasMovements) {
            popup.menu.add("Eliminar")
        }

        popup.setOnMenuItemClickListener { item ->
            when (item.title) {
                "Modificar" -> {
                    showAccountDialog(account.id)
                    true
                }
                "Eliminar" -> {
                    deleteAccount(account)
                    true
                }
                else -> false
            }
        }
        popup.show()
    }

    private fun deleteAccount(account: Account) {
        viewModel.delete(account)
        Snackbar.make(binding.root, "Cuenta eliminada", Snackbar.LENGTH_LONG)
            .setAction("Deshacer") {
                viewModel.undoDelete()
            }
            .show()
    }

    private fun showAccountDialog(accountId: Int?) {
        val dialog = AccountDialogFragment.newInstance(accountId)
        dialog.show(childFragmentManager, "AccountDialog")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
