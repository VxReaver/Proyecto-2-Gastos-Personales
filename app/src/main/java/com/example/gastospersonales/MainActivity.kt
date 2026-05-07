package com.example.gastospersonales

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Persistencia de sesión: Verificar si el usuario ya inició sesión
        val sharedPref = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        val estaLogueado = sharedPref.getBoolean("isLoggedIn", false)

        if (estaLogueado) {
            // Si ya inició sesión, se saltaría a la pantalla principal
            // Por ahora solo mostramos un mensaje, ya que no existe la siguiente pantalla.
            Toast.makeText(this, "Bienvenido de nuevo", Toast.LENGTH_SHORT).show()
            // irAPantallaPrincipal()
            // finish()
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Referencias a los componentes de la interfaz
        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val btnCreateAccount = findViewById<Button>(R.id.btnCreateAccount)

        // Botón para iniciar sesión
        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (validarDatos(email, password, tilEmail, tilPassword)) {
                // Aquí se validaría contra la base de datos local en el futuro
                guardarSesion()
                Toast.makeText(this, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                // Navegar a la pantalla principal (cuando esté creada)
            }
        }

        // Botón para crear cuenta (Pantalla #2)
        btnCreateAccount.setOnClickListener {
            Toast.makeText(this, "Redirigiendo a registro...", Toast.LENGTH_SHORT).show()
            // val intent = Intent(this, RegistroActivity::class.java)
            // startActivity(intent)
        }
    }

    private fun validarDatos(email: String, pass: String, tilEmail: TextInputLayout, tilPass: TextInputLayout): Boolean {
        var esValido = true

        // Validación de correo electrónico
        if (email.isEmpty()) {
            tilEmail.error = "El correo es obligatorio"
            esValido = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Formato de correo no válido"
            esValido = false
        } else {
            tilEmail.error = null
        }

        // Validación de contraseña
        if (pass.isEmpty()) {
            tilPass.error = "La contraseña es obligatoria"
            esValido = false
        } else {
            tilPass.error = null
        }

        return esValido
    }

    private fun guardarSesion() {
        val sharedPref = getSharedPreferences("sesion_usuario", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            putBoolean("isLoggedIn", true)
            apply()
        }
    }
}