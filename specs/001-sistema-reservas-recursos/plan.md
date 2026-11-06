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

## Phase 4: Entorno local reproducible

- **Compose**: `docker-compose.yml` levanta `db` (PostgreSQL 16) y `api` (Spring Boot) con un solo `docker compose up --build`. El servicio `frontend` es opcional y se activa con el perfil `frontend` (`docker compose --profile frontend up --build`), detrás de Nginx en `http://localhost:5173`.
- **Imagen de la API**: `Dockerfile` multi-stage. La etapa de build usa JDK 21 y el wrapper de Gradle para generar el `bootJar`; la etapa final usa JRE 21 y ejecuta el jar con un usuario sin privilegios.
- **Imagen del frontend**: `frontend/Dockerfile` multi-stage. Compila con Node 24 y `npm ci`, y sirve el `dist` con Nginx; `frontend/nginx.conf` resuelve el *fallback* de la SPA y reenvía `/api` a `api:8080`.
- **Datos semilla**: `src/main/resources/db/seed/R__local_seed.sql`, migración repeatable e idempotente. Solo se carga desde `application-local.yml`, que agrega `classpath:db/seed` a `spring.flyway.locations`; producción no incluye esa ubicación.
- **Credenciales**: `DB_NAME`, `DB_USERNAME` y `DB_PASSWORD` se leen de variables de entorno. `.env.example` documenta los valores de desarrollo y `.env` queda fuera del repositorio.
- **Verificación**: `docker compose config` y `docker compose --profile frontend config` validan la composición; con Docker corriendo, `docker compose up --build` debe dejar la API en `http://localhost:8080` y los usuarios y recursos semilla consultables, y el perfil `frontend` debe servir la SPA en `http://localhost:5173`.

## Phase 5: Paginación y filtros en listados

- **Endpoints**: `GET /usuarios`, `GET /recursos` y un nuevo `GET /reservas`. Los endpoints `/reservas/usuario/{id}`, `/reservas/recurso/{id}` y `/reservas/rango` mantienen su contrato sin paginar.
- **Parámetros**: `page` (base 0), `size` (20 por defecto, 100 máximo) y `sort` con el formato de Spring Data (`campo,asc|desc`), resueltos por el `PageableHandlerMethodArgumentResolver`.
- **Filtros**:
  - Usuarios: `estado`.
  - Recursos: `estado` y `tipo`; el tipo se compara sin distinguir mayúsculas y se ignora si viene en blanco.
  - Reservas: `estado`, `usuarioId` y `recursoId`; los tres son opcionales y se combinan con AND.
- **Respuesta**: `Page<T>` de Spring Data con `content`, `totalElements`, `totalPages`, `number`, `size` y los metadatos de ordenamiento.
- **Consulta de reservas**: `ReservaRepository.buscar(estado, usuarioId, recursoId, pageable)` con filtros nulos opcionales, para no duplicar consultas por cada combinación.
- **Compatibilidad**: los listados dejan de devolver un arreglo plano; el cambio se documenta en esta historia y el contrato OpenAPI se crea en HU-11.

## Phase 6: Integración continua

- **Plataforma**: GitHub Actions, porque el repositorio vive en GitHub. El workflow está en `.github/workflows/ci.yml`.
- **Disparadores**: cada `push` y cada `pull_request`; no hay despliegue ni publicación de artefactos de la aplicación.
- **Etapas**: compilación, pruebas unitarias e integración, mutation testing con PIT, verificación de cobertura con JaCoCo, pruebas de aceptación y pruebas E2E. Cada etapa es un paso nombrado del workflow; si una falla, el pipeline termina en rojo y el paso señala la etapa.
- **Pruebas por etapa**: `test` ejecuta la suite unitaria y de integración sobre el perfil `test` (H2). `acceptanceTest` y `e2eTest` son tareas de Gradle separadas que seleccionan las pruebas con los tags `acceptance` y `e2e`; la suite común las excluye para no duplicarlas.
- **Suite de aceptación**: Cucumber-JVM sobre JUnit Platform. Las features Gherkin de `src/test/resources/features/` ejecutan los escenarios de FR-001 a FR-016 contra la API con MockMvc; cada escenario lleva el tag del requisito que verifica. Los escenarios de comportamiento aún no implementado se escriben igual y quedan omitidos (`TestAbortedException`) sin bloquear al resto.
- **Alcance de aceptación y E2E**: la suite de Gradle ejercita la API completa por HTTP. La prueba E2E de navegador vive en `frontend/e2e` con Playwright y la ejecuta el job `browser-e2e`, que levanta PostgreSQL, arranca la API y Vite; el stage de Gradle se mantiene para la verificación por HTTP.
- **Umbral**: PIT falla por debajo de 80 % de mutation score y JaCoCo por debajo de 90 % de líneas en los servicios, según `build.gradle`.
- **Reportes**: cada etapa publica sus reportes como artefactos de GitHub Actions: resultados de pruebas (`build/reports/tests/*`), cobertura (`build/reports/jacoco/test/html`) y mutación (`build/reports/pitest`). La publicación usa `if: always()` para que los reportes queden disponibles incluso cuando una etapa falla.

## Phase 7: Concurrencia y rendimiento

### Verificación de cero duplicados bajo concurrencia

- **Decisión**: una prueba de integración que envía peticiones HTTP reales y simultáneas contra un PostgreSQL real y verifica que solo una se confirma, sin reservas duplicadas ni solapadas.
- **Prueba**: `ReservaConcurrencyTest` levanta el servidor embebido en un puerto aleatorio y lanza N hilos que disparan `POST /reservas` a la vez con un `CountDownLatch`; cada hilo usa un usuario distinto y comparte el mismo recurso e intervalo, de modo que lo que los serializa es el bloqueo pesimista del recurso.
- **Perfil y tarea**: corre con el perfil `postgres` (`src/test/resources/application-postgres.yml`) y la tarea `./gradlew postgresTest`; la suite rápida `./gradlew test` la excluye con la etiqueta `postgres` para seguir sin un PostgreSQL externo.
- **Parámetros**: 10 hilos por escenario, intervalo de 1 a 2 horas a 2 días vista, HTTP real sobre el servidor embebido.
- **Resultado**: 10 hilos sobre el mismo recurso e intervalo producen 1 respuesta 201 y 9 respuestas 409 `RECURSO_NO_DISPONIBLE`, con una sola reserva activa en la base de datos; 6 hilos del mismo usuario por recursos distintos producen 3 respuestas 201 y 3 respuestas 400 `LIMITE_RESERVAS_ACTIVAS`, sin superar nunca 3 reservas activas.
- **Justificación**: los bloqueos pesimistas ya implementados en `ReservaServiceImpl` se verifican contra el motor que los ejecuta, no contra H2, que no reproduce su semántica con fidelidad.

## Phase 8: Interfaz web

- **Stack**: React + Vite + TypeScript, React Router para la navegación, TanStack Query para el estado del servidor y Tailwind CSS para los estilos. Se descartó Thymeleaf porque el plan recomendaba una SPA y el equipo lo confirmó.
- **Ubicación**: `frontend/`, una aplicación independiente de la API. En desarrollo y en las pruebas E2E, Vite proxya `/api` hacia `http://localhost:8080`; `VITE_API_BASE_URL` permite apuntar a otro backend sin recompilar la lógica de la interfaz.
- **Flujo de HU-19**: selector de usuario activo —sin autenticación en esta fase—, listado y búsqueda de recursos, detalle con disponibilidad y formulario de reserva con validación de fechas en el cliente.
- **Errores**: el cliente lee la estructura `{status, error, message}` de la API y traduce los códigos 400, 404 y 409 a mensajes claros. Los estados de carga, vacío y error son explícitos en cada pantalla.
- **Pruebas**: Vitest y Testing Library para la lógica y los componentes; Playwright en `frontend/e2e` para el flujo real contra el backend. El job `browser-e2e` del workflow levanta PostgreSQL, arranca la API y Vite, y publica el reporte de Playwright.
- **Convenciones**: identificadores en inglés, textos de interfaz en español y archivos en kebab-case, según la política de idioma de `docs/base-standards.md`. El detalle vive en `docs/frontend-standards.md`.
