package com.example

import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.Table // <--- ¡ESTA FALTA!
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val driverClassName = "com.mysql.cj.jdbc.Driver"
        val jdbcUrl = "jdbc:mysql://localhost:3306/uniruta_db"
        val user = "root"
        val password = ""

        Database.connect(jdbcUrl, driverClassName, user, password)
        println("¡Conectado exitosamente a la base de datos!")

        // Esto crea la tabla automáticamente si no existe en XAMPP
        transaction {
            SchemaUtils.create(UbicacionesTable)
        }
    }
}

// LA TABLA VA AFUERA DEL OTRO OBJECT (O debajo, pero con sus propias llaves)
object UbicacionesTable : Table("ubicaciones_unidades") {
    val idChofer = integer("id_chofer")
    val latitud = double("latitud")
    val longitud = double("longitud")
    override val primaryKey = PrimaryKey(idChofer)
}