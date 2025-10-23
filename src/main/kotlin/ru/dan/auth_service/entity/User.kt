package ru.dan.auth_service.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val username: String,
    val password: String,
    val email: String,
    @Enumerated(EnumType.STRING)
    val role: Role = Role.USER // По умолчанию USER
)