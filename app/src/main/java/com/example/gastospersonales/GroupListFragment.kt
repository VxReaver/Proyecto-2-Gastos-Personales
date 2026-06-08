package com.example.gastospersonales

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gastospersonales.data.AppDatabase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GroupListFragment : Fragment() {

    private lateinit var adapter: GroupAdapter
    private var currentUserId = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_group_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val rvGroups = view.findViewById<RecyclerView>(R.id.rvGroups)
        val btnCreateGroup = view.findViewById<Button>(R.id.btnCreateGroup)
        val btnJoinGroup = view.findViewById<Button>(R.id.btnJoinGroup)

        // Obtener userId de sesión
        val sharedPref = requireContext().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        currentUserId = sharedPref.getInt("user_id", -1)

        // Configurar RecyclerView
        adapter = GroupAdapter(emptyList()) { group ->
            val intent = Intent(requireContext(), GroupPanelActivity::class.java)
            intent.putExtra("GROUP_ID", group.id)
            startActivity(intent)
        }
        rvGroups.layoutManager = LinearLayoutManager(requireContext())
        rvGroups.adapter = adapter

        // Botones
        btnCreateGroup.setOnClickListener {
            (parentFragment as? GroupManagementFragment)?.switchToCreateJoin(true)
        }

        btnJoinGroup.setOnClickListener {
            (parentFragment as? GroupManagementFragment)?.switchToCreateJoin(false)
        }

        // Observar grupos del usuario
        if (currentUserId != -1) {
            observeGroups()
        }
    }

    private fun observeGroups() {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            db.groupDao().getGroupsOfMember(currentUserId).collectLatest { groups ->
                adapter.updateData(groups)
            }
        }
    }
}
