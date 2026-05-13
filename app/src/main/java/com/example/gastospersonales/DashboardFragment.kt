package com.example.gastospersonales

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

        // Configurar RecyclerView
        adapter = MovementAdapter(emptyList()) { movement ->
            val intent = Intent(requireContext(), EditMovementActivity::class.java)
            // Aquí se pasaría el ID si tuviéramos la lógica de edición completa
            startActivity(intent)
        }
        rvRecent.layoutManager = LinearLayoutManager(requireContext())
        rvRecent.adapter = adapter

        // Conectar a la Base de Datos
        val db = AppDatabase.getDatabase(requireContext())
        val dao = db.movementDao()

        // Observar datos en tiempo real
        lifecycleScope.launch {
            // Saldo Total
            dao.getBalance().collectLatest { balance ->
                tvTotalBalance.text = "$ ${String.format(Locale.US, "%.2f", balance ?: 0.0)}"
            }
        }

        lifecycleScope.launch {
            // Ingresos Totales
            dao.getTotalIncome().collectLatest { income ->
                tvIncome.text = "+$ ${String.format(Locale.US, "%.2f", income ?: 0.0)}"
            }
        }

        lifecycleScope.launch {
            // Gastos Totales
            dao.getTotalExpense().collectLatest { expense ->
                tvExpense.text = "-$ ${String.format(Locale.US, "%.2f", expense ?: 0.0)}"
            }
        }

        lifecycleScope.launch {
            // Movimientos Recientes (últimos 5)
            dao.getRecent(5).collectLatest { movements ->
                adapter.updateData(movements)
            }
        }
    }
}