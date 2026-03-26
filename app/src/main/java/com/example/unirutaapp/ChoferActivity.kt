package com.example.unirutaapp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class ChoferActivity : AppCompatActivity() {

    // Variables para el cronómetro (Lógica Offline)
    private var isRutaIniciada = false
    private var segundos = 0
    private val handler = Handler(Looper.getMainLooper())
    private lateinit var runnable: Runnable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chofer)

        // 1. Vinculamos los elementos del XML
        val btnAccionRuta = findViewById<Button>(R.id.btnActivarRuta)
        val btnCerrar = findViewById<Button>(R.id.btnCerrarSesion)
        val tvCrono = findViewById<TextView>(R.id.tvCronometro) // Asegúrate de tener este ID en tu XML

        // 2. Lógica del Botón Iniciar/Detener Ruta
        btnAccionRuta.setOnClickListener {
            if (!isRutaIniciada) {
                // INICIAR
                isRutaIniciada = true
                btnAccionRuta.text = "DETENER RECORRIDO"
                btnAccionRuta.setBackgroundColor(Color.parseColor("#E53935")) // Rojo
                iniciarContador(tvCrono)
                Toast.makeText(this, "Ruta iniciada", Toast.LENGTH_SHORT).show()
            } else {
                // DETENER
                isRutaIniciada = false
                btnAccionRuta.text = "INICIAR RECORRIDO"
                btnAccionRuta.setBackgroundColor(Color.parseColor("#43A047")) // Verde
                detenerContador()
                Toast.makeText(this, "Ruta finalizada", Toast.LENGTH_SHORT).show()
            }
        }

        // 3. Lógica del Botón Cerrar Sesión
        btnCerrar.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    // Funciones del Cronómetro
    private fun iniciarContador(tv: TextView) {
        runnable = object : Runnable {
            override fun run() {
                segundos++
                val mins = segundos / 60
                val secs = segundos % 60
                tv.text = String.format("%02d:%02d", mins, secs)
                handler.postDelayed(this, 1000)
            }
        }
        handler.post(runnable)
    }

    private fun detenerContador() {
        if (::runnable.isInitialized) {
            handler.removeCallbacks(runnable)
        }
        segundos = 0 // Reiniciamos para la próxima vez
    }
}