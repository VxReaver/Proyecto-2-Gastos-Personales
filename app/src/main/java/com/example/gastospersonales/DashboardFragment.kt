package com.example.gastospersonales

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gastospersonales.data.AppDatabase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class DashboardFragment : Fragment() {

    private lateinit var adapter: MovementAdapter
    private var currentAccount = "Todas"
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1

    private var incomeJob: Job? = null
    private var expenseJob: Job? = null
    private var balanceJob: Job? = null
    private var movementsJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Referencias a los componentes de la UI
        val tvCurrentBalance = view.findViewById<TextView>(R.id.tv_current_balance)
        val tvMonthlyIncome = view.findViewById<TextView>(R.id.tv_monthly_income)
        val tvMonthlyExpense = view.findViewById<TextView>(R.id.tv_monthly_expense)
        val rvCategorySummary = view.findViewById<RecyclerView>(R.id.rv_category_summary)

        val spinnerAccount = view.findViewById<AutoCompleteTextView>(R.id.spinner_account)
        val spinnerYear = view.findViewById<AutoCompleteTextView>(R.id.spinner_year)
        val spinnerMonth = view.findViewById<AutoCompleteTextView>(R.id.spinner_month)

        // Botones de acción rápida
        view.findViewById<View>(R.id.btn_action_income).setOnClickListener {
            startActivity(Intent(requireContext(), AddMovementActivity::class.java))
        }
        view.findViewById<View>(R.id.btn_action_expense).setOnClickListener {
            startActivity(Intent(requireContext(), AddMovementActivity::class.java))
        }
        view.findViewById<View>(R.id.btn_action_transfer).setOnClickListener {
            startActivity(Intent(requireContext(), AddMovementActivity::class.java))
        }

        // Obtener el userId de la sesión
        val sharedPref = requireContext().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        // Configurar RecyclerView
        adapter = MovementAdapter(emptyList()) { movement ->
            val intent = Intent(requireContext(), EditMovementActivity::class.java)
            intent.putExtra("MOVEMENT_ID", movement.id)
            startActivity(intent)
        }
        rvCategorySummary.layoutManager = LinearLayoutManager(requireContext())
        rvCategorySummary.adapter = adapter

        // Configurar Filtros
        setupFilters(spinnerAccount, spinnerYear, spinnerMonth) {
            observeData(userId, tvCurrentBalance, tvMonthlyIncome, tvMonthlyExpense)
        }

        // Iniciar observación inicial
        observeData(userId, tvCurrentBalance, tvMonthlyIncome, tvMonthlyExpense)
    }

    private fun setupFilters(
        sAcc: AutoCompleteTextView,
        sYear: AutoCompleteTextView,
        sMonth: AutoCompleteTextView,
        onFilterChanged: () -> Unit
    ) {
        val accounts = arrayOf("Todas", "Efectivo", "Tarjeta Débito", "Tarjeta Crédito", "Ahorros")
        sAcc.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, accounts))
        sAcc.setText(currentAccount, false)
        sAcc.setOnItemClickListener { _, _, pos, _ ->
            currentAccount = accounts[pos]
            onFilterChanged()
        }

        val years = (2020..2030).map { it.toString() }.toTypedArray()
        sYear.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, years))
        sYear.setText(currentYear.toString(), false)
        sYear.setOnItemClickListener { _, _, pos, _ ->
            currentYear = years[pos].toInt()
            onFilterChanged()
        }

        val months = arrayOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                             "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        sMonth.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, months))
        sMonth.setText(months[currentMonth - 1], false)
        sMonth.setOnItemClickListener { _, _, pos, _ ->
            currentMonth = pos + 1
            onFilterChanged()
        }
    }

    private fun observeData(userId: Int, tvBalance: TextView, tvIncome: TextView, tvExpense: TextView) {
        val db = AppDatabase.getDatabase(requireContext())
        val dao = db.movementDao()

        // Cancelar observaciones previas para evitar duplicados
        incomeJob?.cancel()
        expenseJob?.cancel()
        balanceJob?.cancel()
        movementsJob?.cancel()

        // Observar Saldo Total (Global del usuario)
        balanceJob = lifecycleScope.launch {
            dao.getBalance(userId).collectLatest { balance ->
                tvBalance.text = "$ ${String.format(Locale.US, "%.2f", balance ?: 0.0)}"
            }
        }

        // Observar Ingresos Filtrados
        incomeJob = lifecycleScope.launch {
            dao.getFilteredIncome(userId, currentAccount, currentYear, currentMonth).collectLatest { income ->
                tvIncome.text = "$ ${String.format(Locale.US, "%.2f", income ?: 0.0)}"
            }
        }

        // Observar Gastos Filtrados
        expenseJob = lifecycleScope.launch {
            dao.getFilteredExpense(userId, currentAccount, currentYear, currentMonth).collectLatest { expense ->
                tvExpense.text = "$ ${String.format(Locale.US, "%.2f", expense ?: 0.0)}"
            }
        }

        // Observar Movimientos Recientes (Filtrados por cuenta/fecha si se desea, o globales)
        movementsJob = lifecycleScope.launch {
            dao.getRecent(userId, 5).collectLatest { movements ->
                adapter.updateData(movements)
            }
        }
    }
}