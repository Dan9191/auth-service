package ru.dan.auth_service.config

import org.testcontainers.containers.PostgreSQLContainer
/**
 * Синглтон фабрика тест контейнеров.
 */
object TestContainersFactory {

    private const val POSTGRES_IMAGE = "postgres:17.6-alpine3.21"

    val POSTGRES: PostgreSQLContainer<*> = PostgreSQLContainer(POSTGRES_IMAGE)
        .withDatabaseName("test")
        .withUsername("sa")
        .withPassword("sa")

    init {
        POSTGRES.start()

        Runtime.getRuntime().addShutdownHook(Thread {
            POSTGRES.stop()
        })
    }
}