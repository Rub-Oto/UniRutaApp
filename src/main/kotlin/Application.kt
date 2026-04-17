package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*

fun main() {
    // Volvemos al puerto local 8080 para XAMPP
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // 1. Inicializamos la base de datos local (asegúrate de que XAMPP esté prendido)
    DatabaseFactory.init()

    // 2. Activamos el soporte para JSON
    install(ContentNegotiation) {
        json()
    }

    // 3. Configuramos las rutas
    configureRouting()
}