package com.example

import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.transactions.transaction

fun Application.configureRouting() {
    routing {
        // 1. RUTA DE PRUEBA (Para saber si el servidor vive)
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

                if (userFound != null) {
                    call.respond(userFound)
                } else {
                    call.respond(LoginResponse(false, "Correo o contraseña incorrectos"))
                }
            } catch (e: Exception) {
                call.respond(LoginResponse(false, "Error: ${e.localizedMessage}"))
            }
        }

        // 3. REGISTRO
        post("/registrar") {
            try {
                val request = call.receive<RegistroRequest>()
                val inserted = transaction {
                    val existe = UsuariosTestTable.selectAll().where {
                        UsuariosTestTable.correo eq request.correo
                    }.count() > 0

                    if (!existe) {
                        UsuariosTestTable.insert {
                            it[nombre] = request.nombre
                            it[correo] = request.correo
                            it[contrasena] = request.password
                            it[rol] = "Chofer"
                        }
                        true
                    } else {
                        false
                    }
                }

                if (inserted) {
                    call.respond(HttpStatusCode.Created, RegistroResponse("success", "Registrado con éxito"))
                } else {
                    call.respond(HttpStatusCode.Conflict, RegistroResponse("error", "El correo ya existe"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, RegistroResponse("error", e.localizedMessage))
            }
        }

        // 4. ACTUALIZAR UBICACIÓN (Chofer envía coordenadas)
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

        // 5. OBTENER UBICACIÓN (Pasajero consulta dónde está el bus)
        get("/obtener-ubicacion/{idChofer}") {
            try {
                val id = call.parameters["idChofer"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "ID inválido")
                    return@get
                }

                val ubicacion = transaction {
                    UbicacionesUnidades.selectAll().where { UbicacionesUnidades.idChofer eq id }
                        .map {
                            UbicacionResponse(
                                latitud = it[UbicacionesUnidades.latitud],
                                longitud = it[UbicacionesUnidades.longitud]
                            )
                        }.singleOrNull()
                }

                if (ubicacion != null) {
                    call.respond(ubicacion)
                } else {
                    call.respond(HttpStatusCode.NotFound, "Sin ubicación")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }

        // 6. FINALIZAR RUTA (Reportes)
        post("/rutas/finalizar") {
            try {
                val request = call.receive<ReporteRequest>()
                transaction {
                    ReportesRutasTable.insert {
                        it[idChofer] = request.idChofer
                        it[tiempoTotal] = request.tiempoTotal
                    }
                }
                call.respond(HttpStatusCode.Created, mapOf("status" to "success"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }
    }
}