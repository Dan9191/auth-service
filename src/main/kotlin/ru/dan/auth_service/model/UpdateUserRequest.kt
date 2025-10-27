package ru.dan.auth_service.model

data class UpdateUserRequest(
    val password: String? = null,
    val email: String? = null,
    val role: String? = null
)