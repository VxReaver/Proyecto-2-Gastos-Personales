package com.example.gastospersonales

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.gastospersonales.data.AppDatabase
import com.example.gastospersonales.data.entities.Group
import com.example.gastospersonales.data.entities.GroupMember
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.util.Date

import androidx.lifecycle.ViewModelProvider
import com.example.gastospersonales.ui.GroupViewModel

class CreateJoinGroupFragment : Fragment() {

    private var currentUserId = -1
    private var isCreating = true
    private lateinit var viewModel: GroupViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_join_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[GroupViewModel::class.java]
        
        val radioGroupMode = view.findViewById<RadioGroup>(R.id.radioGroupMode)
        val rbCreate = view.findViewById<RadioButton>(R.id.rbCreate)
        val rbJoin = view.findViewById<RadioButton>(R.id.rbJoin)

        val layoutName = view.findViewById<View>(R.id.layout_group_name)
        val layoutDesc = view.findViewById<View>(R.id.layout_group_description)
        val layoutCode = view.findViewById<View>(R.id.layout_group_code)

        val etGroupName = view.findViewById<TextInputEditText>(R.id.etGroupName)
        val etGroupCode = view.findViewById<TextInputEditText>(R.id.etGroupCode)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirmGroup)
        val btnBack = view.findViewById<Button>(R.id.btnBackToList)

        // Obtener userId
        val sharedPref = requireContext().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        val userIdInt = sharedPref.getInt("user_id", -1)
        currentUserId = userIdInt

        // Obtener modo desde argumentos
        isCreating = arguments?.getBoolean("IS_CREATING", true) ?: true

        // Actualizar UI según modo inicial
        updateUIMode(isCreating, rbCreate, rbJoin, layoutName, layoutDesc, layoutCode)

        radioGroupMode.setOnCheckedChangeListener { _, checkedId ->
            isCreating = checkedId == R.id.rbCreate
            updateUIMode(isCreating, rbCreate, rbJoin, layoutName, layoutDesc, layoutCode)
        }

        btnConfirm.setOnClickListener {
            val userIdStr = if (currentUserId != -1) currentUserId.toString() else "guest"
            
            if (isCreating) {
                val nombre = etGroupName.text.toString().trim()
                if (nombre.isEmpty()) {
                    Toast.makeText(requireContext(), "Nombre obligatorio", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.createGroup(nombre, userIdStr) {
                    Toast.makeText(requireContext(), "Grupo creado con éxito", Toast.LENGTH_SHORT).show()
                    (activity as? GroupManagementActivity)?.switchToList()
                }
            } else {
                val codigo = etGroupCode.text.toString().trim().uppercase()
                if (codigo.isEmpty()) {
                    Toast.makeText(requireContext(), "Código obligatorio", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                viewModel.joinGroup(codigo, userIdStr) { success ->
                    if (success) {
                        Toast.makeText(requireContext(), "Te uniste al grupo", Toast.LENGTH_SHORT).show()
                        (activity as? GroupManagementActivity)?.switchToList()
                    } else {
                        Toast.makeText(requireContext(), "Código inválido o grupo no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnBack.setOnClickListener {
            (activity as? GroupManagementActivity)?.switchToList()
        }
    }

    private fun updateUIMode(
        isCreating: Boolean,
        rbCreate: RadioButton,
        rbJoin: RadioButton,
        layoutName: View,
        layoutDesc: View,
        layoutCode: View
    ) {
        rbCreate.isChecked = isCreating
        rbJoin.isChecked = !isCreating

        if (isCreating) {
            layoutName.visibility = View.VISIBLE
            layoutDesc.visibility = View.VISIBLE
            layoutCode.visibility = View.GONE
        } else {
            layoutName.visibility = View.GONE
            layoutDesc.visibility = View.GONE
            layoutCode.visibility = View.VISIBLE
        }
    }

    companion object {
        fun newInstance(isCreating: Boolean): CreateJoinGroupFragment {
            return CreateJoinGroupFragment().apply {
                arguments = Bundle().apply {
                    putBoolean("IS_CREATING", isCreating)
                }
            }
        }
    }
}
