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

class CreateJoinGroupFragment : Fragment() {

    private var currentUserId = -1
    private var isCreating = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_create_join_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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
        currentUserId = sharedPref.getInt("user_id", -1)

        // Obtener modo desde argumentos
        isCreating = arguments?.getBoolean("IS_CREATING", true) ?: true

        // Actualizar UI según modo
        updateUIMode(isCreating, rbCreate, rbJoin, etGroupName, etGroupDescription, etGroupCode)

        radioGroupMode.setOnCheckedChangeListener { _, checkedId ->
            isCreating = checkedId == R.id.rbCreate
            updateUIMode(isCreating, rbCreate, rbJoin, etGroupName, etGroupDescription, etGroupCode)
        }

        btnConfirm.setOnClickListener {
            if (isCreating) {
                createGroup(
                    etGroupName.text.toString(),
                    etGroupDescription.text.toString()
                )
            } else {
                joinGroup(etGroupCode.text.toString())
            }
        }

        btnBack.setOnClickListener {
            (parentFragment as? GroupManagementFragment)?.switchToList()
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

    private fun createGroup(nombre: String, descripcion: String) {
        if (nombre.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor ingresa el nombre del grupo", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentUserId == -1) {
            Toast.makeText(requireContext(), "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val code = CodeGenerator.generateGroupCode()

            val group = Group(
                nombre = nombre,
                codigoUnico = code,
                usuarioCreador = currentUserId,
                fechaCreacion = Date(),
                descripcion = descripcion
            )

            db.groupDao().insert(group)

            // Obtener el grupo creado para agregar al usuario como miembro
            val createdGroup = db.groupDao().getByCode(code)
            createdGroup?.let {
                val member = GroupMember(
                    groupId = it.id,
                    userId = currentUserId,
                    fechaUnion = Date()
                )
                db.groupMemberDao().insert(member)
            }

            Toast.makeText(requireContext(), "Grupo creado con código: $code", Toast.LENGTH_SHORT).show()
            (parentFragment as? GroupManagementFragment)?.switchToList()
        }
    }

    private fun joinGroup(codigo: String) {
        if (codigo.isEmpty()) {
            Toast.makeText(requireContext(), "Por favor ingresa el código del grupo", Toast.LENGTH_SHORT).show()
            return
        }

        if (currentUserId == -1) {
            Toast.makeText(requireContext(), "Error: Usuario no identificado", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val group = db.groupDao().getByCode(codigo.uppercase())

            if (group == null) {
                Toast.makeText(requireContext(), "Código de grupo no encontrado", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val isMember = db.groupMemberDao().isMember(group.id, currentUserId)
            if (isMember > 0) {
                Toast.makeText(requireContext(), "Ya eres miembro de este grupo", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val member = GroupMember(
                groupId = group.id,
                userId = currentUserId,
                fechaUnion = Date()
            )
            db.groupMemberDao().insert(member)

            Toast.makeText(requireContext(), "Te uniste al grupo ${group.nombre}", Toast.LENGTH_SHORT).show()
            (parentFragment as? GroupManagementFragment)?.switchToList()
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
