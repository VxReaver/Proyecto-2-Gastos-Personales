package com.example.gastospersonales

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.gastospersonales.data.AppDatabase
import com.example.gastospersonales.data.entities.Category
import com.example.gastospersonales.databinding.DialogCategoryBinding
import com.example.gastospersonales.databinding.ItemIconSelectorBinding
import kotlinx.coroutines.launch

class CategoryDialogFragment : DialogFragment() {

    private var _binding: DialogCategoryBinding? = null
    private val binding get() = _binding!!

    private var categoryToEdit: Category? = null
    private var selectedIconResId: Int = R.drawable.ic_categories

    private val icons = listOf(
        R.drawable.ic_wallet,
        R.drawable.ic_accounts,
        R.drawable.ic_calendar,
        R.drawable.ic_movements,
        R.drawable.ic_categories,
        R.drawable.ic_home,
        R.drawable.ic_person,
        R.drawable.ic_info,
        R.drawable.ic_help,
        R.drawable.ic_lock,
        R.drawable.ic_email,
        R.drawable.ic_logout
    )

    companion object {
        private const val ARG_CATEGORY = "arg_category"

        fun newInstance(category: Category? = null): CategoryDialogFragment {
            val fragment = CategoryDialogFragment()
            category?.let {
                val args = Bundle()
                args.putParcelable(ARG_CATEGORY, it)
                fragment.arguments = args
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, android.R.style.Theme_DeviceDefault_Light_Dialog_NoActionBar_MinWidth)
        categoryToEdit = arguments?.getParcelable(ARG_CATEGORY)
        categoryToEdit?.let {
            selectedIconResId = it.iconResource
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = DialogCategoryBinding.inflate(inflater, container, false)
        dialog?.window?.setBackgroundDrawableResource(android.R.color.transparent)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupIconSelector()

        binding.btnCancel.setOnClickListener { dismiss() }
        binding.btnSaveCategory.setOnClickListener { saveCategory() }
    }

    private fun setupUI() {
        categoryToEdit?.let {
            binding.tvDialogTitle.text = "Modificar Categoría"
            binding.etCategoryDescription.setText(it.description)
        } ?: run {
            binding.tvDialogTitle.text = "Nueva Categoría"
        }
    }

    private fun setupIconSelector() {
        val adapter = IconAdapter(icons, selectedIconResId) { iconRes ->
            selectedIconResId = iconRes
        }
        binding.rvIconSelector.layoutManager = GridLayoutManager(requireContext(), 4)
        binding.rvIconSelector.adapter = adapter
    }

    private fun saveCategory() {
        val description = binding.etCategoryDescription.text.toString().trim()

        if (description.isEmpty()) {
            binding.tilCategoryDescription.error = "La descripción es obligatoria"
            return
        }

        val sharedPref = requireActivity().getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        val userId = sharedPref.getInt("user_id", -1)

        if (userId == -1) {
            Toast.makeText(requireContext(), "Error: Sesión no válida", Toast.LENGTH_SHORT).show()
            return
        }

        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            val categoryDao = db.categoryDao()

            if (categoryToEdit == null) {
                val newCategory = Category(
                    userId = userId,
                    description = description,
                    iconResource = selectedIconResId
                )
                categoryDao.insert(newCategory)
                Toast.makeText(requireContext(), "Categoría creada", Toast.LENGTH_SHORT).show()
            } else {
                val updatedCategory = categoryToEdit!!.copy(
                    description = description,
                    iconResource = selectedIconResId
                )
                categoryDao.update(updatedCategory)
                Toast.makeText(requireContext(), "Categoría actualizada", Toast.LENGTH_SHORT).show()
            }
            dismiss()
        }
    }

    override fun onStart() {
        super.onStart()
        val width = (resources.displayMetrics.widthPixels * 0.90).toInt()
        dialog?.window?.setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private inner class IconAdapter(
        private val iconList: List<Int>,
        private var selectedResId: Int,
        private val onIconSelected: (Int) -> Unit
    ) : RecyclerView.Adapter<IconAdapter.ViewHolder>() {

        inner class ViewHolder(val binding: ItemIconSelectorBinding) : RecyclerView.ViewHolder(binding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val binding = ItemIconSelectorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            return ViewHolder(binding)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val iconRes = iconList[position]
            holder.binding.ivIcon.setImageResource(iconRes)

            if (iconRes == selectedResId) {
                holder.binding.ivSelected.visibility = View.VISIBLE
            } else {
                holder.binding.ivSelected.visibility = View.GONE
            }

            holder.itemView.setOnClickListener {
                val previousSelected = selectedResId
                selectedResId = iconRes
                onIconSelected(iconRes)
                
                val prevIndex = iconList.indexOf(previousSelected)
                if (prevIndex != -1) notifyItemChanged(prevIndex)
                notifyItemChanged(position)
            }
        }

        override fun getItemCount() = iconList.size
    }
}
