package com.example.gastospersonales.ui.accounts

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.gastospersonales.R
import com.example.gastospersonales.data.entities.Account
import com.example.gastospersonales.databinding.DialogAccountOptionsBinding
import com.example.gastospersonales.databinding.FragmentAccountsBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch

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
        
        val sharedPref = requireContext().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)
        viewModel.setUserId(userId)

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
            showOptionsDialog(account)
        }
        binding.rvAccounts.layoutManager = LinearLayoutManager(requireContext())
        binding.rvAccounts.adapter = adapter
    }

    private fun observeViewModel() {
        viewModel.allAccounts.observe(viewLifecycleOwner) { accounts ->
            adapter.submitList(accounts)
        }
    }

    private fun showOptionsDialog(account: Account) {
        val dialogBinding = DialogAccountOptionsBinding.inflate(layoutInflater)
        val dialog = MaterialAlertDialogBuilder(requireContext(), R.style.Theme_GastosPersonales_Dialog)
            .setView(dialogBinding.root)
            .create()

        dialogBinding.tvOptionTitle.text = account.name
        
        viewLifecycleOwner.lifecycleScope.launch {
            val canDelete = viewModel.canDeleteAccount(account.name)
            dialogBinding.btnOptionDelete.visibility = if (canDelete) View.VISIBLE else View.GONE
            
            dialogBinding.btnOptionEdit.setOnClickListener {
                dialog.dismiss()
                showAccountDialog(account.id)
            }

            dialogBinding.btnOptionDelete.setOnClickListener {
                dialog.dismiss()
                showDeleteConfirmation(account)
            }

            dialogBinding.btnOptionCancel.setOnClickListener {
                dialog.dismiss()
            }

            dialog.show()
            
            // Hacer el diálogo más ancho
            val width = (resources.displayMetrics.widthPixels * 0.85).toInt()
            dialog.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
            dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

    private fun showDeleteConfirmation(account: Account) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("¿Eliminar cuenta?")
            .setMessage("Esta acción no se puede deshacer si existen datos vinculados.")
            .setPositiveButton("Eliminar") { _, _ -> deleteAccount(account) }
            .setNegativeButton("Cancelar", null)
            .show()
    }

    private fun deleteAccount(account: Account) {
        viewModel.delete(account)
        Snackbar.make(binding.root, "Cuenta '${account.name}' eliminada", Snackbar.LENGTH_LONG)
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
