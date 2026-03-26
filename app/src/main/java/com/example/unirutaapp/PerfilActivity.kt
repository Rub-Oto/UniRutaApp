package com.example.unirutaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView // Importante para el nombre
import androidx.appcompat.app.AppCompatActivity

class PerfilActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_profile)

        // 1. RECIBIR EL NOMBRE: Lo atrapamos del Intent que mandó el Home
        val nombreRecibido = intent.getStringExtra("USER_NAME")
        val tvNombre = findViewById<TextView>(R.id.tvNombrePerfil) // Revisa este ID en tu XML

        if (nombreRecibido != null) {
            tvNombre.text = nombreRecibido
        }

        // 2. BOTÓN CERRAR SESIÓN
        val btnCerrar = findViewById<Button>(R.id.btnCerrarSesionPerfil)
        btnCerrar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            // Limpia el historial para que no puedan regresar al perfil con el botón de "atrás"
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }
    }
}