package com.example

import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureRouting() {
    routing {
        // Ruta de prueba para ver si el servidor responde
        get("/") {
            call.respondText("¡Servidor UniRuta encendido y conectado!")
        }

        // Ruta de Login
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()

                // Buscamos en la base de datos de XAMPP
                val userFound = transaction {
                    UsuariosTestTable.selectAll().where {
                        (UsuariosTestTable.correo eq request.correo) and
                                (UsuariosTestTable.contrasena eq request.contrasena)
                    }.map {
                        LoginResponse(
                            exitoso = true,
                            mensaje = "Acceso correcto",
                            rol = it[UsuariosTestTable.rol],
                            nombre = it[UsuariosTestTable.nombre]
                        )
                    }.singleOrNull()
                }

                if (userFound != null) {
                    call.respond(userFound)
                } else {
                    call.respond(LoginResponse(false, "Correo o contraseña incorrectos"))
                }

            } catch (e: Exception) {
                // Si algo falla (como que el JSON venga mal), respondemos el error
                call.respond(LoginResponse(false, "Error en el servidor: ${e.localizedMessage}"))
            }
        }
    }
}