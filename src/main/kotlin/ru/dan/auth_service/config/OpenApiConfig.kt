package ru.dan.auth_service.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class OpenApiConfig {

    @Bean
    fun api(): OpenAPI = OpenAPI()
        .info(
            Info()
                .title("My MVC API")
                .description("Simple REST API using Spring Boot + Kotlin + SpringDoc")
                .version("1.0.0")
        )
}