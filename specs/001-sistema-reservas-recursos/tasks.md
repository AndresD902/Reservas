<!-- Archivo: specs/001-sistema-reservas-recursos/tasks.md -->
# Tareas de Implementación (Tasks)

## Fase 1: Configuración Inicial y Migraciones

- [x] **T-001**: Crear estructura de proyecto Java 21 / Spring Boot con paquetes `controller`, `service`, `repository`, `entity`, `dto`, `exception`, `mapper`.
  - **Archivos**: `build.gradle`, estructura de paquetes.
  - **Dependencias**: Ninguna.

- [x] **T-002**: Configurar base de datos y script de migración Flyway inicial.
  - **Archivos**: `src/main/resources/application.yml`, `src/main/resources/db/migration/V1__init_schema.sql`.
  - **Dependencias**: T-001.

## Fase 2: Entidades y Repositorios

- [x] **T-003**: Implementar entidades JPA (`UsuarioEntity`, `RecursoEntity`, `ReservaEntity`).
  - **Archivos**: `entity/UsuarioEntity.java`, `entity/RecursoEntity.java`, `entity/ReservaEntity.java`.
  - **Dependencias**: T-002.

- [x] **T-004**: Crear interfaces Repositorio JPA con consultas de validación de reservas y bloqueo pesimista.
  - **Archivos**: `repository/UsuarioRepository.java`, `repository/RecursoRepository.java`, `repository/ReservaRepository.java`.
  - **Dependencias**: T-003.

## Fase 3: Manejo Global de Excepciones y DTOs

- [x] **T-005**: Definir DTOs de Request/Response y Mappers para Usuario, Recurso y Reserva.
  - **Archivos**: `dto/*`, `mapper/*`.
  - **Dependencias**: T-003.

- [x] **T-006**: Implementar `@RestControllerAdvice` y excepciones personalizadas (`ResourceNotFoundException`, `BusinessRuleException`, `ReservationConflictException`).
  - **Archivos**: `exception/GlobalExceptionHandler.java`, `exception/ErrorResponseDto.java`, `exception/*Exception.java`.
  - **Dependencias**: T-005.

## Fase 4: Capa de Servicios y Reglas de Negocio

- [x] **T-007**: Implementar `UsuarioService` con operaciones de creación, consulta y actualización de estado (`ACTIVO`/`INACTIVO`).
  - **Archivos**: `service/UsuarioService.java`, `service/impl/UsuarioServiceImpl.java`.
  - **Dependencias**: T-004, T-006.

- [x] **T-008**: Implementar `RecursoService` con gestión de recursos y actualización de estado (`DISPONIBLE`, `MANTENIMIENTO`, `FUERA_DE_SERVICIO`).
  - **Archivos**: `service/RecursoService.java`, `service/impl/RecursoServiceImpl.java`.
  - **Dependencias**: T-004, T-006.

- [x] **T-009**: Implementar `ReservaService` incorporando todas las reglas de negocio obligatorias:
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

- [x] **T-010**: Implementar `UsuarioController` con endpoints `/usuarios`, `/usuarios/{id}`, `/usuarios/{id}/estado`.
  - **Archivos**: `controller/UsuarioController.java`.
  - **Dependencias**: T-007.

- [x] **T-011**: Implementar `RecursoController` con endpoints `/recursos`, `/recursos/{id}`, `/recursos/{id}/estado`, `/recursos/{id}/disponibilidad`.
  - **Archivos**: `controller/RecursoController.java`.
  - **Dependencias**: T-008.

- [x] **T-012**: Implementar `ReservaController` con endpoints `/reservas`, `/reservas/{id}`, `/reservas/{id}` (DELETE/cancelar), `/reservas/usuario/{usuarioId}`, `/reservas/recurso/{recursoId}`.
  - **Archivos**: `controller/ReservaController.java`.
  - **Dependencias**: T-009.

- [x] **T-016**: Implementar la consulta de reservas por rango de fechas que exige FR-016: `GET /reservas/rango?fechaInicio=<ISO-8601>&fechaFin=<ISO-8601>`, con solapamiento semiabierto y validación de que el fin sea estrictamente posterior al inicio.
  - **Archivos**: `controller/ReservaController.java`, `service/ReservaService.java`, `service/impl/ReservaServiceImpl.java`, `repository/ReservaRepository.java`, `src/test/java/com/institucion/reservas/service/impl/ReservaServiceImplTest.java`, `src/test/java/com/institucion/reservas/controller/ReservaControllerTest.java`.
  - **Dependencias**: T-012.

## Fase 6: Pruebas y Calidad de Software

- [x] **T-013**: Crear suite de pruebas unitarias con JUnit 5 y Mockito para `ReservaService` probando cada regla de negocio y caso borde.
  - **Archivos**: `src/test/java/.../service/ReservaServiceTest.java`, `src/test/java/.../service/UsuarioServiceTest.java`, `src/test/java/.../service/RecursoServiceTest.java`.
  - **Dependencias**: T-009.

- [x] **T-014**: Crear pruebas de integración para los controladores REST y el manejador de excepciones.
  - **Archivos**: `src/test/java/.../controller/*ControllerTest.java`, `src/test/java/.../exception/GlobalExceptionHandlerTest.java`.
  - **Dependencias**: T-012.

- [x] **T-015**: Configurar plugin PIT Mutation Testing y verificar cobertura de mutación en la capa de servicios.
  - **Archivos**: `build.gradle`, `src/test/java/.../service/ReservaServiceTest.java`, `src/test/java/.../service/UsuarioServiceTest.java`, `src/test/java/.../service/RecursoServiceTest.java`.
  - **Dependencias**: T-013.

## Fase 7: Ciclo de vida de la reserva y mantenimiento

- [x] **T-017**: Implementar la finalización automática de reservas vencidas: proceso `@Scheduled` con frecuencia configurable (`reservas.finalizacion.frecuencia`, por defecto `PT5M`) que pasa a `FINALIZADA` las reservas `ACTIVA` con `fecha_fin` anterior al instante de ejecución.
  - **Archivos**: `ReservasApplication.java`, `service/ReservaService.java`, `service/impl/ReservaServiceImpl.java` (o un componente de finalización), `repository/ReservaRepository.java`, `src/main/resources/application.yml`.
  - **Dependencias**: T-009.

- [x] **T-018**: Bloquear el cambio de estado de un recurso a `MANTENIMIENTO` cuando tenga reservas `ACTIVA` con `fecha_fin` posterior al momento del cambio, con el error `RECURSO_CON_RESERVAS_FUTURAS` (409).
  - **Archivos**: `service/impl/RecursoServiceImpl.java`, `repository/ReservaRepository.java`, `exception/GlobalExceptionHandler.java`.
  - **Dependencias**: T-008.

- [ ] **T-019**: Agregar la migración Flyway del índice que soporta la finalización automática.
  - **Archivos**: `src/main/resources/db/migration/V2__add_index_reservas_estado_fin.sql`.
  - **Dependencias**: T-002.

## Fase 8: Entorno local

- [x] **T-020**: Levantar la base, la API y los datos semilla con `docker compose up --build`: `Dockerfile` multi-stage, `docker-compose.yml`, migración repeatable local y documentación de variables.
  - **Archivos**: `Dockerfile`, `.dockerignore`, `docker-compose.yml`, `.env.example`, `src/main/resources/application-local.yml`, `src/main/resources/db/seed/R__local_seed.sql`, `README.md`, `docs/backend-standards.md`.
  - **Dependencias**: T-002, T-017.

- [x] **T-021**: Agregar el frontend como servicio opcional de Compose (`--profile frontend`), con imagen multi-stage de Node 24 + Nginx y proxy de `/api` hacia la API.
  - **Archivos**: `docker-compose.yml`, `frontend/Dockerfile`, `frontend/nginx.conf`, `frontend/.dockerignore`, `.env.example`, `README.md`, `docs/frontend-standards.md`, `docs/backend-standards.md`.
  - **Dependencias**: T-020, HU-19.

## Fase 9: Paginación y filtros en listados

- [x] **T-022**: Paginar y filtrar los listados de usuarios, recursos y reservas con `page`, `size`, `sort`, 20 elementos por defecto y 100 como máximo; usuarios por `estado`, recursos por `estado` y `tipo`, y reservas por `estado`, `usuarioId` y `recursoId`.
  - **Archivos**: `controller/UsuarioController.java`, `controller/RecursoController.java`, `controller/ReservaController.java`, `service/UsuarioService.java`, `service/RecursoService.java`, `service/ReservaService.java`, `service/impl/UsuarioServiceImpl.java`, `service/impl/RecursoServiceImpl.java`, `service/impl/ReservaServiceImpl.java`, `repository/UsuarioRepository.java`, `repository/RecursoRepository.java`, `repository/ReservaRepository.java`, `src/main/resources/application.yml`, `src/test/java/com/institucion/reservas/service/impl/*ServiceImplTest.java`, `src/test/java/com/institucion/reservas/controller/PaginationControllerTest.java`.
  - **Dependencias**: T-010, T-011, T-012.

## Fase 10: Integración continua

- [x] **T-023**: Crear el pipeline de GitHub Actions con compilación, pruebas unitarias e integración, PIT con umbral, aceptación, E2E y publicación de reportes en cada push y pull request.
  - **Archivos**: `.github/workflows/ci.yml`, `build.gradle`, `src/test/java/com/institucion/reservas/acceptance/ReservationAcceptanceTest.java`, `src/test/java/com/institucion/reservas/e2e/ReservationE2ETest.java`, `specs/001-sistema-reservas-recursos/plan.md`, `README.md`.
  - **Dependencias**: T-013, T-015, T-022.

## Fase 11: Concurrencia y rendimiento

- [x] **T-024**: Verificar cero duplicados bajo concurrencia con una prueba de integración que lanza N hilos HTTP simultáneos contra PostgreSQL real y confirma que solo una solicitud se confirma, el resto recibe 409 `RECURSO_NO_DISPONIBLE`, y la base de datos conserva una sola reserva activa para el recurso e intervalo; incluye el escenario de límite de 3 reservas activas por usuario.
  - **Archivos**: `src/test/java/com/institucion/reservas/concurrency/ReservaConcurrencyTest.java`, `src/test/resources/application-postgres.yml`, `build.gradle`.
  - **Dependencias**: T-009, T-014.

## Fase 12: Interfaz web

- [x] **T-025**: Crear la aplicación `frontend/` con React, Vite, TypeScript, React Router, TanStack Query y Tailwind CSS, y el selector de usuario activo.
  - **Archivos**: `frontend/package.json`, `frontend/vite.config.ts`, `frontend/src/main.tsx`, `frontend/src/App.tsx`, `frontend/src/components/user-selector.tsx`, `frontend/src/context/user-selection-context.ts`, `frontend/src/context/user-selection-provider.tsx`, `frontend/src/hooks/use-user-selection.ts`, `frontend/src/api/*`, `frontend/src/hooks/use-usuarios.ts`, `docs/frontend-standards.md`.
  - **Dependencias**: T-022.

- [x] **T-026**: Implementar el listado y la búsqueda de recursos, el detalle y la consulta de disponibilidad, con estados de carga, vacío y error.
  - **Archivos**: `frontend/src/pages/reservation-page.tsx`, `frontend/src/pages/resource-detail-page.tsx`, `frontend/src/components/resource-list.tsx`, `frontend/src/components/availability-panel.tsx`, `frontend/src/hooks/use-recursos.ts`, `frontend/src/hooks/use-disponibilidad.ts`.
  - **Dependencias**: T-025.

- [x] **T-027**: Implementar el formulario de reserva con validación de fechas en el cliente y mensajes claros para los errores 400, 404 y 409 de la API.
  - **Archivos**: `frontend/src/components/reservation-form.tsx`, `frontend/src/utils/date-time.ts`, `frontend/src/utils/error-messages.ts`, `frontend/src/hooks/use-crear-reserva.ts`.
  - **Dependencias**: T-026.

- [x] **T-028**: Escribir la prueba E2E del flujo de reserva con Playwright contra el backend real, incluidos el conflicto de horario y la validación de fechas.
  - **Archivos**: `frontend/playwright.config.ts`, `frontend/e2e/reservation.spec.ts`, `frontend/scripts/start-backend.mjs`.
  - **Dependencias**: T-027.

- [x] **T-029**: Integrar la interfaz en el pipeline —lint, typecheck, pruebas y build— y agregar el job de E2E de navegador con PostgreSQL real.
  - **Archivos**: `.github/workflows/ci.yml`, `README.md`, `docs/frontend-standards.md`, `docs/backend-standards.md`, `.gitignore`, `.dockerignore`.
  - **Dependencias**: T-028.

## Fase 13: Escenarios de aceptación ejecutables

- [x] **T-030**: Convertir los escenarios de la especificación en pruebas de aceptación ejecutables con Cucumber-JVM sobre JUnit Platform, etiquetadas por FR para consultar la trazabilidad, con la tarea `acceptanceTest` separada de la suite rápida y los escenarios de comportamiento no implementado como omitidos.
  - **Archivos**: `build.gradle`, `src/test/java/com/institucion/reservas/acceptance/CucumberAcceptanceSuite.java`, `src/test/java/com/institucion/reservas/acceptance/steps/*`, `src/test/java/com/institucion/reservas/acceptance/support/*`, `src/test/resources/features/*.feature`, `docs/backend-standards.md`.
  - **Dependencias**: T-013, T-014, T-023.

## Fase 14: Administración web

- [x] **T-031**: Extender el cliente de la API y los tipos para listar, consultar, crear y cambiar el estado de usuarios, y listar, consultar, crear, editar y cambiar el estado de recursos desde el panel administrativo.
  - **Archivos**: `frontend/src/api/types.ts`, `frontend/src/api/users.ts`, `frontend/src/api/resources.ts`, `frontend/src/utils/error-messages.ts`, `frontend/src/hooks/use-admin-users.ts`, `frontend/src/hooks/use-admin-resources.ts`.
  - **Dependencias**: T-025.

- [x] **T-032**: Implementar la página `/admin/usuarios` con listado, búsqueda, detalle, alta y cambio de estado, con validación en el cliente y mensajes claros para datos duplicados o inválidos.
  - **Archivos**: `frontend/src/pages/admin-users-page.tsx`, `frontend/src/components/admin/user-form.tsx`, `frontend/src/components/admin/user-detail.tsx`.
  - **Dependencias**: T-031.

- [x] **T-033**: Implementar la página `/admin/recursos` con listado, búsqueda, detalle, alta, edición y cambio de estado, incluido el error de recurso con reservas futuras.
  - **Archivos**: `frontend/src/pages/admin-resources-page.tsx`, `frontend/src/components/admin/resource-form.tsx`, `frontend/src/components/admin/resource-detail.tsx`.
  - **Dependencias**: T-031.

- [x] **T-034**: Agregar navegación al panel administrativo y pruebas de componentes para los flujos de usuarios y recursos.
  - **Archivos**: `frontend/src/App.tsx`, `frontend/src/components/layout.tsx`, `frontend/src/pages/admin-users-page.test.tsx`, `frontend/src/pages/admin-resources-page.test.tsx`.
  - **Dependencias**: T-032, T-033.

- [x] **T-035**: Escribir las pruebas E2E del panel administrativo con Playwright contra el backend real: alta de usuario, documento duplicado, alta de recurso, edición, cambio de estado a `MANTENIMIENTO` y bloqueo del mantenimiento con reservas futuras.
  - **Archivos**: `frontend/e2e/admin.spec.ts`.
  - **Dependencias**: T-034.

- [x] **T-036**: Actualizar la documentación de frontend y el estado de la historia en el backlog.
  - **Archivos**: `docs/frontend-standards.md`, `backlog/backlog.md`.
  - **Dependencias**: T-035.
