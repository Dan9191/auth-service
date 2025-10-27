package ru.dan.auth_service.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.dan.auth_service.model.LoginRequest
import ru.dan.auth_service.model.LoginResponse
import ru.dan.auth_service.model.RegisterRequest
import ru.dan.auth_service.model.UserDto
import ru.dan.auth_service.service.JwtUtil
import ru.dan.auth_service.service.UserService

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val jwtUtil: JwtUtil
) {
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<UserDto> {
        val user = userService.register(request)
        return ResponseEntity.ok(user)
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val user = userService.authenticate(request)
        val token = jwtUtil.generateToken(user.username, user.role)
        return ResponseEntity.ok(LoginResponse(token))
    }
}