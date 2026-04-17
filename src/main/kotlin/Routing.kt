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
        // 1. Ruta de prueba
        get("/") {
            call.respondText("¡Servidor UniRuta encendido y conectado!")
        }

        // 2. Login
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
                            id = it[UsuariosTestTable.id] // <--- ¡ESTA ES LA LÍNEA QUE FALTABA!
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

        // 3. Registro
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

        // 4. Lista de Choferes
        get("/choferes") {
            try {
                val lista = transaction {
                    UsuariosTestTable.selectAll().where {
                        (UsuariosTestTable.rol eq "Chofer") or (UsuariosTestTable.rol eq "chofer")
                    }.map {
                        ChoferResponse(
                            id = it[UsuariosTestTable.id],
                            nombre = it[UsuariosTestTable.nombre],
                            correo = it[UsuariosTestTable.correo],
                            rol = it[UsuariosTestTable.rol]
                        )
                    }
                }
                call.respond(lista)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.localizedMessage))
            }
        }

        // 5. NUEVA RUTA: Finalizar Ruta (Guardar tiempo)
        post("/rutas/finalizar") {
            try {
                val request = call.receive<ReporteRequest>()
                transaction {
                    ReportesRutasTable.insert {
                        it[idChofer] = request.idChofer
                        it[tiempoTotal] = request.tiempoTotal
                    }
                }
                call.respond(HttpStatusCode.Created, mapOf("status" to "success", "message" to "Reporte guardado"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, "Error: ${e.localizedMessage}")
            }
        }

        // 6. ELIMINAR CHOFER
        delete("/usuarios/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, mapOf("error" to "ID no válido"))
                    return@delete
                }

                val filasBorradas = transaction {
                    UsuariosTestTable.deleteWhere { UsuariosTestTable.id eq id }
                }

                if (filasBorradas > 0) {
                    call.respond(HttpStatusCode.OK, mapOf("status" to "success", "message" to "Eliminado"))
                } else {
                    call.respond(HttpStatusCode.NotFound, mapOf("status" to "error", "message" to "No encontrado"))
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.localizedMessage))
            }
        }

        // 7. ACTUALIZAR (Editar)
        put("/usuarios/{id}") {
            try {
                val id = call.parameters["id"]?.toIntOrNull()
                val request = call.receive<ChoferResponse>()

                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "ID inválido")
                    return@put
                }

                val actualizado = transaction {
                    UsuariosTestTable.update({ UsuariosTestTable.id eq id }) {
                        it[nombre] = request.nombre
                        it[correo] = request.correo
                    } > 0
                }

                if (actualizado) {
                    call.respond(mapOf("status" to "success"))
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage ?: "Error")
            }
        }
        // 8. Obtener todos los reportes (con nombre de chofer)
        get("/reportes") {
            try {
                val reportes = transaction {
                    Join(
                        ReportesRutasTable,
                        UsuariosTestTable,
                        onColumn = ReportesRutasTable.idChofer,
                        otherColumn = UsuariosTestTable.id,
                        joinType = JoinType.INNER
                    ).selectAll()
                        .orderBy(ReportesRutasTable.id to SortOrder.DESC)
                        .map {
                            // En lugar de mapOf, creamos el objeto ReporteDetalle
                            ReporteDetalle(
                                id = it[ReportesRutasTable.id],
                                nombreChofer = it[UsuariosTestTable.nombre],
                                tiempoTotal = it[ReportesRutasTable.tiempoTotal],
                                fecha = it[ReportesRutasTable.fecha] ?: "Sin fecha"
                            )
                        }
                }
                call.respond(reportes) // Ahora Ktor sabe exactamente qué está mandando
            } catch (e: Exception) {
                println("ERROR EN REPORTES: ${e.localizedMessage}")
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.localizedMessage))
            }
        }

        // 9. ACTUALIZAR UBICACIÓN (Usada por el Chofer)
        post("/actualizar-ubicacion") {
            try {
                val request = call.receive<UbicacionRequest>()

                transaction {
                    // Intenta actualizar si ya existe el chofer en la tabla de ubicaciones
                    val actualizado = UbicacionesTable.update({ UbicacionesTable.idChofer eq request.idChofer }) {
                        it[latitud] = request.latitud
                        it[longitud] = request.longitud
                    } > 0

                    // Si no se actualizó nada (es la primera vez), lo insertamos
                    if (!actualizado) {
                        UbicacionesTable.insert {
                            it[idChofer] = request.idChofer
                            it[latitud] = request.latitud
                            it[longitud] = request.longitud
                        }
                    }
                }
                call.respond(HttpStatusCode.OK, mapOf("status" to "success", "message" to "Coordenadas guardadas"))
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, mapOf("error" to e.localizedMessage))
            }
        }

        // 10. OBTENER UBICACIÓN (Usada por el Pasajero/Admin)
        get("/obtener-ubicacion/{idChofer}") {
            try {
                val id = call.parameters["idChofer"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, "ID de chofer inválido")
                    return@get
                }

                val ubicacion = transaction {
                    UbicacionesTable.selectAll().where { UbicacionesTable.idChofer eq id }
                        .map {
                            UbicacionResponse(
                                latitud = it[UbicacionesTable.latitud],
                                longitud = it[UbicacionesTable.longitud]
                            )
                        }.singleOrNull()
                }

                if (ubicacion != null) {
                    call.respond(ubicacion)
                } else {
                    call.respond(HttpStatusCode.NotFound, "No hay ubicación para este chofer")
                }
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }

        // 11. Obtener estadísticas para el Dashboard del Admin
        get("/admin/stats") {
            try {
                val stats = transaction {
                    val totalChoferes = UsuariosTestTable.selectAll()
                        .where { (UsuariosTestTable.rol eq "Chofer") or (UsuariosTestTable.rol eq "chofer") }
                        .count()

                    val totalReportes = ReportesRutasTable.selectAll().count()

                    mapOf(
                        "totalChoferes" to totalChoferes,
                        "totalReportes" to totalReportes
                    )
                }
                call.respond(stats)
            } catch (e: Exception) {
                call.respond(HttpStatusCode.InternalServerError, e.localizedMessage)
            }
        }
    }
}