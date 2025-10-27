package ru.dan.auth_service.controller

import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import ru.dan.auth_service.model.CreateUserRequest
import ru.dan.auth_service.model.UpdateUserRequest
import ru.dan.auth_service.service.UserService

@RestController
@RequestMapping("/api/user")
class UserController(
    private val userService: UserService
) {
    @PostMapping
    fun createUser(@RequestBody request: CreateUserRequest): ResponseEntity<String> {
        userService.createUser(request)
        return ResponseEntity.ok("User created successfully")
    }

    @PatchMapping("/{username}")
    fun updateUser(@PathVariable username: String, @RequestBody request: UpdateUserRequest): ResponseEntity<String> {
        userService.updateUser(username, request)
        return ResponseEntity.ok("User updated successfully")
    }

    @DeleteMapping("/{username}")
    fun deleteUser(@PathVariable username: String): ResponseEntity<String> {
        userService.deleteUser(username)
        return ResponseEntity.ok("User deleted successfully")
    }
}