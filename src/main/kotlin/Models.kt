package com.example
import org.jetbrains.exposed.sql.Table
import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val correo: String,
    val contrasena: String
)

@Serializable
data class LoginResponse(
    val exitoso: Boolean,
    val mensaje: String,
    val rol: String? = null,
    val nombre: String? = null
)
object UsuariosTestTable : Table("usuarios_test") {
    val id = integer("id").autoIncrement()
    val nombre = varchar("nombre", 100)
    val correo = varchar("correo", 100)
    val contrasena = varchar("contrasena", 100)
    val rol = varchar("rol", 50)

    override val primaryKey = PrimaryKey(id)
}