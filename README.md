# Сервис авторизации (auth-service)

## Swagger

Доступен при запуске

http://localhost:8082/swagger-ui/index.html#/

## Описание

Сервис для регистрации, аутентификации и управления пользователями с использованием JWT-токенов. Эндпоинты `/api/user/**` доступны только для `ROLE_ADMIN`.

## Сборка

Требуется Java 21 и Gradle.

### Сборка
```shell
./gradlew clean build
```

### Запуск
```shell
./gradlew bootRun
```

## Конфигурация

Настройки в `src/main/resources/application.properties`:

| Переменная                 | Значение по умолчанию                                   | Описание                     |
|----------------------------|---------------------------------------------------------|------------------------------|
| SERVER_PORT                | 8080                                                    | Порт сервиса                 |
| SPRING_DATASOURCE_URL      | jdbc:postgresql://localhost:5432/test?currentSchema=auth | URL базы данных PostgreSQL   |
| SPRING_DATASOURCE_USERNAME | test                                                    | Пользователь базы данных     |
| SPRING_DATASOURCE_PASSWORD | test                                                    | Пароль базы данных           |

## Эндпоинты API

### 1. Регистрация пользователя
- **Эндпоинт**: `POST /api/auth/register`
- **Заголовки**: `Content-Type: application/json`
- **Тело запроса**:
  ```json
  {"username":"testuser","password":"password123","email":"testuser@example.com"}
  ```
- **Пример**:
  ```bash
  curl -X POST http://localhost:8080/api/auth/register -H "Content-Type: application/json" -d '{"username":"testuser","password":"password123","email":"testuser@example.com"}'
  ```
- **Ответ**:
    - 200 OK: `"User registered successfully"`
    - 400 Bad Request: `"Username already exists"`

### 2. Аутентификация
- **Эндпоинт**: `POST /api/auth/login`
- **Заголовки**: `Content-Type: application/json`
- **Тело запроса**:
  ```json
  {"username":"testuser","password":"password123"}
  ```
- **Пример**:
  ```bash
  curl -X POST http://localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"testuser","password":"password123"}'
  ```
- **Ответ**:
    - 200 OK: `{"token":"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."}`
    - 400 Bad Request: `"Invalid username or password"`

### 3. Создание пользователя (админ)
- **Эндпоинт**: `POST /api/user`
- **Заголовки**: `Content-Type: application/json`, `Authorization: Bearer <jwt-token>`
- **Тело запроса**:
  ```json
  {"username":"newuser","password":"newpass123","email":"newuser@example.com","role":"USER"}
  ```
- **Пример**:
  ```bash
  curl -X POST http://localhost:8080/api/user -H "Content-Type: application/json" -H "Authorization: Bearer <jwt-token>" -d '{"username":"newuser","password":"newpass123","email":"newuser@example.com","role":"USER"}'
  ```
- **Ответ**:
    - 200 OK: `"User created successfully"`
    - 403 Forbidden: `"Access Denied"`
    - 400 Bad Request: `"Username already exists"`

### 4. Обновление пользователя (админ)
- **Эндпоинт**: `PATCH /api/user/{username}`
- **Заголовки**: `Content-Type: application/json`, `Authorization: Bearer <jwt-token>`
- **Тело запроса**:
  ```json
  {"password":"newpass123","email":"updated@example.com","role":"USER"}
  ```
- **Пример**:
  ```bash
  curl -X PATCH http://localhost:8080/api/user/newuser -H "Content-Type: application/json" -H "Authorization: Bearer <jwt-token>" -d '{"password":"newpass123","email":"updated@example.com","role":"USER"}'
  ```
- **Ответ**:
    - 200 OK: `"User updated successfully"`
    - 403 Forbidden: `"Access Denied"`
    - 400 Bad Request: `"User not found"`

### 5. Удаление пользователя (админ)
- **Эндпоинт**: `DELETE /api/user/{username}`
- **Заголовки**: `Authorization: Bearer <jwt-token>`
- **Пример**:
  ```bash
  curl -X DELETE http://localhost:8080/api/user/newuser -H "Authorization: Bearer <jwt-token>"
  ```
- **Ответ**:
    - 200 OK: `"User deleted successfully"`
    - 403 Forbidden: `"Access Denied"`
    - 400 Bad Request: `"User not found"`

## Тестирование

Запуск тестов:
```shell
./gradlew test
```

Тесты используют Testcontainers (требуется Docker).

### Зависимости для тестов
- `org.springframework.boot:spring-boot-starter-test`
- `org.testcontainers:junit-jupiter`
- `org.testcontainers:postgresql`
- `com.fasterxml.jackson.module:jackson-module-kotlin`
