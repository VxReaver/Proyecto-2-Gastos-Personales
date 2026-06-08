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
import com.example.gastospersonales.data.entities.Account
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView

class HomeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout

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
        val avatarId = sharedPref.getInt("user_avatar", 0)

        tvName.text = name
        tvEmail.text = email
        
        // Mapear el ID del avatar al recurso correspondiente
        ivAvatar.setImageResource(R.drawable.ic_person)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.nav_host_fragment_content_home, fragment)
            .commit()
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                replaceFragment(DashboardFragment())
            }
            R.id.nav_movements -> {
                val intent = Intent(this, MovementsActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_accounts -> {
              val intent = Intent(this, Account::class.java)
                startActivity(intent)
            }
            R.id.nav_categories -> {
                val intent = Intent(this, CategoryReportActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_shared_finances -> {
                val intent = Intent(this, GroupManagementActivity::class.java)
                startActivity(intent)
            }
            R.id.nav_help -> {
                // Implementar Ayuda
            }
            R.id.nav_about -> {
                val intent = Intent(this, AboutActivity::class.java)
                startActivity(intent)
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }

    private fun cerrarSesion() {
        val sharedPref = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear() // Limpia toda la sesión
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