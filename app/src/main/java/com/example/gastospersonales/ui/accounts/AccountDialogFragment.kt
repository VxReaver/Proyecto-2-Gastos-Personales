package com.example.gastospersonales.ui.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.gastospersonales.R
import com.example.gastospersonales.data.entities.Account
import com.example.gastospersonales.databinding.DialogAccountBinding
import kotlinx.coroutines.launch

class AccountDialogFragment : DialogFragment() {

    private var _binding: DialogAccountBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AccountsViewModel by viewModels()
    
    private var accountId: Int? = null
    private var selectedIcon: Int = R.drawable.ic_wallet // Icono por defecto

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            if (it.containsKey(ARG_ACCOUNT_ID)) {
                accountId = it.getInt(ARG_ACCOUNT_ID)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogAccountBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupIconSelector()
        setupButtons()

        if (accountId != null) {
            loadAccountData(accountId!!)
        }
    }

    private fun setupIconSelector() {
        // Lista de iconos disponibles en el proyecto
        val icons = listOf(
            R.drawable.ic_wallet,
            R.drawable.ic_accounts,
            R.drawable.ic_calendar,
            R.drawable.ic_movements,
            R.drawable.ic_categories,
            R.drawable.ic_person,
            R.drawable.ic_info
        )

        val iconAdapter = IconAdapter(icons, selectedIcon) { icon ->
            selectedIcon = icon
        }
        binding.rvIconSelector.adapter = iconAdapter
    }

    private fun loadAccountData(id: Int) {
        binding.tvDialogTitle.text = "Modificar Cuenta"
        lifecycleScope.launch {
            val account = viewModel.getAccountById(id)
            account?.let {
                binding.etAccountName.setText(it.name)
                selectedIcon = it.iconRes
                // Refrescar selector de iconos si es necesario
                (binding.rvIconSelector.adapter as? IconAdapter)?.let { adapter ->
                    // Aquí se podría implementar una lógica para actualizar el item seleccionado en el adapter
                }
            }
        }
    }

    private fun setupButtons() {
        binding.btnCancel.setOnClickListener { dismiss() }
        
        binding.btnSave.setOnClickListener {
            val name = binding.etAccountName.text.toString().trim()
            
            if (name.isEmpty()) {
                binding.tilAccountName.error = "El nombre es obligatorio"
                return@setOnClickListener
            }

            if (accountId == null) {
                // Agregar
                val newAccount = Account(
                    name = name,
                    iconRes = selectedIcon,
                    userId = 0 // Ajustar según lógica de sesión
                )
                viewModel.insert(newAccount)
                Toast.makeText(requireContext(), "Cuenta creada", Toast.LENGTH_SHORT).show()
            } else {
                // Modificar - Para simplificar, recuperamos y actualizamos
                lifecycleScope.launch {
                    val existing = viewModel.getAccountById(accountId!!)
                    existing?.let {
                        val updated = it.copy(name = name, iconRes = selectedIcon)
                        viewModel.update(updated)
                        Toast.makeText(requireContext(), "Cuenta actualizada", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        // Ajustar ancho del diálogo
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val ARG_ACCOUNT_ID = "account_id"

        fun newInstance(accountId: Int?): AccountDialogFragment {
            val fragment = AccountDialogFragment()
            val args = Bundle()
            accountId?.let { args.putInt(ARG_ACCOUNT_ID, it) }
            fragment.arguments = args
            return fragment
        }
    }
}
