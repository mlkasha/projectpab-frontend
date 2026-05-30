package com.pab.deucepadelapp.api

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    // Pastikan pakai link online Ngrok kamu yang tadi ya!
    private const val BASE_URL = "https://paralegal-silicon-stoplight.ngrok-free.dev/"

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // 🔴 KUNCI NYA DI SINI: Pastikan namanya "instance" bukan yang lain
    val instance: ApiService by lazy {
        retrofit.create(ApiService::class.java)
    }
}