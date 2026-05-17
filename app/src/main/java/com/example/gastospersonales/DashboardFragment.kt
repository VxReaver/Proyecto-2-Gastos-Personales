package com.example.gastospersonales

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gastospersonales.data.AppDatabase
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Locale

class DashboardFragment : Fragment() {

    private lateinit var adapter: MovementAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvTotalBalance = view.findViewById<TextView>(R.id.tvTotalBalance)
        val tvIncome = view.findViewById<TextView>(R.id.tvIncome)
        val tvExpense = view.findViewById<TextView>(R.id.tvExpense)
        val rvRecent = view.findViewById<RecyclerView>(R.id.rvRecentMovements)

        // Obtener el userId de la sesión
        val sharedPref = requireContext().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        // Configurar RecyclerView
        adapter = MovementAdapter(emptyList()) { movement ->
            val intent = Intent(requireContext(), EditMovementActivity::class.java)
            intent.putExtra("MOVEMENT_ID", movement.id)
            startActivity(intent)
        }
        rvRecent.layoutManager = LinearLayoutManager(requireContext())
        rvRecent.adapter = adapter

        val db = AppDatabase.getDatabase(requireContext())
        val dao = db.movementDao()

        // Observar datos filtrados por userId
        lifecycleScope.launch {
            dao.getBalance(userId).collectLatest { balance ->
                tvTotalBalance.text = "$ ${String.format(Locale.US, "%.2f", balance ?: 0.0)}"
            }
        }

        lifecycleScope.launch {
            dao.getTotalIncome(userId).collectLatest { income ->
                tvIncome.text = "+$ ${String.format(Locale.US, "%.2f", income ?: 0.0)}"
            }
        }

        lifecycleScope.launch {
            dao.getTotalExpense(userId).collectLatest { expense ->
                tvExpense.text = "-$ ${String.format(Locale.US, "%.2f", expense ?: 0.0)}"
            }
        }

        lifecycleScope.launch {
            dao.getRecent(userId, 5).collectLatest { movements ->
                adapter.updateData(movements)
            }
        }
    }
}