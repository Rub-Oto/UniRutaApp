package com.example

import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.plugins.contentnegotiation.*

fun main() {
    // CAMBIO CRÍTICO: Lee el puerto de Render o usa 8080 por defecto
    val port = System.getenv("PORT")?.toInt() ?: 8080

    embeddedServer(Netty, port = port, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    // Inicializamos la base de datos
    DatabaseFactory.init()

    install(ContentNegotiation) {
        json()
    }

    configureRouting()
}