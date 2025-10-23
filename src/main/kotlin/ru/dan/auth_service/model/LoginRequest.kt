package ru.dan.auth_service.model

data class LoginRequest(
    val username: String,
    val password: String
)