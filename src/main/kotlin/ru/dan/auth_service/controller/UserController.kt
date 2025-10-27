package ru.dan.auth_service.controller

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.dan.auth_service.model.CreateUserRequest
import ru.dan.auth_service.model.UpdateUserRequest
import ru.dan.auth_service.model.UserDto
import ru.dan.auth_service.service.UserService

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService
) {
    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<UserDto> {
        val user = userService.createUser(request)
        return ResponseEntity.status(HttpStatus.CREATED).body(user)
    }

    @PatchMapping("/{username}")
    fun updateUser(@PathVariable username: String, @RequestBody request: UpdateUserRequest): ResponseEntity<UserDto> {
        val user = userService.updateUser(username, request)
        return ResponseEntity.ok(user)
    }

    @DeleteMapping("/{username}")
    fun deleteUser(@PathVariable username: String): ResponseEntity<Void> {
        userService.deleteUser(username)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/{username}")
    fun getUser(@PathVariable username: String): ResponseEntity<UserDto> {
        val user = userService.getUser(username)
        return ResponseEntity.ok(user)
    }
}