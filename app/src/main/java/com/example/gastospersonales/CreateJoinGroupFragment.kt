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

        val etGroupName = view.findViewById<TextInputEditText>(R.id.etGroupName)
        val etGroupDescription = view.findViewById<TextInputEditText>(R.id.etGroupDescription)
        val etGroupCode = view.findViewById<TextInputEditText>(R.id.etGroupCode)
        val btnConfirm = view.findViewById<Button>(R.id.btnConfirmGroup)
        val btnBack = view.findViewById<Button>(R.id.btnBackToList)

        // Obtener userId
        val sharedPref = requireContext().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        val userIdInt = sharedPref.getInt("user_id", -1)
        currentUserId = userIdInt

        // Obtener modo desde argumentos
        isCreating = arguments?.getBoolean("IS_CREATING", true) ?: true

        // Actualizar UI según modo
        updateUIMode(isCreating, rbCreate, rbJoin, etGroupName, etGroupDescription, etGroupCode)

        radioGroupMode.setOnCheckedChangeListener { _, checkedId ->
            isCreating = checkedId == R.id.rbCreate
            updateUIMode(isCreating, rbCreate, rbJoin, etGroupName, etGroupDescription, etGroupCode)
        }

        btnConfirm.setOnClickListener {
            android.util.Log.d("CREATE_GROUP", "Confirm button clicked. isCreating: $isCreating")
            val userIdStr = if (currentUserId != -1) currentUserId.toString() else "guest"
            
            if (isCreating) {
                val nombre = etGroupName.text.toString().trim()
                if (nombre.isEmpty()) {
                    Toast.makeText(requireContext(), "Nombre obligatorio", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                android.util.Log.d("CREATE_GROUP", "Calling createGroup for: $nombre")
                try {
                    viewModel.createGroup(nombre, userIdStr) {
                        android.util.Log.d("CREATE_GROUP", "Group created successfully in Firestore")
                        Toast.makeText(requireContext(), "Grupo creado con éxito", Toast.LENGTH_SHORT).show()
                        (activity as? GroupManagementActivity)?.switchToList()
                    }
                } catch (e: Exception) {
                    android.util.Log.e("CREATE_GROUP", "Error calling createGroup", e)
                    Toast.makeText(requireContext(), "Error al crear grupo: ${e.message}", Toast.LENGTH_LONG).show()
                }
            } else {
                val codigo = etGroupCode.text.toString().trim().uppercase()
                if (codigo.isEmpty()) {
                    Toast.makeText(requireContext(), "Código obligatorio", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
                android.util.Log.d("JOIN_GROUP", "Calling joinGroup for code: $codigo")
                try {
                    viewModel.joinGroup(codigo, userIdStr) { success ->
                        android.util.Log.d("JOIN_GROUP", "Join result: $success")
                        if (success) {
                            Toast.makeText(requireContext(), "Te uniste al grupo", Toast.LENGTH_SHORT).show()
                            (activity as? GroupManagementActivity)?.switchToList()
                        } else {
                            Toast.makeText(requireContext(), "Código inválido o grupo no encontrado", Toast.LENGTH_SHORT).show()
                        }
                    }
                } catch (e: Exception) {
                    android.util.Log.e("JOIN_GROUP", "Error calling joinGroup", e)
                    Toast.makeText(requireContext(), "Error al unirse: ${e.message}", Toast.LENGTH_LONG).show()
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
        etName: TextInputEditText,
        etDesc: TextInputEditText,
        etCode: TextInputEditText
    ) {
        rbCreate.isChecked = isCreating
        rbJoin.isChecked = !isCreating

        if (isCreating) {
            etName.visibility = View.VISIBLE
            etDesc.visibility = View.VISIBLE
            etCode.visibility = View.GONE
        } else {
            etName.visibility = View.GONE
            etDesc.visibility = View.GONE
            etCode.visibility = View.VISIBLE
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
