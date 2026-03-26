package com.example.unirutaapp

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.unirutaapp.databinding.ActivityMainBinding
import kotlinx.coroutines.launch
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val pass = binding.etPassword.text.toString()

            if (email.isNotEmpty() && pass.isNotEmpty()) {
                iniciarSesion(email, pass)
            } else {
                Toast.makeText(this, "Por favor llena los campos", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun iniciarSesion(correo: String, pass: String) {
        // IMPORTANTE: Cambia 'TU_IP_AQUI' por la IPv4 que te salga en el CMD (ipconfig)
        val retrofit = Retrofit.Builder()
            .baseUrl("http://10.184.241.113:8080/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val service = retrofit.create(ApiService::class.java)

        lifecycleScope.launch {
            try {
                // Aquí usamos la clase LoginRequest que acabas de crear
                val response = service.loginUser(LoginRequest(correo, pass))

                if (response.isSuccessful && response.body()?.exitoso == true) {
                    val user = response.body()
                    val userRol = user?.rol // Obtenemos el rol: Chofer, Pasajero o Admin

                    Toast.makeText(this@MainActivity, "¡Bienvenido ${user?.nombre}!", Toast.LENGTH_SHORT).show()

                    // --- LÓGICA DE SALTO POR ROL ---
                    val intent = when (userRol) {
                        "Chofer" -> Intent(this@MainActivity, ChoferActivity::class.java)
                        "Pasajero" -> Intent(this@MainActivity, PasajeroActivity::class.java)
                        "Admin" -> Intent(this@MainActivity, AdminActivity::class.java)
                        else -> Intent(this@MainActivity, HomeActivity::class.java)
                    }
                    // --- AQUÍ PASAMOS EL NOMBRE ---
                    intent.putExtra("USER_NAME", user?.nombre)

                    startActivity(intent)
                    finish()

                } else {
                    Toast.makeText(this@MainActivity, "Credenciales incorrectas", Toast.LENGTH_SHORT).show()
                }
            } catch (e: Exception) {
                // Si sale este error, es porque el server Ktor está apagado o la IP está mal
                Toast.makeText(this@MainActivity, "Error de conexión: ${e.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}