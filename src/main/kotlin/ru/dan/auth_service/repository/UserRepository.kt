package ru.dan.auth_service.repository

import org.springframework.data.jpa.repository.JpaRepository
import ru.dan.auth_service.entity.User

interface UserRepository : JpaRepository<User, Long> {
    fun findByUsername(username: String): User?
    fun findUserById(id: Long): User?
}