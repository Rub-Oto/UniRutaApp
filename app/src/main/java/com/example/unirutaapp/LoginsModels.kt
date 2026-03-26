package com.example.unirutaapp

data class LoginRequest(
    val correo: String,
    val contrasena: String
)

data class LoginResponse(
    val exitoso: Boolean,
    val mensaje: String,
    val rol: String? = null,
    val nombre: String? = null
)