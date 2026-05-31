package com.pab.deucepadelapp.model

// Penampung untuk updateName di Android
data class UpdateNameRequest(
    val name: String
)

// Penampung untuk updateAccount di Android
data class UpdateAccountRequest(
    val email: String,
    val newPassword: String
)