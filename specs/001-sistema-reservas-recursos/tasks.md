<!-- Archivo: specs/001-sistema-reservas-recursos/tasks.md -->
# Tareas de Implementación (Tasks)

## Fase 1: Configuración Inicial y Migraciones

- [ ] **T-001**: Crear estructura de proyecto Java 17 / Spring Boot con paquetes `controller`, `service`, `repository`, `entity`, `dto`, `exception`, `mapper`.
  - **Archivos**: `pom.xml` / `build.gradle`, estructura de paquetes.
  - **Dependencias**: Ninguna.

- [ ] **T-002**: Configurar base de datos y script de migración Flyway inicial.
  - **Archivos**: `src/main/resources/application.yml`, `src/main/resources/db/migration/V1__init_schema.sql`.
  - **Dependencias**: T-001.

## Fase 2: Entidades y Repositorios

- [ ] **T-003**: Implementar entidades JPA (`UsuarioEntity`, `RecursoEntity`, `ReservaEntity`).
  - **Archivos**: `entity/UsuarioEntity.java`, `entity/RecursoEntity.java`, `entity/ReservaEntity.java`.
  - **Dependencias**: T-002.

- [ ] **T-004**: Crear interfaces Repositorio JPA con consultas de validación de reservas y bloqueo pesimista.
  - **Archivos**: `repository/UsuarioRepository.java`, `repository/RecursoRepository.java`, `repository/ReservaRepository.java`.
  - **Dependencias**: T-003.

## Fase 3: Manejo Global de Excepciones y DTOs

- [ ] **T-005**: Definir DTOs de Request/Response y Mappers para Usuario, Recurso y Reserva.
  - **Archivos**: `dto/*`, `mapper/*`.
  - **Dependencias**: T-003.

- [ ] **T-006**: Implementar `@RestControllerAdvice` y excepciones personalizadas (`ResourceNotFoundException`, `BusinessRuleException`, `ReservationConflictException`).
  - **Archivos**: `exception/GlobalExceptionHandler.java`, `exception/ErrorResponseDto.java`, `exception/*Exception.java`.
  - **Dependencias**: T-005.

## Fase 4: Capa de Servicios y Reglas de Negocio

- [ ] **T-007**: Implementar `UsuarioService` con operaciones de creación, consulta y actualización de estado (`ACTIVO`/`INACTIVO`).
  - **Archivos**: `service/UsuarioService.java`, `service/impl/UsuarioServiceImpl.java`.
  - **Dependencias**: T-004, T-006.

- [ ] **T-008**: Implementar `RecursoService` con gestión de recursos y actualización de estado (`DISPONIBLE`, `MANTENIMIENTO`, `FUERA_DE_SERVICIO`).
  - **Archivos**: `service/RecursoService.java`, `service/impl/RecursoServiceImpl.java`.
  - **Dependencias**: T-004, T-006.

- [ ] **T-009**: Implementar `ReservaService` incorporando todas las reglas de negocio obligatorias:
  1. Usuario activo.
  2. Recurso disponible.
  3. Validar `fechaFin > fechaInicio`.
  4. Validar fecha actual (no fechas pasadas).
  5. Validar máximo 3 reservas activas por usuario.
  6. Validar no solapamiento de horarios (con concurrencia/bloqueo pesimista).
  7. Reglas de cancelación (no se puede cancelar si está `FINALIZADA`).
  - **Archivos**: `service/ReservaService.java`, `service/impl/ReservaServiceImpl.java`.
  - **Dependencias**: T-007, T-008.

## Fase 5: Capa de Controladores REST

- [ ] **T-010**: Implementar `UsuarioController` con endpoints `/usuarios`, `/usuarios/{id}`, `/usuarios/{id}/estado`.
  - **Archivos**: `controller/UsuarioController.java`.
  - **Dependencias**: T-007.

- [ ] **T-011**: Implementar `RecursoController` con endpoints `/recursos`, `/recursos/{id}`, `/recursos/{id}/estado`, `/recursos/{id}/disponibilidad`.
  - **Archivos**: `controller/RecursoController.java`.
  - **Dependencias**: T-008.

- [ ] **T-012**: Implementar `ReservaController` con endpoints `/reservas`, `/reservas/{id}`, `/reservas/{id}` (DELETE/cancelar), `/reservas/usuario/{usuarioId}`, `/reservas/recurso/{recursoId}`.
  - **Archivos**: `controller/ReservaController.java`.
  - **Dependencias**: T-009.

## Fase 6: Pruebas y Calidad de Software

- [ ] **T-013**: Crear suite de pruebas unitarias con JUnit 5 y Mockito para `ReservaService` probando cada regla de negocio y caso borde.
  - **Archivos**: `src/test/java/.../service/ReservaServiceTest.java`.
  - **Dependencias**: T-009.

- [ ] **T-014**: Crear pruebas de integración para los controladores REST y el manejador de excepciones.
  - **Archivos**: `src/test/java/.../controller/*ControllerTest.java`.
  - **Dependencias**: T-012.

- [ ] **T-015**: Configurar plugin PIT Mutation Testing y verificar cobertura de mutación en la capa de servicios.
  - **Archivos**: `pom.xml` / `build.gradle`.
  - **Dependencias**: T-013.