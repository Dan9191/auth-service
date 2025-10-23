package ru.dan.auth_service.service

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import ru.dan.auth_service.entity.Role
import ru.dan.auth_service.entity.User
import ru.dan.auth_service.model.LoginRequest
import ru.dan.auth_service.model.RegisterRequest
import ru.dan.auth_service.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder
) {
    fun register(request: RegisterRequest): User {
        if (userRepository.findByUsername(request.username) != null) {
            throw IllegalArgumentException("Username already exists")
        }
        val user = User(
            username = request.username,
            password = passwordEncoder.encode(request.password),
            email = request.email,
            role = Role.USER
        )
        return userRepository.save(user)
    }

    fun authenticate(request: LoginRequest): User {
        val user = userRepository.findByUsername(request.username)
            ?: throw IllegalArgumentException("Invalid username or password")
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("Invalid username or password")
        }
        return user
    }
}