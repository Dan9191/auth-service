CREATE TABLE users (
--                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       id BIGSERIAL PRIMARY KEY,
                       username VARCHAR(255) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       role VARCHAR(255) NOT NULL DEFAULT 'USER'
);