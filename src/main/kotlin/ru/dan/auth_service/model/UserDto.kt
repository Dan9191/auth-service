package ru.dan.auth_service.model

data class UserDto(
    val id: Long? = null,
    val username: String,
    val email: String,
    val role: String
)