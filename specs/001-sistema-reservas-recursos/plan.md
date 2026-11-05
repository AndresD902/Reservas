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