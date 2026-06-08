package com.example.gastospersonales

import android.os.Bundle
import android.util.Patterns
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.gastospersonales.data.AppDatabase
import com.example.gastospersonales.data.entities.User
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.launch

class RegisterActivity : AppCompatActivity() {

    private var selectedAvatarId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val tilEmail = findViewById<TextInputLayout>(R.id.tilEmail)
        val etEmail = findViewById<TextInputEditText>(R.id.etEmail)
        val tilUsername = findViewById<TextInputLayout>(R.id.tilUsername)
        val etUsername = findViewById<TextInputEditText>(R.id.etUsername)
        val tilPassword = findViewById<TextInputLayout>(R.id.tilPassword)
        val etPassword = findViewById<TextInputEditText>(R.id.etPassword)
        val tilConfirmPassword = findViewById<TextInputLayout>(R.id.tilConfirmPassword)
        val etConfirmPassword = findViewById<TextInputEditText>(R.id.etConfirmPassword)
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLogin = findViewById<TextView>(R.id.tvLogin)

        val avatars = listOf(
            findViewById<ImageView>(R.id.ivAvatar1),
            findViewById<ImageView>(R.id.ivAvatar2),
            findViewById<ImageView>(R.id.ivAvatar3),
            findViewById<ImageView>(R.id.ivAvatar4)
        )

        avatars.forEachIndexed { index, imageView ->
            imageView.setOnClickListener {
                // Deseleccionar todos
                avatars.forEach { it.isSelected = false }
                // Seleccionar el actual
                imageView.isSelected = true
                selectedAvatarId = index
            }
        }

        btnBack.setOnClickListener {
            finish()
        }

        tvLogin.setOnClickListener {
            finish()
        }

        btnRegister.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val username = etUsername.text.toString().trim()
            val password = etPassword.text.toString().trim()
            val confirmPassword = etConfirmPassword.text.toString().trim()

            if (validarRegistro(email, username, password, confirmPassword, tilEmail, tilUsername, tilPassword, tilConfirmPassword)) {
                if (selectedAvatarId == -1) {
                    Toast.makeText(this, "Por favor selecciona un avatar", Toast.LENGTH_SHORT).show()
                } else {
                    registrarUsuario(email, username, password)
                }
            }
        }
    }

    private fun registrarUsuario(email: String, nombre: String, pass: String) {
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(this@RegisterActivity)
            
            // Verificar si el correo ya existe
            val existingUser = db.userDao().getUserByEmail(email)
            if (existingUser != null) {
                findViewById<TextInputLayout>(R.id.tilEmail).error = "Este correo ya está registrado"
                return@launch
            }

            // Crear el nuevo usuario
            val newUser = User(
                nombre = nombre,
                email = email,
                password = pass,
                avatarId = selectedAvatarId
            )

            try {
                db.userDao().insert(newUser)
                Toast.makeText(this@RegisterActivity, "¡Cuenta creada con éxito!", Toast.LENGTH_SHORT).show()
                finish() // Regresar al Login
            } catch (e: Exception) {
                Toast.makeText(this@RegisterActivity, "Error al crear la cuenta", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun validarRegistro(
        email: String,
        username: String,
        pass: String,
        confirmPass: String,
        tilEmail: TextInputLayout,
        tilUsername: TextInputLayout,
        tilPass: TextInputLayout,
        tilConfirmPass: TextInputLayout
    ): Boolean {
        var esValido = true

        if (email.isEmpty()) {
            tilEmail.error = "El correo es obligatorio"
            esValido = false
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.error = "Formato de correo no válido"
            esValido = false
        } else {
            tilEmail.error = null
        }

        if (username.isEmpty()) {
            tilUsername.error = "El nombre de usuario es obligatorio"
            esValido = false
        } else {
            tilUsername.error = null
        }

        if (pass.isEmpty()) {
            tilPass.error = "La contraseña es obligatoria"
            esValido = false
        } else if (pass.length < 6) {
            tilPass.error = "Mínimo 6 caracteres"
            esValido = false
        } else {
            tilPass.error = null
        }

        if (confirmPass != pass) {
            tilConfirmPass.error = "Las contraseñas no coinciden"
            esValido = false
        } else {
            tilConfirmPass.error = null
        }

        return esValido
    }
}