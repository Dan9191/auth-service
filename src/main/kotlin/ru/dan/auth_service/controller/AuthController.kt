package ru.dan.auth_service.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import ru.dan.auth_service.model.LoginRequest
import ru.dan.auth_service.model.LoginResponse
import ru.dan.auth_service.model.RegisterRequest
import ru.dan.auth_service.service.JwtUtil
import ru.dan.auth_service.service.UserService

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val userService: UserService,
    private val jwtUtil: JwtUtil
) {
    @PostMapping("/register")
    fun register(@RequestBody request: RegisterRequest): ResponseEntity<String> {
        userService.register(request)
        return ResponseEntity.ok("User registered successfully")
    }

    @PostMapping("/login")
    fun login(@RequestBody request: LoginRequest): ResponseEntity<LoginResponse> {
        val user = userService.authenticate(request)
        val token = jwtUtil.generateToken(user.username, user.role.name)
        return ResponseEntity.ok(LoginResponse(token))
    }
}