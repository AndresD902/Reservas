-- V1__init_schema.sql
-- Esquema inicial del sistema de reservas de recursos (PostgreSQL)

CREATE TABLE usuarios (
    id        BIGSERIAL PRIMARY KEY,
    nombre    VARCHAR(100) NOT NULL,
    documento VARCHAR(20)  NOT NULL,
    correo    VARCHAR(100) NOT NULL,
    estado    VARCHAR(20)  NOT NULL,
    CONSTRAINT uq_usuarios_documento UNIQUE (documento),
    CONSTRAINT uq_usuarios_correo UNIQUE (correo),
    CONSTRAINT ck_usuarios_estado CHECK (estado IN ('ACTIVO', 'INACTIVO'))
);

CREATE TABLE recursos (
    id          BIGSERIAL PRIMARY KEY,
    nombre      VARCHAR(100) NOT NULL,
    descripcion VARCHAR(255),
    tipo        VARCHAR(50)  NOT NULL,
    capacidad   INTEGER      NOT NULL,
    estado      VARCHAR(20)  NOT NULL,
    CONSTRAINT ck_recursos_capacidad CHECK (capacidad > 0),
    CONSTRAINT ck_recursos_estado CHECK (estado IN ('DISPONIBLE', 'MANTENIMIENTO', 'FUERA_DE_SERVICIO'))
);

CREATE TABLE reservas (
    id             BIGSERIAL PRIMARY KEY,
    usuario_id     BIGINT      NOT NULL,
    recurso_id     BIGINT      NOT NULL,
    fecha_inicio   TIMESTAMP   NOT NULL,
    fecha_fin      TIMESTAMP   NOT NULL,
    estado         VARCHAR(20) NOT NULL,
    fecha_creacion TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_reservas_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT fk_reservas_recurso FOREIGN KEY (recurso_id) REFERENCES recursos (id),
    CONSTRAINT ck_reservas_estado CHECK (estado IN ('ACTIVA', 'CANCELADA', 'FINALIZADA')),
    CONSTRAINT ck_reservas_fechas CHECK (fecha_fin > fecha_inicio)
);

CREATE INDEX idx_reservas_recurso_fechas ON reservas (recurso_id, fecha_inicio, fecha_fin, estado);
CREATE INDEX idx_reservas_usuario_estado ON reservas (usuario_id, estado);
