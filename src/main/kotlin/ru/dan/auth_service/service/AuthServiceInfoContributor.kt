package ru.dan.auth_service.service

import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.actuate.info.Info
import org.springframework.boot.actuate.info.InfoContributor
import org.springframework.stereotype.Component
import ru.dan.auth_service.repository.UserRepository

@Component
class AuthServiceInfoContributor(
    private val userRepository: UserRepository,
    @Value("\${spring.application.name}") private val appName: String,
    @Value("\${server.port}") private val serverPort: String
) : InfoContributor {

    override fun contribute(builder: Info.Builder) {
        val userCount = userRepository.count()

        builder.withDetail("app", mapOf(
            "name" to "Auth Service",
            "description" to "Authentication and Authorization Service for Microservices",
            "version" to "1.0.0"
        ))
        builder.withDetail("database", mapOf(
            "connected" to true,
            "schema" to "auth_service",
            "total-users" to userCount
        ))
        builder.withDetail("eureka", mapOf(
            "instance-id" to "$appName:$serverPort"
        ))
    }
}