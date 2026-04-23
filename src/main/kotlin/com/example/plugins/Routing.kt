package com.example.plugins

import com.example.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureRouting() {
    routing {
        // 1. PRUEBA
        get("/") {
            call.respondText("¡Servidor UniRuta encendido y conectado a Railway!")
        }

        // 2. LOGIN
        post("/login") {
            try {
                val request = call.receive<LoginRequest>()
                val userFound = transaction {
                    UsuariosTestTable.selectAll().where {
                        (UsuariosTestTable.correo eq request.correo) and
                                (UsuariosTestTable.contrasena eq request.contrasena)
                    }.map {
                        LoginResponse(
                            exitoso = true,
                            mensaje = "Acceso correcto",
                            rol = it[UsuariosTestTable.rol],
                            nombre = it[UsuariosTestTable.nombre],
                            id = it[UsuariosTestTable.id]
                        )
                    }.singleOrNull()
                }
                if (userFound != null) call.respond(userFound)
                else call.respond(LoginResponse(false, "Correo o contraseña incorrectos"))
            } catch (e: Exception) {
                call.respond(LoginResponse(false, "Error: ${e.localizedMessage}"))
            }
        }

        // 3. REGISTRO
        post("/registrar") {
            try {
                val request = call.receive<RegistroRequest>()
                val inserted = transaction {
                    val existe = UsuariosTestTable.selectAll().where { UsuariosTestTable.correo eq request.correo }.count() > 0
                    if (!existe) {
                        UsuariosTestTable.insert {
                            it[nombre] = request.nombre
                            it[correo] = request.correo
                            it[contrasena] = request.password
                            it[rol] = "Chofer"
                        }
                        true
                    } else false
                }
                if (inserted) call.respond(HttpStatusCode.Created, RegistroResponse("success", "Registrado con éxito"))
                else call.respond(HttpStatusCode.Conflict, RegistroResponse("error", "El correo ya existe"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, RegistroResponse("error", e.localizedMessage))
            }
        }

        // 4. OBTENER CHOFERES (Admin)
        get("/choferes") {
            try {
                val choferes = transaction {
                    UsuariosTestTable.selectAll()
                        .where { UsuariosTestTable.rol eq "Chofer" }
                        .map {
                            ChoferResponse(
                                id = it[UsuariosTestTable.id],
                                nombre = it[UsuariosTestTable.nombre],
                                correo = it[UsuariosTestTable.correo],
                                rol = it[UsuariosTestTable.rol]
                            )
                        }
                }
                call.respond(choferes)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.localizedMessage}")
            }
        }

        // 5. OBTENER REPORTES (Admin)
        get("/reportes") {
            try {
                val reportes = transaction {
                    ReportesRutasTable.selectAll().map {
                        ReporteDetalle( // <--- Asegúrate que se llame igual que en Models.kt
                            id = it[ReportesRutasTable.id],
                            nombreChofer = "ID Chofer: ${it[ReportesRutasTable.idChofer]}",
                            tiempoTotal = it[ReportesRutasTable.tiempoTotal],
                            fecha = it[ReportesRutasTable.fecha] ?: "Sin fecha"
                        )
                    }
                }
                call.respond(reportes)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.localizedMessage}")
            }
        }

        // 6. FINALIZAR RUTA
        post("/rutas/finalizar") {
            try {
                val request = call.receive<ReporteRequest>()
                transaction {
                    ReportesRutasTable.insert {
                        it[idChofer] = request.idChofer
                        it[tiempoTotal] = request.tiempoTotal
                    }
                }
                call.respond(HttpStatusCode.Created, RegistroResponse("success", "Ruta finalizada"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, RegistroResponse("error", e.localizedMessage))
            }
        }

        // 7. ACTUALIZAR UBICACIÓN
        post("/actualizar-ubicacion") {
            try {
                val request = call.receive<UbicacionRequest>()
                transaction {
                    val actualizado = UbicacionesUnidades.update({ UbicacionesUnidades.idChofer eq request.idChofer }) {
                        it[latitud] = request.latitud
                        it[longitud] = request.longitud
                    } > 0
                    if (!actualizado) {
                        UbicacionesUnidades.insert {
                            it[idChofer] = request.idChofer
                            it[latitud] = request.latitud
                            it[longitud] = request.longitud
                        }
                    }
                }
                call.respond(HttpStatusCode.OK, mapOf("status" to "success"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.localizedMessage))
            }
        }
    }
}