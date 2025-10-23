package ru.dan.auth_service.model

data class RegisterRequest(
    val username: String,
    val password: String,
    val email: String
)