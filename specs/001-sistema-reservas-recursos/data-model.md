# Modelo de Datos

## Diagrama Entidad-Relación (Textual)

```
+------------------+           1 : N           +------------------+
|     USUARIO      |---------------------------<     RESERVA      |
+------------------+                           +------------------+
| id (PK)          |                               | id (PK)          |
| nombre           |                               | usuario_id (FK)  |
| documento        |                               | recurso_id (FK)  |
| correo           |                           N : 1 | fecha_inicio     |
| estado           |---------------------------< fecha_fin        |
+------------------+                           | estado           |
                                               | fecha_creacion   |
+------------------+                           +------------------+
|     RECURSO      |
+------------------+
| id (PK)          |
| nombre           |
| descripcion      |
| tipo             |
| capacidad        |
| estado           |
+------------------+
```

## Definición de Entidades

### 1. Entidad: `Usuario` (`usuarios`)
- **`id`**: `BIGINT` | Primary Key | Auto-incremental.
- **`nombre`**: `VARCHAR(100)` | NOT NULL.
- **`documento`**: `VARCHAR(20)` | NOT NULL | Unique Index.
- **`correo`**: `VARCHAR(100)` | NOT NULL | Unique Index.
- **`estado`**: `VARCHAR(20)` | NOT NULL | Valores permitidos: `'ACTIVO'`, `'INACTIVO'`.

### 2. Entidad: `Recurso` (`recursos`)
- **`id`**: `BIGINT` | Primary Key | Auto-incremental.
- **`nombre`**: `VARCHAR(100)` | NOT NULL.
- **`descripcion`**: `VARCHAR(255)` | NULLABLE.
- **`tipo`**: `VARCHAR(50)` | NOT NULL (ej. 'LABORATORIO', 'AUDITORIO', 'EQUIPO').
- **`capacidad`**: `INTEGER` | NOT NULL | Mayor a 0.
- **`estado`**: `VARCHAR(20)` | NOT NULL | Valores permitidos: `'DISPONIBLE'`, `'MANTENIMIENTO'`, `'FUERA_DE_SERVICIO'`.

### 3. Entidad: `Reserva` (`reservas`)
- **`id`**: `BIGINT` | Primary Key | Auto-incremental.
- **`usuario_id`**: `BIGINT` | Foreign Key -> `usuarios(id)` | NOT NULL.
- **`recurso_id`**: `BIGINT` | Foreign Key -> `recursos(id)` | NOT NULL.
- **`fecha_inicio`**: `TIMESTAMP` | NOT NULL.
- **`fecha_fin`**: `TIMESTAMP` | NOT NULL.
- **`estado`**: `VARCHAR(20)` | NOT NULL | Valores permitidos: `'ACTIVA'`, `'CANCELADA'`, `'FINALIZADA'`.
- **`fecha_creacion`**: `TIMESTAMP` | NOT NULL | Default `CURRENT_TIMESTAMP`.

## Índices y Restricciones de Integridad

- **Índice Compuesto de Búsqueda de Reservas**:
  - `CREATE INDEX idx_reservas_recurso_fechas ON reservas(recurso_id, fecha_inicio, fecha_fin, estado);`
  - Optimiza la consulta de solapamiento de horarios para un recurso específico.
- **Restricción de Coherencia de Fechas**:
  - `CONSTRAINT ck_reservas_fechas CHECK (fecha_fin > fecha_inicio)`
  - Refuerza en la base de datos la regla de que la fecha final sea posterior a la inicial.
- **Índice de Conteo por Usuario**:
  - `CREATE INDEX idx_reservas_usuario_estado ON reservas(usuario_id, estado);`
  - Optimiza la validación del límite de 3 reservas activas simultáneas.
- **Índice de Finalización Automática**:
  - `CREATE INDEX idx_reservas_estado_fin ON reservas(estado, fecha_fin);`
  - Optimiza la búsqueda de reservas `ACTIVA` vencidas que ejecuta el proceso programado.

## Transiciones de estado de la reserva

| Origen | Destino | Disparador | Regla |
|---|---|---|---|
| `ACTIVA` | `CANCELADA` | Cancelación explícita (`DELETE /reservas/{id}`) | Solo una reserva `ACTIVA` se puede cancelar (FR-014). |
| `ACTIVA` | `FINALIZADA` | Proceso programado, frecuencia configurable (5 minutos por defecto) | Ocurre cuando `fecha_fin` es anterior al instante de ejecución. |
| `CANCELADA` | — | — | Estado terminal; ninguna transición posterior. |
| `FINALIZADA` | — | — | Estado terminal; ninguna transición posterior. |