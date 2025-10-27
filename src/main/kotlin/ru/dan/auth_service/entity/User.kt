package ru.dan.auth_service.entity

import jakarta.persistence.*

@Entity
@Table(name = "users")
data class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val username: String,
    var password: String,
    var email: String,
    @Enumerated(EnumType.STRING)
    var role: Role = Role.ROLE_USER // По умолчанию USER
)