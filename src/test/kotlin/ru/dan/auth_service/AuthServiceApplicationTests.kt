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
import ru.dan.auth_service.model.UserDto
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
		assertEquals(HttpStatus.OK, tempLoginResponse.statusCode)
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

		// Act
		val response = restTemplate.postForEntity(
			"/api/auth/register",
			HttpEntity(request),
			UserDto::class.java
		)

		// Assert
		assertEquals(HttpStatus.OK, response.statusCode)
		val userDto = response.body
		assertNotNull(userDto)
		assertEquals("testuser", userDto.username)
		assertEquals("testuser@example.com", userDto.email)
		assertEquals("ROLE_USER", userDto.role)
	}

	@Test
	@DisplayName("Регистрация пользователя с существующим именем")
	fun shouldFailToRegisterExistingUser() {
		// Arrange: Register a user first
		val request = RegisterRequest(
			username = "duplicateuser",
			password = "password123",
			email = "duplicate@example.com"
		)
		restTemplate.postForEntity("/api/auth/register", HttpEntity(request), UserDto::class.java)

		// Act: Try to register the same user again
		val response = restTemplate.postForEntity(
			"/api/auth/register",
			HttpEntity(request),
			Map::class.java
		)

		// Assert
		assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
		val error = response.body as Map<*, *>
		assertEquals("Username already exists", error["message"])
	}

	@Test
	@DisplayName("Аутентификация пользователя")
	fun shouldAuthenticateSuccessfully() {
		// Arrange: Register a user first
		val registerRequest = RegisterRequest(
			username = "authuser",
			password = "authpass",
			email = "authuser@example.com"
		)
		restTemplate.postForEntity("/api/auth/register", HttpEntity(registerRequest), UserDto::class.java)

		val request = LoginRequest(username = "authuser", password = "authpass")

		// Act
		val response = restTemplate.postForEntity(
			"/api/auth/login",
			HttpEntity(request),
			LoginResponse::class.java
		)

		// Assert
		assertEquals(HttpStatus.OK, response.statusCode)
		val loginResponse = response.body
		assertNotNull(loginResponse)
		val token = loginResponse.token
		assertNotNull(token)
		assertEquals("authuser", jwtUtil.getUsernameFromToken(token))
		assertTrue(jwtUtil.getRolesFromToken(token).contains("ROLE_USER"))
	}

	@Test
	@DisplayName("Создание пользователя администратором")
	fun shouldCreateUserAsAdmin() {
		// Arrange
		val request = CreateUserRequest(
			username = "newuser",
			password = "newpass123",
			email = "newuser@example.com",
			role = "USER"
		)
		val headers = HttpHeaders().apply {
			set("Authorization", "Bearer $adminToken")
		}
		val httpEntity = HttpEntity(request, headers)

		// Act
		val response = restTemplate.postForEntity(
			"/api/user",
			httpEntity,
			UserDto::class.java
		)

		// Assert
		assertEquals(HttpStatus.CREATED, response.statusCode)
		val userDto = response.body
		assertNotNull(userDto)
		assertEquals("newuser", userDto.username)
		assertEquals("newuser@example.com", userDto.email)
		assertEquals("ROLE_USER", userDto.role)
	}

	@Test
	@DisplayName("Обновление пользователя администратором")
	fun shouldUpdateUserAsAdmin() {
		// Arrange: Create a user to update
		val createRequest = CreateUserRequest(
			username = "updateuser",
			password = "userpass",
			email = "update@example.com",
			role = "USER"
		)
		val createHeaders = HttpHeaders().apply {
			set("Authorization", "Bearer $adminToken")
		}
		restTemplate.postForEntity("/api/user", HttpEntity(createRequest, createHeaders), UserDto::class.java)

		val updateRequest = UpdateUserRequest(
			password = "updatedpass",
			email = "updatedemail@example.com",
			role = "ADMIN"
		)
		val updateHeaders = HttpHeaders().apply {
			set("Authorization", "Bearer $adminToken")
		}
		val httpEntity = HttpEntity(updateRequest, updateHeaders)

		// Act
		val response = restTemplate.exchange(
			"/api/user/updateuser",
			HttpMethod.PATCH,
			httpEntity,
			UserDto::class.java
		)

		// Assert
		assertEquals(HttpStatus.OK, response.statusCode)
		val userDto = response.body
		assertNotNull(userDto)
		assertEquals("updateuser", userDto.username)
		assertEquals("updatedemail@example.com", userDto.email)
		assertEquals("ROLE_ADMIN", userDto.role)
	}

	@Test
	@DisplayName("Удаление пользователя администратором")
	fun shouldDeleteUserAsAdmin() {
		// Arrange: Register a user to delete
		val userRequest = RegisterRequest(
			username = "deleteuser",
			password = "userpass",
			email = "delete@example.com"
		)
		restTemplate.postForEntity("/api/auth/register", HttpEntity(userRequest), UserDto::class.java)

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
			Void::class.java
		)

		// Assert
		assertEquals(HttpStatus.NO_CONTENT, response.statusCode)
	}

	@Test
	@DisplayName("Получение пользователя администратором")
	fun shouldGetUserAsAdmin() {
		// Arrange: Create a user to get
		val userRequest = CreateUserRequest(
			username = "getuser",
			password = "userpass",
			email = "getuser@example.com",
			role = "USER"
		)
		val createHeaders = HttpHeaders().apply {
			set("Authorization", "Bearer $adminToken")
		}
		restTemplate.postForEntity("/api/user", HttpEntity(userRequest, createHeaders), UserDto::class.java)

		// Get user
		val headers = HttpHeaders().apply {
			set("Authorization", "Bearer $adminToken")
		}
		val httpEntity = HttpEntity<Any>(headers)

		// Act
		val response = restTemplate.exchange(
			"/api/user/getuser",
			HttpMethod.GET,
			httpEntity,
			UserDto::class.java
		)

		// Assert
		assertEquals(HttpStatus.OK, response.statusCode)
		val userDto = response.body
		assertNotNull(userDto)
		assertEquals("getuser", userDto.username)
		assertEquals("getuser@example.com", userDto.email)
		assertEquals("ROLE_USER", userDto.role)
	}

	@Test
	@DisplayName("Получение пользователя для booking-service")
	fun shouldGetUserAsBookingService() {
		// Arrange: Register a user
		val userRequest = RegisterRequest(
			username = "bookinguser",
			password = "userpass",
			email = "bookinguser@example.com"
		)
		restTemplate.postForEntity("/api/auth/register", HttpEntity(userRequest), UserDto::class.java)

		// Create booking-service user with ROLE_BOOKING_SERVICE
		val bookingServiceRequest = CreateUserRequest(
			username = "bookingservice",
			password = "servicepass",
			email = "booking@service.com",
			role = "BOOKING_SERVICE"
		)
		val createHeaders = HttpHeaders().apply {
			set("Authorization", "Bearer $adminToken")
		}
		restTemplate.postForEntity("/api/user", HttpEntity(bookingServiceRequest, createHeaders), UserDto::class.java)

		// Login as booking-service
		val loginRequest = LoginRequest(username = "bookingservice", password = "servicepass")
		val loginResponse = restTemplate.postForEntity(
			"/api/auth/login",
			HttpEntity(loginRequest),
			LoginResponse::class.java
		)
		assertEquals(HttpStatus.OK, loginResponse.statusCode)
		val bookingServiceToken = loginResponse.body!!.token

		// Verify token roles
		val roles = jwtUtil.getRolesFromToken(bookingServiceToken)
		assertTrue(roles.contains("ROLE_BOOKING_SERVICE"), "Token must contain ROLE_BOOKING_SERVICE")

		// Get user as booking-service
		val headers = HttpHeaders().apply {
			set("Authorization", "Bearer $bookingServiceToken")
		}
		val httpEntity = HttpEntity<Any>(headers)

		// Act
		val response = restTemplate.exchange(
			"/api/user/bookinguser",
			HttpMethod.GET,
			httpEntity,
			UserDto::class.java
		)

		// Assert
		assertEquals(HttpStatus.OK, response.statusCode)
		val userDto = response.body
		assertNotNull(userDto)
		assertEquals("bookinguser", userDto.username)
		assertEquals("bookinguser@example.com", userDto.email)
		assertEquals("ROLE_USER", userDto.role)
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
		restTemplate.postForEntity("/api/auth/register", HttpEntity(userRequest), UserDto::class.java)

		val loginRequest = LoginRequest(username = "regularuser", password = "userpass")
		val loginResponse = restTemplate.postForEntity(
			"/api/auth/login",
			HttpEntity(loginRequest),
			LoginResponse::class.java
		)
		assertEquals(HttpStatus.OK, loginResponse.statusCode)
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
			Map::class.java
		)

		// Assert
		assertEquals(HttpStatus.FORBIDDEN, response.statusCode)
		val error = response.body as Map<*, *>
		assertEquals("Access Denied", error["message"])
	}
}