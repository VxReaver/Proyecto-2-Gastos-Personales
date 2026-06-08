package com.example.gastospersonales

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.gastospersonales.databinding.ActivityAboutBinding

class AboutActivity : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Acerca de"
        
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        
        // Aquí puedes configurar la versión de la app u otra información
        binding.tvVersion.text = "Versión 1.0.0"
    }
}
