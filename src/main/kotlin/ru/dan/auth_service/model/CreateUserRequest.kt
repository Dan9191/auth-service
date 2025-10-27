package ru.dan.auth_service.model

data class CreateUserRequest(
    val username: String,
    val password: String,
    val email: String,
    val role: String = "USER"
)