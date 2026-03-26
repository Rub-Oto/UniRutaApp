package com.example.unirutaapp // Revisa que sea tu paquete real

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("login")
    suspend fun loginUser(@Body request: LoginRequest): Response<LoginResponse>
}