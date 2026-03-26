package com.example.unirutaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class PasajeroActivity: AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pasajero) // Esto vincula tu XML

        // Buscamos el botón por su ID
        val btnCerrar = findViewById<Button>(R.id.btnCerrarSesion)

        btnCerrar.setOnClickListener {
            // Creamos el "salto" de regreso al MainActivity (Login)
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            // Cerramos esta pantalla para que no quede abierta en segundo plano
            finish()
        }
    }
}