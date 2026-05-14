package com.example.gastospersonales

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
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class DashboardFragment : Fragment() {

    private lateinit var categoryAdapter: CategoryReportAdapter
    
    private var currentAccount = "Todas"
    private var currentYear = Calendar.getInstance().get(Calendar.YEAR)
    private var currentMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    
    private var dataJob: Job? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_dashboard, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSpinners(view)
        setupActionButtons(view)
        setupRecyclerView(view)
        observeData(view)
    }

    private fun setupSpinners(view: View) {
        val spinnerAccount = view.findViewById<AutoCompleteTextView>(R.id.spinner_account)
        val spinnerYear = view.findViewById<AutoCompleteTextView>(R.id.spinner_year)
        val spinnerMonth = view.findViewById<AutoCompleteTextView>(R.id.spinner_month)

        val accounts = arrayOf("Todas", "Efectivo", "T. Débito", "T. Crédito", "Vales")
        spinnerAccount.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, accounts))
        spinnerAccount.setText(currentAccount, false)
        spinnerAccount.setOnItemClickListener { _, _, position, _ ->
            currentAccount = accounts[position]
            observeData(view)
        }

        val years = (2020..2030).map { it.toString() }.toTypedArray()
        spinnerYear.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, years))
        spinnerYear.setText(currentYear.toString(), false)
        spinnerYear.setOnItemClickListener { _, _, position, _ ->
            currentYear = years[position].toInt()
            observeData(view)
        }

        val months = arrayOf("Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", 
                             "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre")
        spinnerMonth.setAdapter(ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, months))
        spinnerMonth.setText(months[currentMonth - 1], false)
        spinnerMonth.setOnItemClickListener { _, _, position, _ ->
            currentMonth = position + 1
            observeData(view)
        }
    }

    private fun setupActionButtons(view: View) {
        view.findViewById<View>(R.id.btn_action_income).setOnClickListener { openAddMovement("Ingreso") }
        view.findViewById<View>(R.id.btn_action_expense).setOnClickListener { openAddMovement("Gasto") }
        view.findViewById<View>(R.id.btn_action_transfer).setOnClickListener { openAddMovement("Transferencia") }
    }

    private fun openAddMovement(type: String) {
        val intent = Intent(requireContext(), AddMovementActivity::class.java)
        intent.putExtra("PRESELECTED_TYPE", type)
        startActivity(intent)
    }

    private fun setupRecyclerView(view: View) {
        val rvCategorySummary = view.findViewById<RecyclerView>(R.id.rv_category_summary)
        categoryAdapter = CategoryReportAdapter(emptyList()) { categoryName ->
            val intent = Intent(requireContext(), CategoryDetailActivity::class.java)
            intent.putExtra("CATEGORY_NAME", categoryName)
            startActivity(intent)
        }
        rvCategorySummary.layoutManager = LinearLayoutManager(requireContext())
        rvCategorySummary.adapter = categoryAdapter
    }

    private fun observeData(view: View) {
        dataJob?.cancel()
        dataJob = lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(requireContext()).movementDao()

            val tvIncome = view.findViewById<TextView>(R.id.tv_monthly_income)
            val tvExpense = view.findViewById<TextView>(R.id.tv_monthly_expense)
            val tvPrevBalance = view.findViewById<TextView>(R.id.tv_previous_balance)
            val tvCurrBalance = view.findViewById<TextView>(R.id.tv_current_balance)

            val calendar = Calendar.getInstance()
            calendar.set(currentYear, currentMonth - 1, 1, 0, 0, 0)
            val startDate = calendar.timeInMillis

            combine(
                dao.getMonthlyIncome(currentAccount, currentYear, currentMonth),
                dao.getMonthlyExpense(currentAccount, currentYear, currentMonth),
                dao.getPreviousBalance(currentAccount, startDate)
            ) { income, expense, prev ->
                val inc = income ?: 0.0
                val exp = expense ?: 0.0
                val pr = prev ?: 0.0
                val curr = pr + inc - exp
                
                Triple(inc, exp, pr to curr)
            }.collectLatest { (inc, exp, balances) ->
                val (pr, curr) = balances
                tvIncome.text = String.format(Locale.US, "$ %.2f", inc)
                tvExpense.text = String.format(Locale.US, "$ %.2f", exp)
                tvPrevBalance.text = String.format(Locale.US, "$ %.2f", pr)
                tvCurrBalance.text = String.format(Locale.US, "$ %.2f", curr)
            }
        }

        lifecycleScope.launch {
            val dao = AppDatabase.getDatabase(requireContext()).movementDao()
            dao.getCategoryReport(currentAccount, currentYear, currentMonth).collectLatest { report ->
                categoryAdapter.updateData(report)
            }
        }
    }
}
