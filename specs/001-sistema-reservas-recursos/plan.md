<!-- Archivo: specs/001-sistema-reservas-recursos/plan.md -->
# Implementation Plan: Sistema de Reserva de Espacios y Recursos

## Technical Context

- **Lenguaje / Runtime**: Java 21 LTS
- **Framework**: Spring Boot 4.x (Spring Web MVC, Spring Data JPA, Spring Validation)
- **Base de Datos**: PostgreSQL (Base de datos relacional con soporte ACID)
- **Migraciones de BD**: Flyway Migration (`src/main/resources/db/migration/`)
- **Mapeo de Datos**: Mappers manuales como componentes de Spring (`@Component`)
- **Librerías de Soporte**: Lombok
- **Arquitectura de Capas**:
  - `controller`: Expone endpoints REST y mapea respuestas DTO.
  - `service`: Contiene la lógica de negocio, transacciones (`@Transactional`) y reglas de validación.
  - `repository`: Interfaces `JpaRepository` con consultas JPQL/Nativas.
  - `entity`: Modelos de datos de persistencia JPA.
  - `dto`: Clases Record / POJO para request y response.
  - `exception`: Excepciones personalizadas del dominio y `@RestControllerAdvice`.
  - `mapper`: Componentes para transformar de Entidad a DTO y viceversa.

## Constitution Check

- **Separación de Capas**: Cumple. Los controladores no inyectan repositorios directamente; usan únicamente interfaces de servicios.
- **Invariantes de Negocio**: Cumple. Todas las reglas (máximo 3 reservas, traslape, fechas pasadas, estado de usuario y recurso) se validan en la capa `service` bajo transacciones relacionales.
- **Control de Concurrencia**: Cumple. Se implementa bloqueo pesimista (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) o validación a nivel de base de datos en las consultas de solapamiento de reservas durante la creación.
- **Diseño para Pruebas**: Cumple. Servicios desacoplados de controladores, listos para pruebas con JUnit 5, Mockito y PIT.

## Phase 0: Research

1. **Prevención de Solapamiento de Reservas y Condición de Carrera**:
   - *Decisión*: Aplicar bloqueo pesimista a nivel de base de datos sobre el `Recurso` al momento de validar y registrar la reserva, o utilizar una consulta transaccional `SELECT COUNT(...) FOR UPDATE` para verificar superposición.
   - *Consulta SQL de solapamiento*: Dos rangos [A, B] y [C, D] se solapan si `max(A, C) < min(B, D)`, expresado en SQL como `(fecha_inicio < :nuevaFin AND fecha_fin > :nuevaInicio)`.

2. **Estrategia de Migración de Base de Datos**:
   - *Decisión*: Flyway con scripts de versionado SQL en `V1__init_schema.sql` para garantizar reproducibilidad en todos los entornos de prueba.

## Phase 1: Design

### Modelo de Excepciones y Respuesta HTTP

Estructura de error unificada según requerimiento:

```json
{
  "status": 409,
  "error": "RECURSO_NO_DISPONIBLE",
  "message": "El recurso ya se encuentra reservado para este horario"
}
```

## Phase 2: Ciclo de vida de la reserva y mantenimiento

### Finalización automática de reservas vencidas

- **Decisión**: proceso programado con Spring `@Scheduled` (`@EnableScheduling`), con la frecuencia configurable por la propiedad `reservas.finalizacion.frecuencia` en formato ISO-8601; valor por defecto `PT5M` (5 minutos).
- **Comportamiento**: en cada ejecución, dentro de una transacción, marca como `FINALIZADA` las reservas en estado `ACTIVA` cuya `fecha_fin` sea anterior al instante de ejecución. No modifica `CANCELADA` ni `FINALIZADA`.
- **Consulta**: `ReservaRepository.finalizarVencidas(ahora)` como `UPDATE ... WHERE estado = 'ACTIVA' AND fecha_fin < :ahora`. Se apoya en el índice `idx_reservas_estado_fin`.
- **Justificación**: un job desacoplado mantiene el estado correcto sin penalizar las lecturas ni exigir una petición del usuario.

### Cambio de un recurso a `MANTENIMIENTO`

- **Decisión**: bloquear la transición si el recurso tiene reservas `ACTIVA` con `fecha_fin` posterior al momento del cambio, incluidas las que están en curso.
- **Comportamiento**: `RecursoService.actualizarEstado` consulta las reservas vigentes o futuras antes de persistir el cambio; si existen, lanza el error de negocio `RECURSO_CON_RESERVAS_FUTURAS` (HTTP 409). El administrador debe cancelarlas primero.
- **Alternativa descartada**: cancelar automáticamente las reservas, porque destruye reservas ya confirmadas sin intervención del usuario.

## Phase 3: Consulta de reservas por rango de fechas

- **Endpoint**: `GET /reservas/rango?fechaInicio=<ISO-8601>&fechaFin=<ISO-8601>`.
- **Parámetros**: `fechaInicio` y `fechaFin` son obligatorios y se interpretan como `LocalDateTime` en formato ISO-8601. No se combinan con filtros por usuario o recurso: esos casos ya tienen endpoints específicos.
- **Semántica**: devuelve las reservas cuyo intervalo se solapa con el rango, con la misma regla semiabierta `[inicio, fin)` de la creación: `r.fechaInicio < :fechaFin AND r.fechaFin > :fechaInicio`.
- **Estados**: devuelve reservas en cualquier estado; el DTO incluye `estado` para que el cliente filtre.
- **Validación**: si `fechaFin` no es estrictamente posterior a `fechaInicio`, se lanza `BusinessRuleException` con error `FECHAS_INVALIDAS` y se responde 400.
- **Consulta**: `ReservaRepository.findOverlappingInRange(fechaInicio, fechaFin)`, ordenada por `fechaInicio ASC`.
- **Respuesta**: `List<ReservaResponse>` con el mismo DTO de las consultas existentes.

## Phase 4: Paginación y filtros en listados

- **Endpoints**: `GET /usuarios`, `GET /recursos` y un nuevo `GET /reservas`. Los endpoints `/reservas/usuario/{id}`, `/reservas/recurso/{id}` y `/reservas/rango` mantienen su contrato sin paginar.
- **Parámetros**: `page` (base 0), `size` (20 por defecto, 100 máximo) y `sort` con el formato de Spring Data (`campo,asc|desc`), resueltos por el `PageableHandlerMethodArgumentResolver`.
- **Filtros**:
  - Usuarios: `estado`.
  - Recursos: `estado` y `tipo`; el tipo se compara sin distinguir mayúsculas y se ignora si viene en blanco.
  - Reservas: `estado`, `usuarioId` y `recursoId`; los tres son opcionales y se combinan con AND.
- **Respuesta**: `Page<T>` de Spring Data con `content`, `totalElements`, `totalPages`, `number`, `size` y los metadatos de ordenamiento.
- **Consulta de reservas**: `ReservaRepository.buscar(estado, usuarioId, recursoId, pageable)` con filtros nulos opcionales, para no duplicar consultas por cada combinación.
- **Compatibilidad**: los listados dejan de devolver un arreglo plano; el cambio se documenta en esta historia y el contrato OpenAPI se crea en HU-11.
