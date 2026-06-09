package com.example.gastospersonales

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gastospersonales.data.entities.GroupFirestore
import com.example.gastospersonales.ui.GroupViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GroupListFragment : Fragment() {

    private lateinit var adapter: GroupFirestoreAdapter
    private lateinit var viewModel: GroupViewModel
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
        viewModel = ViewModelProvider(requireActivity())[GroupViewModel::class.java]

        val rvGroups = view.findViewById<RecyclerView>(R.id.rvGroups)
        val btnCreateGroup = view.findViewById<Button>(R.id.btnCreateGroup)
        val btnJoinGroup = view.findViewById<Button>(R.id.btnJoinGroup)

        val sharedPref = requireContext().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        val userIdInt = sharedPref.getInt("user_id", -1)
        currentUserId = userIdInt
        val userIdStr = if (userIdInt != -1) userIdInt.toString() else "guest"

        adapter = GroupFirestoreAdapter(emptyList()) { group ->
            val intent = android.content.Intent(requireContext(), GroupPanelActivity::class.java).apply {
                putExtra("GROUP_ID_STR", group.id)
                putExtra("GROUP_NAME", group.name)
            }
            startActivity(intent)
        }
        rvGroups.layoutManager = LinearLayoutManager(requireContext())
        rvGroups.adapter = adapter

        btnCreateGroup.setOnClickListener {
            (activity as? GroupManagementActivity)?.switchToCreateJoin(true)
        }

        btnJoinGroup.setOnClickListener {
            (activity as? GroupManagementActivity)?.switchToCreateJoin(false)
        }

        lifecycleScope.launch {
            viewModel.loadUserGroups(userIdStr)
            viewModel.userGroups.collectLatest { groups ->
                adapter.updateData(groups)
            }
        }
    }
}

class GroupFirestoreAdapter(
    private var groups: List<GroupFirestore>,
    private val onItemClick: (GroupFirestore) -> Unit
) : RecyclerView.Adapter<GroupFirestoreAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvGroupName)
        val tvCode: TextView = view.findViewById(R.id.tvGroupCode)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_group, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val group = groups[position]
        holder.tvName.text = group.name
        holder.tvCode.text = "Código: ${group.inviteCode}"
        holder.itemView.setOnClickListener { onItemClick(group) }
    }

    override fun getItemCount() = groups.size

    fun updateData(newGroups: List<GroupFirestore>) {
        groups = newGroups
        notifyDataSetChanged()
    }
}
