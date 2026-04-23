package com.example.dao

import com.example.* // Esto importa UsuariosTestTable, ReportesRutasTable, etc.
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

object DatabaseFactory {
    fun init() {
        val driverClassName = "com.mysql.cj.jdbc.Driver"

        // Datos de tu base en Railway
        val jdbcURL = "jdbc:mysql://mainline.proxy.rlwy.net:37562/railway?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC"
        val user = "uniruta_user"
        val password = "UniRuta2026!"

        val database = Database.connect(
            url = jdbcURL,
            driver = driverClassName,
            user = user,
            password = password
        )

        transaction(database) {
            // Esto crea las tablas si no existen
            SchemaUtils.create(UsuariosTestTable, ReportesRutasTable, UbicacionesUnidades)
        }
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) { block() }
}