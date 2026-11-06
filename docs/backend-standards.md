---
description: "Estándares del backend del sistema de reservas: stack, estructura de paquetes, base de datos, migraciones, configuración local y pruebas, con un puntero a los artefactos de spec-kit que fijan los principios de arquitectura. Se consulta antes de tocar código bajo src/ o el esquema de la base de datos."
---
# Estándares de backend

## Propósito y alcance

Este documento gobierna la forma concreta del backend: con qué se construye, dónde va cada pieza y cómo se cambia el esquema. Es un documento delegado desde `docs/base-standards.md` y especializa `docs/engineering-standards.md`, que conserva lo transversal a todo el código —Test Driven Development, tipado, convenciones de nombres y estructura de pruebas—. Lo que dice ese documento aplica aquí sin repetirse.

## Fuentes de verdad

Los principios de arquitectura y el diseño de cada funcionalidad viven en los artefactos de Spec-Driven Development, no aquí. Este documento los referencia; no reproduce sus reglas.

| Artefacto | Gobierna |
|---|---|
| `.specify/memory/constitution.md` | Principios no negociables: separación de capas, invariantes de negocio, control de concurrencia, DTOs en la API, manejo centralizado de excepciones, formato de error y *mutation testing* |
| `specs/<nnn>-<funcionalidad>/` | Especificación, plan técnico, investigación, modelo de datos y tareas de cada funcionalidad |
| `build.gradle` | Versiones exactas del stack y dependencias |

Si este documento y la constitución se contradicen, manda la constitución y este documento se corrige.

## Stack

Las versiones no se copian aquí: las fija `build.gradle`, y una copia queda desactualizada en el primer *upgrade*.

| Pieza | Tecnología |
|---|---|
| Lenguaje | Java, con la versión fijada por el *toolchain* de Gradle |
| Build | Gradle, siempre a través del *wrapper* (`./gradlew`) |
| Framework | Spring Boot: Web MVC, Data JPA, Validation |
| Contrato OpenAPI | SpringDoc OpenAPI (Swagger UI en `/swagger-ui/index.html`, YAML en `docs/api-spec.yml`) |
| Base de datos | PostgreSQL |
| Migraciones | Flyway |
| Reducción de *boilerplate* | Lombok |
| Pruebas | JUnit Platform, con los *starters* de prueba de Spring Boot |
| Pruebas de aceptación | Cucumber-JVM sobre JUnit Platform, con features en Gherkin |
| Base de datos de pruebas | H2 en modo PostgreSQL, en memoria |

## Estructura de paquetes

Todo el código vive bajo `com.institucion.reservas`. La regla de dependencia entre capas la fija la constitución, en su principio 1; la tabla dice solo dónde va cada cosa.

| Paquete | Contenido |
|---|---|
| `controller` | Endpoints REST. Reciben y devuelven DTOs |
| `service` | Interfaces de los servicios de negocio |
| `service.impl` | Implementaciones: reglas de negocio y límites transaccionales |
| `repository` | Interfaces de Spring Data JPA |
| `entity` | Entidades JPA y enums de estado |
| `dto` | Objetos de request y response de la API |
| `mapper` | Conversión entre entidades y DTOs, escrita a mano como componentes de Spring |
| `exception` | Excepciones del dominio y el manejador global |

## Base de datos y migraciones

- **El esquema solo cambia con una migración de Flyway.** Hibernate corre con `ddl-auto: validate`: verifica el esquema contra las entidades, pero no lo modifica.
- Las migraciones viven en `src/main/resources/db/migration/` y siguen la convención de Flyway `V<n>__<descripcion>.sql`.
- **Una migración aplicada no se edita.** Cualquier corrección va en una migración nueva.
- Un cambio de esquema dispara la actualización del modelo de datos de la funcionalidad afectada, según el catálogo de `docs/documentation-standards.md`.

## Configuración local

- `application.yml` activa el perfil `local` y toma las credenciales de la base de datos de las variables de entorno `DB_USERNAME` y `DB_PASSWORD`.
- `src/main/resources/application-local.example.yml` es la plantilla versionada del perfil local. La copia con credenciales reales va en la raíz del repositorio como `application-local.yml`: Spring Boot la carga desde el directorio de trabajo, y `.gitignore` la excluye solo en esa ruta.
- `.devcontainer/` es el entorno de desarrollo recomendado: JDK 21 y PostgreSQL 16, sin instalar nada en la máquina. La API usa la base `sistema_reservas`; las pruebas usan el perfil `test` de `src/test/resources/application-test.yml`, que levanta H2 en memoria en modo PostgreSQL; no necesita la base de desarrollo ni un PostgreSQL externo. Las credenciales del contenedor son solo de desarrollo y no se reutilizan fuera de él.
- Para levantar la API dentro del contenedor hay que sobreescribir la URL, porque la base no está en `localhost`: `SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/sistema_reservas ./gradlew bootRun`.

## Entorno local con Docker Compose

- `docker-compose.yml` levanta PostgreSQL (`db`) y la API (`api`) con `docker compose up --build`. El `Dockerfile` es multi-stage: compila con JDK 21 y el wrapper de Gradle, y ejecuta el `bootJar` con JRE 21 y un usuario sin privilegios.
- Los datos semilla viven en `src/main/resources/db/seed/R__local_seed.sql`, una migración repeatable e idempotente. `application-local.yml` agrega `classpath:db/seed` a `spring.flyway.locations`; los perfiles que no son `local` no cargan esa ubicación.
- Las variables `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `POSTGRES_PORT` y `API_PORT` se leen del entorno. `.env.example` documenta valores de desarrollo y `.env` está ignorado por Git.
- El frontend no se define en el compose hasta que HU-19 elija el stack y exista el directorio de la interfaz. No se agrega un servicio vacío.

## Pruebas

La mecánica es la de `docs/engineering-standards.md`. Lo que se exige a la suite —aislamiento de la lógica de negocio, pruebas unitarias y de integración, *mutation testing* sobre los servicios— lo fija la constitución, en su principio 4 y en la sección "Estándares de Calidad". Las pruebas de integración corren con el perfil `test` sobre H2 en memoria; Flyway aplica las migraciones y no se comparte la base de datos de desarrollo. Los builders de datos y la limpieza entre pruebas viven en `src/test/java/.../support/`.

Las pruebas que necesitan un PostgreSQL real son una excepción señalada, no la regla: viven con la etiqueta `postgres`, corren solo con `./gradlew postgresTest` y el perfil `postgres` de `src/test/resources/application-postgres.yml` (que toma la URL de `TEST_DB_URL`). `./gradlew test` las excluye para seguir sin base externa. HU-17 usa este camino en `ReservaConcurrencyTest`, que verifica contra PostgreSQL los bloqueos pesimistas de `ReservaServiceImpl` porque H2 no reproduce su semántica.

- Las pruebas unitarias de la capa de servicio viven en `src/test/java/com/institucion/reservas/service/impl/`. Usan JUnit 5 y Mockito, mappers reales y mocks solo de repositorios.
- `build.gradle` configura JaCoCo con umbral de 90 % de líneas por clase sobre `com.institucion.reservas.service.impl.*` y PIT con `mutationThreshold = 80` y `coverageThreshold = 90` sobre las mismas clases. `check` depende de ambas verificaciones y de las tareas `acceptanceTest` y `e2eTest`, así que `./gradlew build` falla si alguna baja o si una prueba de aceptación o E2E falla.
- PIT solo corre las pruebas de `com.institucion.reservas.service.**` (`targetTests`), para acotar la mutación a la lógica de servicios y no ejecutar la suite de integración.
- Las pruebas de aceptación y E2E viven en `src/test/java/com/institucion/reservas/acceptance/` y `src/test/java/com/institucion/reservas/e2e/`, y se seleccionan por los tags `acceptance` y `e2e`. La suite `test` las excluye para no duplicarlas; se ejecutan con `./gradlew acceptanceTest` y `./gradlew e2eTest`. La prueba E2E actual ejercita el flujo por HTTP; la de navegador se agrega con HU-19.
- La suite de aceptación usa Cucumber-JVM sobre JUnit Platform. Las features en Gherkin viven en `src/test/resources/features/` y los step definitions en `src/test/java/com/institucion/reservas/acceptance/steps/`; el contexto de Spring se configura en `CucumberSpringConfiguration` y los colaboradores se registran solo ahí con `CucumberSupportConfiguration`. Cada escenario lleva el tag `@FR-0NN` del requisito que verifica, de modo que `./gradlew acceptanceTest -PcucumberTags=@FR-012` acota la ejecución a ese requisito.
- Un escenario de un comportamiento que todavía no existe se escribe igual y se aborta con `TestAbortedException`: aparece como omitido en el reporte y no como fallo, así que no bloquea al resto de la suite. El estado omitido actual es el de la transición a `MANTENIMIENTO` con reservas futuras (FR-005, pendiente de T-018).
- `.github/workflows/ci.yml` corre en cada push y pull request la compilación, `test`, PIT, JaCoCo, `acceptanceTest` y `e2eTest`, y publica los reportes como artefactos incluso cuando una etapa falla.
- El contrato OpenAPI se genera en runtime con SpringDoc (`/v3/api-docs`, Swagger UI en `/swagger-ui/index.html`). `docs/api-spec.yml` es el YAML exportado y versionado; se regenera tras cambiar un endpoint y `OpenApiContractTest` verifica los 19 endpoints, la paginación y la estructura de error común.
- Queda una línea sin cubrir en `ReservaServiceImpl.validarFechas` (`if (fechaFin.isBefore(ahora))`): es inalcanzable porque `fechaInicio >= ahora` y `fechaFin > fechaInicio` ya implican `fechaFin >= ahora`. Se dejó intacta durante HU-14; eliminarla es un `refactor` aparte con test de regresión.

## Desviaciones conocidas

Brechas entre el código actual y los estándares, detectadas al adoptar el harness. Cada una es trabajo pendiente, no una excepción aceptada: se resuelve con su propia historia en el backlog y se quita de esta tabla cuando se cierra.

| Desviación | Estándar que incumple |
|---|---|
| Los identificadores del código están en español (`RecursoController`, `fechaInicio`, `EstadoReserva`) | Política de idioma de `docs/base-standards.md`: código e identificadores en inglés |
| El directorio `specs/001-sistema-reservas-recursos/` está nombrado en español | Política de idioma de `docs/base-standards.md`: nombres de archivos y directorios en inglés |

