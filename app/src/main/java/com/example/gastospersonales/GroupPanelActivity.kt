package com.example.gastospersonales

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gastospersonales.data.entities.ExpenseFirestore
import com.example.gastospersonales.ui.GroupViewModel
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class GroupPanelActivity : AppCompatActivity() {

    private lateinit var viewModel: GroupViewModel
    private lateinit var adapter: SharedExpenseFirestoreAdapter
    private var groupId: String? = null
    private var currentUserName: String = "Usuario"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_panel)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        groupId = intent.getStringExtra("GROUP_ID_STR")
        val groupName = intent.getStringExtra("GROUP_NAME") ?: "Grupo"
        supportActionBar?.title = groupName

        val sharedPref = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        currentUserName = sharedPref.getString("user_name", "Usuario") ?: "Usuario"

        if (groupId == null) {
            Toast.makeText(this, "Error: Grupo no encontrado", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        viewModel = ViewModelProvider(this)[GroupViewModel::class.java]

        // UI Components
        val tvGroupName = findViewById<TextView>(R.id.tvGroupNamePanel)
        val tvBalance = findViewById<TextView>(R.id.tvGroupBalance)
        val rvExpenses = findViewById<RecyclerView>(R.id.rvSharedExpenses)
        val etAmount = findViewById<TextInputEditText>(R.id.etExpenseAmount)
        val etDescription = findViewById<TextInputEditText>(R.id.etExpenseDescription)
        val btnAddExpense = findViewById<Button>(R.id.btnAddSharedExpense)

        tvGroupName.text = groupName

        // Adapter
        adapter = SharedExpenseFirestoreAdapter(emptyList())
        rvExpenses.layoutManager = LinearLayoutManager(this)
        rvExpenses.adapter = adapter

        // Add Expense
        btnAddExpense.setOnClickListener {
            val amountStr = etAmount.text.toString()
            val description = etDescription.text.toString()

            if (amountStr.isNotEmpty() && description.isNotEmpty()) {
                val amount = amountStr.toDoubleOrNull() ?: 0.0
                viewModel.addExpense(groupId!!, description, amount, currentUserName)
                etAmount.text?.clear()
                etDescription.text?.clear()
                Toast.makeText(this, "Gasto registrado", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            }
        }

        // Observe Data
        lifecycleScope.launch {
            viewModel.selectGroup(groupId!!)
            viewModel.selectedGroupExpenses.collectLatest { expenses ->
                adapter.updateData(expenses)
                val total = expenses.sumOf { it.amount }
                tvBalance.text = "Balance General: $ ${String.format("%.2f", total)}"
            }
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}

class SharedExpenseFirestoreAdapter(
    private var expenses: List<ExpenseFirestore>
) : RecyclerView.Adapter<SharedExpenseFirestoreAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvDesc: TextView = view.findViewById(R.id.tvSharedDescription)
        val tvAmount: TextView = view.findViewById(R.id.tvSharedAmount)
        val tvPaidBy: TextView = view.findViewById(R.id.tvSharedPaidBy)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_shared_expense, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val expense = expenses[position]
        holder.tvDesc.text = expense.description
        holder.tvAmount.text = "$ ${String.format("%.2f", expense.amount)}"
        holder.tvPaidBy.text = "Pagado por: ${expense.paidBy}"
    }

    override fun getItemCount() = expenses.size

    fun updateData(newExpenses: List<ExpenseFirestore>) {
        expenses = newExpenses
        notifyDataSetChanged()
    }
}
