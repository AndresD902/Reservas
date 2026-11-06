-- V2__admin_auth.sql
-- Administradores con credenciales propias para el panel de gestión.

CREATE TABLE administradores (
    id            BIGSERIAL PRIMARY KEY,
    nombre        VARCHAR(100) NOT NULL,
    correo        VARCHAR(100) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    CONSTRAINT uq_administradores_correo UNIQUE (correo)
);
