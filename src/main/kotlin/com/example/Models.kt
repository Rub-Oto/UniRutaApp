package com.example.com.example

import org.jetbrains.exposed.sql.Table
import kotlinx.serialization.Serializable


@Serializable
data class LoginRequest(val correo: String, val contrasena: String)

@Serializable
data class LoginResponse(
    val exitoso: Boolean,
    val mensaje: String,
    val rol: String? = null,
    val nombre: String? = null,
    val id: Int? = null // Añadimos el ID para que el chofer sepa quién es
)

@Serializable
data class RegistroRequest(val nombre: String, val correo: String, val password: String, val rol: String = "chofer")

@Serializable
data class RegistroResponse(val status: String, val message: String)

@Serializable
data class ChoferResponse(val id: Int, val nombre: String, val correo: String, val rol: String)

@Serializable
data class ReporteRequest(val idChofer: Int, val tiempoTotal: String)

object UsuariosTestTable : Table("usuarios_test") {
    val id = integer("id").autoIncrement()
    val nombre = varchar("nombre", 100)
    val correo = varchar("correo", 100)
    val contrasena = varchar("contrasena", 100)
    val rol = varchar("rol", 50)
    override val primaryKey = PrimaryKey(id)
}

object ReportesRutasTable : Table("reportes_rutas") {
    val id = integer("id").autoIncrement()
    val idChofer = integer("id_chofer")
    val tiempoTotal = varchar("tiempo_total", 20)
    val fecha = varchar("fecha", 50).nullable()
    override val primaryKey = PrimaryKey(id)
}

@Serializable
data class ReporteDetalle(
    val id: Int,
    val nombreChofer: String,
    val tiempoTotal: String,
    val fecha: String
)

@Serializable
data class UbicacionRequest(val idChofer: Int, val latitud: Double, val longitud: Double)

@Serializable
data class UbicacionResponse(val latitud: Double, val longitud: Double)
object UbicacionesUnidades : Table("ubicaciones_unidades") {
    val idChofer = integer("id_chofer")
    val latitud = double("latitud")
    val longitud = double("longitud")
    // Lo cambiamos a varchar para evitar errores de librerías
    val ultimaActualizacion = varchar("ultima_actualizacion", 50).default("2026-04-22 00:00:00")
    override val primaryKey = PrimaryKey(idChofer)
}