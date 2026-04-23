package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*
// ESTA ES LA LÍNEA CLAVE:
import com.example.dao.DatabaseFactory

fun main() {
    // Esto permite que en Render use el puerto de la nube y en tu casa el 8080
    val port = System.getenv("PORT")?.toInt() ?: 8080
    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // 1. Inicializamos la conexión a RAILWAY (Ya no necesitas XAMPP prendido)
    DatabaseFactory.init()

    // 2. Activamos el soporte para JSON
    install(ContentNegotiation) {
        json()
    }

    // 3. Configuramos las rutas
    configureRouting()
}