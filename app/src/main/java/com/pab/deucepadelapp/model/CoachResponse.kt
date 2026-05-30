package com.pab.deucepadelapp.model

data class CoachResponse(
    val id: Long,
    val name: String,
    val profileImageUrl: String?,
    val availableHours: List<String> // Menampung array jam dinamis dari Spring Boot
)