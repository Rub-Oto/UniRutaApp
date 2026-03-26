package com.example

import org.jetbrains.exposed.sql.Database

object DatabaseFactory {
    fun init() {
        val driverClassName = "com.mysql.cj.jdbc.Driver"
        // Asegúrate de que el nombre 'uniruta_db' sea el mismo que pusiste en phpMyAdmin
        val jdbcUrl = "jdbc:mysql://localhost:3306/uniruta_db"
        val user = "root"
        val password = ""

        Database.connect(jdbcUrl, driverClassName, user, password)
        println("¡Conectado exitosamente a la base de datos de XAMPP!")
    }
}