package ru.dan.auth_service.service

import jakarta.transaction.Transactional
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder
import org.springframework.stereotype.Service
import ru.dan.auth_service.entity.Role
import ru.dan.auth_service.entity.User
import ru.dan.auth_service.model.CreateUserRequest
import ru.dan.auth_service.model.LoginRequest
import ru.dan.auth_service.model.RegisterRequest
import ru.dan.auth_service.model.UpdateUserRequest
import ru.dan.auth_service.repository.UserRepository

@Service
class UserService(
    private val userRepository: UserRepository,
    private val passwordEncoder: BCryptPasswordEncoder
) {

    /**
     * Регистрация пользователя.
     */
    @Transactional
    fun register(request: RegisterRequest): User {
        if (userRepository.findByUsername(request.username) != null) {
            throw IllegalArgumentException("Username already exists")
        }
        val user = User(
            username = request.username,
            password = passwordEncoder.encode(request.password),
            email = request.email,
            role = Role.ROLE_USER
        )
        return userRepository.save(user)
    }

    /**
     * Аутентификация пользователя.
     */
    @Transactional
    fun authenticate(request: LoginRequest): User {
        val user = userRepository.findByUsername(request.username)
            ?: throw IllegalArgumentException("Invalid username or password")
        if (!passwordEncoder.matches(request.password, user.password)) {
            throw IllegalArgumentException("Invalid username or password")
        }
        return user
    }

    /**
     * Создание пользователя.
     */
    @Transactional
    fun createUser(request: CreateUserRequest): User {
        if (userRepository.findByUsername(request.username) != null) {
            throw IllegalArgumentException("Username already exists")
        }
        val roleEnum = try {
            Role.valueOf("ROLE_${request.role.uppercase()}")
        } catch (e: IllegalArgumentException) {
            throw IllegalArgumentException("Invalid role")
        }
        val user = User(
            username = request.username,
            password = passwordEncoder.encode(request.password),
            email = request.email,
            role = roleEnum
        )
        return userRepository.save(user)
    }

    /**
     * Обновление пользователя.
     */
    @Transactional
    fun updateUser(username: String, request: UpdateUserRequest): User {
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("User not found")
        request.password?.let { user.password = passwordEncoder.encode(it) }
        request.email?.let { user.email = it }
        request.role?.let {
            user.role = try {
                Role.valueOf("ROLE_${it.uppercase()}")
            } catch (e: IllegalArgumentException) {
                throw IllegalArgumentException("Invalid role")
            }
        }
        return userRepository.save(user)
    }

    /**
     * Удаление пользователя.
     */
    @Transactional
    fun deleteUser(username: String) {
        val user = userRepository.findByUsername(username)
            ?: throw IllegalArgumentException("User not found")
        userRepository.delete(user)
    }
}