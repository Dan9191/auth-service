package ru.dan.auth_service

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.test.context.ContextConfiguration
import ru.dan.auth_service.config.BaseTestWithContext
import ru.dan.auth_service.config.SecurityConfig
import ru.dan.auth_service.model.CreateUserRequest
import ru.dan.auth_service.model.LoginRequest
import ru.dan.auth_service.model.LoginResponse
import ru.dan.auth_service.model.RegisterRequest
import ru.dan.auth_service.model.UpdateUserRequest
import ru.dan.auth_service.service.JwtUtil
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@ContextConfiguration(classes = [AuthServiceApplication::class, SecurityConfig::class])
class AuthServiceApplicationTests : BaseTestWithContext() {

	@Autowired
	private lateinit var restTemplate: TestRestTemplate

	@Autowired
	private lateinit var jwtUtil: JwtUtil

	private lateinit var adminToken: String

	@BeforeEach
	fun setupAdminUser() {

		val tempLogin = LoginRequest(username = "admin", password = "password")
		val tempLoginResponse = restTemplate.postForEntity(
			"/api/auth/login",
			HttpEntity(tempLogin),
			LoginResponse::class.java
		)
		adminToken = tempLoginResponse.body!!.token
	}

	@Test
	@DisplayName("Context loads successfully")
	fun contextLoads() {
	}

	@Test
	@DisplayName("Регистрация нового пользователя")
	fun shouldRegisterNewUserSuccessfully() {
		// Arrange
		val request = RegisterRequest(
			username = "testuser",
			password = "password123",
			email = "testuser@example.com"
		)
		val httpEntity = HttpEntity(request)

		// Act
		val response = restTemplate.postForEntity(
			"/api/auth/register",
			httpEntity,
			String::class.java
		)

		// Assert
		assertEquals(HttpStatus.OK, response.statusCode)
		assertEquals("User registered successfully", response.body)
	}

	@Test
	@DisplayName("Ошибка регистрации пользователя с существующим именем")
	fun shouldFailToRegisterExistingUsername() {
		// Arrange
		val request = RegisterRequest(
			username = "testuser",
			password = "password123",
			email = "testuser@example.com"
		)
		restTemplate.postForEntity("/api/auth/register", HttpEntity(request), String::class.java)

		// Act
		val response = restTemplate.postForEntity(
			"/api/auth/register",
			HttpEntity(request),
			String::class.java
		)

		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
	}

	@Test
	@DisplayName("Логин и получение jwt")
	fun shouldLoginSuccessfully() {
		// Arrange
		val registerRequest = RegisterRequest(
			username = "testuser2",
			password = "password123",
			email = "testuser2@example.com"
		)
		restTemplate.postForEntity("/api/auth/register", HttpEntity(registerRequest), String::class.java)

		val loginRequest = LoginRequest(
			username = "testuser2",
			password = "password123"
		)
		val httpEntity = HttpEntity(loginRequest)

		// Act
		val response = restTemplate.postForEntity(
			"/api/auth/login",
			httpEntity,
			LoginResponse::class.java
		)

		// Assert
		assertEquals(HttpStatus.OK, response.statusCode)
		assertNotNull(response.body?.token)
		assertTrue(jwtUtil.validateToken(response.body!!.token))
		assertEquals("testuser2", jwtUtil.getUsernameFromToken(response.body!!.token))
		assertEquals(listOf("ROLE_USER"), jwtUtil.getRolesFromToken(response.body!!.token))
	}

	@Test
	@DisplayName("Ошибка логина с неверными кредами")
	fun shouldFailToLoginWithInvalidCredentials() {
		// Arrange
		val loginRequest = LoginRequest(
			username = "nonexistent",
			password = "wrongpassword"
		)
		val httpEntity = HttpEntity(loginRequest)

		// Act
		val response = restTemplate.postForEntity(
			"/api/auth/login",
			httpEntity,
			String::class.java
		)

		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
	}

	@Test
	@DisplayName("Создание пользователя через администратора")
	fun shouldCreateUserAsAdmin() {
		// Arrange
		val createUserRequest = CreateUserRequest(
			username = "newuser",
			password = "newpass123",
			email = "newuser@example.com",
			role = "USER"
		)
		val headers = HttpHeaders().apply {
			set("Authorization", "Bearer $adminToken")
		}
		val httpEntity = HttpEntity(createUserRequest, headers)

		// Act
		val response = restTemplate.postForEntity(
			"/api/user",
			httpEntity,
			String::class.java
		)

		// Assert
		assertEquals(HttpStatus.OK, response.statusCode)
		assertEquals("User created successfully", response.body)
	}

	@Test
	@DisplayName("Обновление пользователя")
	fun shouldUpdateUserAsAdmin() {
		// Arrange: Register a user to update
		val userRequest = RegisterRequest(
			username = "updateuser",
			password = "oldpass",
			email = "old@example.com"
		)
		restTemplate.postForEntity("/api/auth/register", HttpEntity(userRequest), String::class.java)

		// Update user request
		val updateRequest = UpdateUserRequest(
			password = "newpass123",
			email = "new@example.com",
			role = "USER"
		)
		val headers = HttpHeaders().apply {
			set("Authorization", "Bearer $adminToken")
		}
		val httpEntity = HttpEntity(updateRequest, headers)

		// Act
		val response = restTemplate.exchange(
			"/api/user/updateuser",
			HttpMethod.PATCH,
			httpEntity,
			String::class.java
		)

		// Assert
		assertEquals(HttpStatus.OK, response.statusCode)
		assertEquals("User updated successfully", response.body)
	}

	@Test
	@DisplayName("Удаление пользователя через администратора")
	fun shouldDeleteUserAsAdmin() {
		// Arrange: Register a user to delete
		val userRequest = RegisterRequest(
			username = "deleteuser",
			password = "userpass",
			email = "delete@example.com"
		)
		restTemplate.postForEntity("/api/auth/register", HttpEntity(userRequest), String::class.java)

		// Delete user
		val headers = HttpHeaders().apply {
			set("Authorization", "Bearer $adminToken")
		}
		val httpEntity = HttpEntity<Any>(headers)

		// Act
		val response = restTemplate.exchange(
			"/api/user/deleteuser",
			HttpMethod.DELETE,
			httpEntity,
			String::class.java
		)

		// Assert
		assertEquals(HttpStatus.OK, response.statusCode)
		assertEquals("User deleted successfully", response.body)
	}

	@Test
	@DisplayName("Запрос не доступен для НЕ администратора")
	fun shouldDenyAccessToUserEndpointsForNonAdmin() {
		// Arrange: Register and login as non-admin user
		val userRequest = RegisterRequest(
			username = "regularuser",
			password = "userpass",
			email = "user@example.com"
		)
		restTemplate.postForEntity("/api/auth/register", HttpEntity(userRequest), String::class.java)

		val loginRequest = LoginRequest(username = "regularuser", password = "userpass")
		val loginResponse = restTemplate.postForEntity(
			"/api/auth/login",
			HttpEntity(loginRequest),
			LoginResponse::class.java
		)
		val token = loginResponse.body!!.token

		// Attempt to create user as non-admin
		val createUserRequest = CreateUserRequest(
			username = "newuser2",
			password = "newpass123",
			email = "newuser2@example.com",
			role = "USER"
		)
		val headers = HttpHeaders().apply {
			set("Authorization", "Bearer $token")
		}
		val httpEntity = HttpEntity(createUserRequest, headers)

		// Act
		val response = restTemplate.postForEntity(
			"/api/user",
			httpEntity,
			String::class.java
		)

		// Assert
		assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
	}
}