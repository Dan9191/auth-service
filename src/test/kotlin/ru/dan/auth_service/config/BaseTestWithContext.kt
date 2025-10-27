package ru.dan.auth_service.config

import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.SpringBootTest.*
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.DynamicPropertyRegistry
import org.springframework.test.context.DynamicPropertySource
import org.springframework.test.context.junit.jupiter.SpringExtension

/**
 * Базовый класс с полным контекстом сервиса для тестов.
 */
@ExtendWith(SpringExtension::class)
@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
abstract class BaseTestWithContext {

    companion object {
        @JvmStatic
        @DynamicPropertySource
        fun configureProperties(registry: DynamicPropertyRegistry) {
            registry.add("spring.datasource.url") {
                "jdbc:postgresql://${TestContainersFactory.POSTGRES.host}:${TestContainersFactory.POSTGRES.firstMappedPort}/test?currentSchema=patients_history"
            }
            registry.add("spring.datasource.username") { TestContainersFactory.POSTGRES.username }
            registry.add("spring.datasource.password") { TestContainersFactory.POSTGRES.password }
        }
    }
}