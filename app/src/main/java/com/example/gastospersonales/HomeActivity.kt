package com.example.gastospersonales

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.gastospersonales.ui.accounts.AccountsFragment
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.example.gastospersonales.ui.GroupViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var viewModel: GroupViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        val toolbar: androidx.appcompat.widget.Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Gastos Personales"

        drawerLayout = findViewById(R.id.drawer_layout)
        val navView: NavigationView = findViewById(R.id.nav_view)
        navView.setNavigationItemSelectedListener(this)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        // Configurar la información del usuario en la cabecera
        setupNavigationHeader(navView)

        // ViewModel y carga de grupos
        viewModel = ViewModelProvider(this)[GroupViewModel::class.java]
        val sharedPref = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        val userIdInt = sharedPref.getInt("user_id", -1)
        val userId = if (userIdInt != -1) userIdInt.toString() else "guest"
        
        viewModel.loadUserGroups(userId)
        observeGroups(navView)

        // Cargar DashboardFragment por defecto
        if (savedInstanceState == null) {
            replaceFragment(DashboardFragment())
            navView.setCheckedItem(R.id.nav_home)
        }

        // Configurar el botón de cerrar sesión en la cabecera
        val headerView = navView.getHeaderView(0)
        val llLogout = headerView.findViewById<View>(R.id.llLogout)
        llLogout.setOnClickListener {
            cerrarSesion()
        }

        // El FAB abre la pantalla de Agregar (Pantalla 4)
        val fabAddExpense: FloatingActionButton = findViewById(R.id.fabAddExpense)
        fabAddExpense.setOnClickListener {
            val intent = Intent(this, AddMovementActivity::class.java)
            startActivity(intent)
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.drawer_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupNavigationHeader(navView: NavigationView) {
        val headerView = navView.getHeaderView(0)
        val tvName = headerView.findViewById<TextView>(R.id.tvHeaderName)
        val tvEmail = headerView.findViewById<TextView>(R.id.tvHeaderEmail)
        val ivAvatar = headerView.findViewById<ImageView>(R.id.ivUserAvatar)

        val sharedPref = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        val name = sharedPref.getString("user_name", "Usuario")
        val email = sharedPref.getString("user_email", "correo@ejemplo.com")

        tvName.text = name
        tvEmail.text = email
        ivAvatar.setImageResource(R.drawable.ic_person)
    }

    private fun observeGroups(navView: NavigationView) {
        lifecycleScope.launch {
            viewModel.userGroups.collectLatest { groups ->
                val menu = navView.menu
                // Limpiar items dinámicos previos si los hay (podrías usar un ID de grupo de menú)
                menu.removeGroup(R.id.nav_group_shared_section)
                
                if (groups.isNotEmpty()) {
                    val submenu = menu.addSubMenu(R.id.nav_group_shared_section, View.NO_ID, 100, "GRUPOS COMPARTIDOS")
                    groups.forEachIndexed { index, group ->
                        val item = submenu.add(R.id.nav_group_shared_section, View.NO_ID, index, group.name)
                        item.setIcon(R.drawable.ic_person)
                        item.setOnMenuItemClickListener {
                            // Abrir Panel de Grupo
                            val intent = Intent(this@HomeActivity, GroupPanelActivity::class.java).apply {
                                // Nota: GroupPanelActivity actualmente usa Int para GROUP_ID, pero Firestore usa String.
                                // Habría que adaptar GroupPanelActivity para recibir el String de Firestore.
                                putExtra("GROUP_ID_STR", group.id) 
                                putExtra("GROUP_NAME", group.name)
                            }
                            startActivity(intent)
                            drawerLayout.closeDrawer(GravityCompat.START)
                            true
                        }
                    }
                }
            }
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_content_home, fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> replaceFragment(DashboardFragment())
            R.id.nav_movements -> startActivity(Intent(this, MovementsActivity::class.java))
            R.id.nav_accounts -> replaceFragment(AccountsFragment())
            R.id.nav_categories -> startActivity(Intent(this, CategoriesActivity::class.java))
            R.id.nav_shared_finances -> startActivity(Intent(this, GroupManagementActivity::class.java))
            R.id.nav_about -> startActivity(Intent(this, AboutActivity::class.java))
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun cerrarSesion() {
        val sharedPref = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear()
            apply()
        }
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
