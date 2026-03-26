package com.example.unirutaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class HomeActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home)

        // Recibimos el nombre que viene desde el MainActivity (Login)
        val nombreUsuario = intent.getStringExtra("USER_NAME")

        // Botón VER RUTA
        val btnRuta = findViewById<Button>(R.id.btnVerRuta)
        btnRuta.setOnClickListener {
            val intent = Intent(this, PasajeroActivity::class.java)
            startActivity(intent)
        }

        // Botón MI PERFIL
        val btnPerfil = findViewById<Button>(R.id.btnPerfil)
        btnPerfil.setOnClickListener {
            val intent = Intent(this, PerfilActivity::class.java)
            // IMPORTANTE: Volvemos a pasar el nombre hacia el Perfil
            intent.putExtra("USER_NAME", nombreUsuario)
            startActivity(intent)
        }
    }
}