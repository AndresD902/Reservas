# Sistema de reservas

API REST para gestionar de forma centralizada la reserva de espacios físicos —laboratorios, auditorios, salas— y de recursos como equipos audiovisuales. Reemplaza los procesos manuales y evita los conflictos que estos producen: reservas duplicadas o solapadas, recursos asignados mientras están en mantenimiento y usuarios que superan el límite de reservas activas.

## Para quién

| Perfil | Qué hace con el sistema |
|---|---|
| Administrador del sistema | Registra usuarios y recursos y gestiona su estado |
| Usuario activo | Reserva un recurso disponible para un intervalo de fecha y hora, y cancela sus reservas |

El alcance funcional completo, con sus reglas de negocio, está en [`specs/001-sistema-reservas-recursos/spec.md`](specs/001-sistema-reservas-recursos/spec.md).

## Arquitectura

El sistema se compone de una API REST, una SPA y PostgreSQL:

```text
Navegador o curl
      |
      +--> SPA React (Vite :5173 o Nginx :5173)
      |          |
      |          +-- /api --> API Spring Boot :8080
      |
      +--------------------> API Spring Boot :8080
                                   |
                                   +--> PostgreSQL :5432
```

En desarrollo, Vite reenvía `/api` a la API; en Docker Compose, Nginx hace el mismo reenvío dentro de la red. La SPA y `curl` consumen la misma API; ninguno accede directamente a PostgreSQL.

### Backend por capas

El flujo de una petición es unidireccional: `controller` → `service` → `repository` → base de datos. El controlador no inyecta repositorios: solo recibe y devuelve DTOs, y delega las reglas de negocio en el servicio.

| Capa | Responsabilidad | Dónde vive |
|---|---|---|
| `controller` | Expone los endpoints REST y valida la forma de la petición | `src/main/java/.../controller` |
| `service` y `service.impl` | Reglas de negocio, transacciones y coordinación entre repositorios | `src/main/java/.../service` |
| `repository` | Consultas Spring Data JPA y bloqueos | `src/main/java/.../repository` |
| `entity` | Entidades JPA y enums de estado | `src/main/java/.../entity` |
| `dto` y `mapper` | Contratos de entrada/salida y conversión desde entidades | `src/main/java/.../dto`, `.../mapper` |
| `exception` | Errores del dominio y respuesta uniforme `{status, error, message}` | `src/main/java/.../exception` |
| `config` | Seguridad, OpenAPI y siembra del administrador local | `src/main/java/.../config` |
| `db/migration` y `db/seed` | Esquema Flyway y datos semilla del perfil local | `src/main/resources/db` |

Flyway es la única vía para cambiar el esquema. Hibernate corre con `ddl-auto: validate` y no modifica tablas.

### Datos principales

| Tabla | Contenido |
|---|---|
| `usuarios` | Personas que reservan, con documento, correo y estado `ACTIVO`/`INACTIVO` |
| `recursos` | Espacios y equipos reservables, con tipo, capacidad y estado |
| `reservas` | Relación entre usuario y recurso con intervalo `[inicio, fin)` y estado `ACTIVA`, `CANCELADA` o `FINALIZADA` |
| `administradores` | Cuentas del panel administrativo, separadas de los usuarios que reservan |

### Interfaz web

La SPA está en `frontend/` y usa React, Vite, TypeScript, React Router, TanStack Query y Tailwind CSS. Las pantallas viven en `pages`, los componentes reutilizables en `components`, las llamadas a la API en `api` y los hooks de datos en `hooks`. `apiFetch` centraliza el JSON, las credenciales y el token CSRF.

| Ruta | Pantalla |
|---|---|
| `/reservar` | Selección de usuario, búsqueda de recursos y creación de reservas |
| `/mis-reservas` | Consulta y cancelación de las reservas del usuario |
| `/login` | Inicio de sesión del administrador |
| `/admin/usuarios` | Alta, consulta y cambio de estado de usuarios |
| `/admin/recursos` | Alta, edición y cambio de estado de recursos |

## Empezar

### Entorno completo con Docker Compose

Requisito: Docker con Compose v2.

```bash
cp .env.example .env   # opcional: ajusta puertos o credenciales locales
docker compose up --build
```

La API queda en `http://localhost:8080`. Flyway aplica `V1__init_schema.sql` y la migración repeatable `db/seed/R__local_seed.sql`, que carga usuarios y recursos de ejemplo solo con el perfil local. Para comprobar:

```bash
curl http://localhost:8080/usuarios
curl http://localhost:8080/recursos
```

La interfaz del contrato OpenAPI está en `http://localhost:8080/swagger-ui/index.html`. La raíz `/usuarios` y `/recursos` devuelven ahora `Page` (paginación de HU-12); las consultas de reservas por usuario, recurso y rango siguen devolviendo un listado plano.

La interfaz web es un servicio opcional que se activa con el perfil `frontend`:

```bash
docker compose --profile frontend up --build
```

Queda en `http://localhost:5173` (configurable con `FRONTEND_PORT`) y reenvía `/api` hacia la API dentro de la red de Compose. Sin el perfil, `docker compose up --build` levanta solo la base y la API, para quien trabaja únicamente con el backend.

`docker compose down` detiene los servicios; `docker compose down -v` elimina también el volumen de PostgreSQL y vuelve a sembrar los datos en el siguiente arranque. Con el perfil activo, usa `docker compose --profile frontend down`.

`.env` no se versiona. `.env.example` solo contiene valores de desarrollo. Las variables `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `POSTGRES_PORT`, `API_PORT`, `FRONTEND_PORT`, `ADMIN_NAME`, `ADMIN_EMAIL` y `ADMIN_PASSWORD` se leen del entorno. En el perfil `local` se crea el administrador de desarrollo con esos valores; las lecturas de usuarios, recursos y reservas siguen siendo públicas, pero las escrituras de usuarios y recursos exigen la sesión de administrador.

### Entorno con devcontainer (recomendado)

El repositorio incluye `.devcontainer/` con un JDK 21 y un PostgreSQL 16. Aísla las herramientas del proyecto de las de tu máquina y evita instalar Java o PostgreSQL a mano.

Requisito: Docker disponible en el host. En WSL 2 se habilita desde Docker Desktop, en *Settings* → *Resources* → *WSL Integration*, para la distribución que uses.

Con VS Code y la extensión *Dev Containers*, usa **Dev Containers: Reopen in Container**. Al crear el contenedor, `postCreateCommand` compila las clases de prueba. La API usa la base `sistema_reservas`; las pruebas levantan H2 en memoria y no tocan la base de desarrollo.

```bash
# pruebas de integración de usuarios (HU-03)
./gradlew test --tests "*UsuarioControllerTest"

# suite completa
./gradlew test
```

Las pruebas de integración levantan H2 en memoria y aplican las migraciones de Flyway; no necesitan `sistema_reservas_test` ni un PostgreSQL externo.

Para recrear la base desde cero, desde el host, elimina el volumen y vuelve a abrir el contenedor:

```bash
docker compose -p reservas_devcontainer -f .devcontainer/docker-compose.yml down -v
```

Dentro del contenedor la base no está en `localhost`, así que para levantar la API se sobreescribe la URL:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://db:5432/sistema_reservas ./gradlew bootRun
```

### Entorno local sin contenedor

Requisitos: un JDK en la versión que fija el *toolchain* de `build.gradle`, y PostgreSQL con la base `sistema_reservas` para levantar la API. Las pruebas usan H2 en memoria.

```bash
# 1. credenciales locales: la copia en la raíz no se versiona
cp src/main/resources/application-local.example.yml application-local.yml
#    y reemplaza la contraseña de ejemplo por la de tu PostgreSQL

# 2. levantar la API: Flyway aplica las migraciones al arrancar
./gradlew bootRun

# 3. correr las pruebas: usan H2 en memoria
./gradlew test
```

## Variables de entorno

`.env.example` documenta los valores de desarrollo. Copiarlo a `.env` es opcional para Compose; `.env` no se versiona. Los valores por defecto de la tabla son los que usa el proyecto si la variable no está definida.

| Variable | Se usa en | Valor por defecto | Para qué |
|---|---|---|---|
| `DB_NAME` | Compose | `sistema_reservas` | Nombre de la base de datos |
| `DB_USERNAME` | Compose y API | `postgres` | Usuario de PostgreSQL |
| `DB_PASSWORD` | Compose y API | `postgres` en Compose; obligatoria en local | Contraseña de PostgreSQL |
| `POSTGRES_PORT` | Compose | `5432` | Puerto del host para PostgreSQL |
| `API_PORT` | Compose | `8080` | Puerto del host para la API |
| `FRONTEND_PORT` | Compose | `5173` | Puerto del host para la SPA |
| `SPRING_DATASOURCE_URL` | API | `jdbc:postgresql://localhost:5432/sistema_reservas` | URL JDBC; Compose la cambia a `db:5432` |
| `SPRING_PROFILES_ACTIVE` | API | `local` | Perfil de Spring; `local` carga los datos semilla |
| `ADMIN_NAME` | API local | `Administrador local` | Nombre del administrador sembrado |
| `ADMIN_EMAIL` | API local | `admin@example.com` | Correo del administrador sembrado |
| `ADMIN_PASSWORD` | API local | `admin123` | Contraseña del administrador sembrado |
| `RESERVAS_FINALIZACION_FRECUENCIA` | API | `PT5M` | Frecuencia ISO-8601 del job que finaliza reservas vencidas |
| `VITE_API_BASE_URL` | SPA | `/api` | Prefijo de las llamadas del navegador |
| `VITE_API_PROXY_TARGET` | Vite en desarrollo | `http://localhost:8080` | Destino del proxy `/api` |
| `TEST_DB_URL` | `./gradlew postgresTest` | sin valor | URL de PostgreSQL para las pruebas de concurrencia |
| `E2E_ADMIN_EMAIL` | E2E de navegador | `admin@example.com` | Administrador con el que operan las pruebas |
| `E2E_ADMIN_PASSWORD` | E2E de navegador | `admin123` | Contraseña del administrador de pruebas |

Los valores `ADMIN_*` y `E2E_ADMIN_*` son solo de desarrollo. Si cambias las credenciales del administrador en `.env`, cambia también las de E2E o exporta las variables antes de correr las pruebas de navegador.

## Interfaz web

La SPA vive en `frontend/` y sigue [`docs/frontend-standards.md`](docs/frontend-standards.md). Se puede levantar con Docker Compose (`docker compose --profile frontend up --build`, en `http://localhost:5173`) o con Vite en modo desarrollo, que requiere Node 24 y la API en `http://localhost:8080` (por ejemplo, con `docker compose up --build`).

```bash
cd frontend
npm install
npm run dev
```

Abre `http://localhost:5173`. Vite proxya `/api` hacia la API; `VITE_API_PROXY_TARGET` permite cambiar ese destino. La reserva está en `/reservar`, mis reservas en `/mis-reservas` y el panel administrativo en `/admin` (redirige a `/admin/usuarios`), que exige iniciar sesión en `/login`.

```bash
npm run lint
npm run typecheck
npm test
npm run build
```

Las pruebas E2E de navegador usan Serenity + Playwright (Java), y los flujos Gherkin de login usan Cucumber + Playwright. Todas corren contra el stack de Docker Compose (`http://localhost:5173` y `http://localhost:8080`), con JDK 21:

```bash
docker compose --profile frontend up -d --build
./gradlew installPlaywrightBrowsers   # una sola vez por máquina
./gradlew serenityE2ETest
./gradlew e2eGherkinTest
```

El reporte narrado con capturas por paso de Serenity queda en `target/site/serenity/index.html`. Los flujos Gherkin de HU-31 y HU-32 graban un video por escenario en `build/e2e-videos/` y dejan el reporte HTML, JSON y JUnit en `build/reports/cucumber/`. Para ver el navegador en vivo en tu pantalla (útil para grabar):

```bash
./gradlew serenityE2ETest -Pplaywright.headless=false -Pplaywright.slowmo=800 -Pserenity.take.screenshots=FOR_FAILURES
./gradlew e2eGherkinTest -Pplaywright.headless=false -Pplaywright.slowmo=500
```

En WSL el navegador se ve desde Windows gracias a WSLg; en Windows nativo se ve directamente. En Linux puede hacer falta instalar las librerías del sistema de Chromium (`playwright install --with-deps chromium`).

## Autenticación del administrador

El panel `/admin` exige una sesión de administrador. En el perfil `local`, la API siembra un administrador de desarrollo al arrancar:

| Variable | Valor de desarrollo |
|---|---|
| `ADMIN_EMAIL` | `admin@example.com` |
| `ADMIN_PASSWORD` | `admin123` |
| `ADMIN_NAME` | `Administrador local` |

Esos valores son solo para desarrollo: cámbialos en `.env` o en el entorno. Las contraseñas se guardan con hash BCrypt.

Endpoints:

- `POST /auth/login` recibe `{"correo","password"}` y abre la sesión.
- `POST /auth/logout` invalida la sesión.
- `GET /auth/me` devuelve el administrador autenticado.
- `GET /auth/csrf` entrega el token CSRF y la cookie `XSRF-TOKEN`.

Las escrituras de usuarios y recursos requieren la sesión y la cabecera `X-XSRF-TOKEN`; el cliente web las gestiona automáticamente. Las lecturas y el flujo de reserva siguen abiertos.

## Uso de la API

La API escucha en `http://localhost:8080`. El contrato completo está en [`docs/api-spec.yml`](docs/api-spec.yml) y Swagger UI en `http://localhost:8080/swagger-ui/index.html`.

### Convenciones

- Los cuerpos y las respuestas son JSON.
- Las fechas y horas son ISO-8601 sin zona: `2030-01-15T09:00:00`.
- `GET /usuarios`, `GET /recursos` y `GET /reservas` devuelven una página de Spring Data: `content`, `totalElements`, `totalPages`, `number` y `size`. Aceptan `page` (base 0), `size` (máximo 100) y `sort=campo,asc|desc`.
- Las consultas por usuario, recurso, rango y disponibilidad devuelven arreglos o un objeto, sin paginar.
- Los errores siempre tienen la forma `{ "status": 404, "error": "USUARIO_NO_ENCONTRADO", "message": "..." }`.
- Las lecturas, `POST /reservas` y `DELETE /reservas/{id}` no exigen sesión. Las escrituras de usuarios y recursos exigen sesión de administrador; todas las escrituras exigen además el token CSRF.

### Autenticación y CSRF

`GET /auth/csrf` crea la cookie `XSRF-TOKEN` y devuelve el valor que debe viajar en la cabecera `X-XSRF-TOKEN`. `POST /auth/login` abre la sesión en una cookie `HttpOnly`; `GET /auth/me` la consulta y `POST /auth/logout` la cierra.

Ejecuta este bloque en la misma sesión de shell; las variables se usan en los ejemplos siguientes:

```bash
BASE_URL=http://localhost:8080
COOKIE_JAR=$(mktemp)
CSRF_TOKEN=$(curl -sS -c "$COOKIE_JAR" "$BASE_URL/auth/csrf" | sed -n 's/.*"token":"\([^"]*\)".*/\1/p')

curl -sS -b "$COOKIE_JAR" -c "$COOKIE_JAR" \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: $CSRF_TOKEN" \
  -d '{"correo":"admin@example.com","password":"admin123"}' \
  "$BASE_URL/auth/login"
```

`GET /auth/me`:

```bash
curl -sS -b "$COOKIE_JAR" "$BASE_URL/auth/me"
```

El cierre de sesión está al final de los ejemplos, para no invalidar la sesión antes de terminar. Si tu administrador local usa otras credenciales, cambia el cuerpo del login o exporta `ADMIN_EMAIL` y `ADMIN_PASSWORD` antes de levantar Compose.

### Usuarios

Crear un usuario (requiere sesión y CSRF):

```bash
STAMP=$(date +%s)
NUEVO_USUARIO=$(curl -sS -b "$COOKIE_JAR" \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: $CSRF_TOKEN" \
  -d "{\"nombre\":\"Usuario de ejemplo\",\"documento\":\"DOC$STAMP\",\"correo\":\"usuario-$STAMP@example.com\",\"estado\":\"ACTIVO\"}" \
  "$BASE_URL/usuarios")
printf '%s\n' "$NUEVO_USUARIO"
USER_ID=$(printf '%s' "$NUEVO_USUARIO" | sed -n 's/^{"id":\([0-9]*\).*/\1/p')
```

Listar con paginación y filtro:

```bash
curl -sS "$BASE_URL/usuarios?page=0&size=5&sort=nombre,asc"
curl -sS "$BASE_URL/usuarios?estado=ACTIVO&page=0&size=5"
```

Consultar un usuario:

```bash
curl -sS "$BASE_URL/usuarios/$USER_ID"
```

Cambiar el estado (también acepta `PUT`):

```bash
curl -sS -b "$COOKIE_JAR" -X PATCH \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: $CSRF_TOKEN" \
  -d '{"estado":"INACTIVO"}' \
  "$BASE_URL/usuarios/$USER_ID/estado"
```

### Recursos

Crear un recurso (requiere sesión y CSRF):

```bash
NUEVO_RECURSO=$(curl -sS -b "$COOKIE_JAR" \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: $CSRF_TOKEN" \
  -d "{\"nombre\":\"Sala de ejemplo $STAMP\",\"descripcion\":\"Sala para probar la API\",\"tipo\":\"SALA\",\"capacidad\":6,\"estado\":\"DISPONIBLE\"}" \
  "$BASE_URL/recursos")
printf '%s\n' "$NUEVO_RECURSO"
RESOURCE_ID=$(printf '%s' "$NUEVO_RECURSO" | sed -n 's/^{"id":\([0-9]*\).*/\1/p')
```

Listar con filtros:

```bash
curl -sS "$BASE_URL/recursos?page=0&size=5&sort=nombre,asc"
curl -sS "$BASE_URL/recursos?tipo=SALA&estado=DISPONIBLE&page=0&size=5"
```

Consultar un recurso:

```bash
curl -sS "$BASE_URL/recursos/$RESOURCE_ID"
```

Actualizar todos sus datos (requiere sesión y CSRF):

```bash
curl -sS -b "$COOKIE_JAR" -X PUT \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: $CSRF_TOKEN" \
  -d "{\"nombre\":\"Sala de ejemplo $STAMP (editada)\",\"descripcion\":\"Sala para probar la API\",\"tipo\":\"SALA\",\"capacidad\":8,\"estado\":\"DISPONIBLE\"}" \
  "$BASE_URL/recursos/$RESOURCE_ID"
```

Cambiar el estado a `MANTENIMIENTO` y volver a `DISPONIBLE`:

```bash
curl -sS -b "$COOKIE_JAR" -X PATCH \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: $CSRF_TOKEN" \
  -d '{"estado":"MANTENIMIENTO"}' \
  "$BASE_URL/recursos/$RESOURCE_ID/estado"

curl -sS -b "$COOKIE_JAR" -X PATCH \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: $CSRF_TOKEN" \
  -d '{"estado":"DISPONIBLE"}' \
  "$BASE_URL/recursos/$RESOURCE_ID/estado"
```

El cambio a `MANTENIMIENTO` devuelve `409 RECURSO_CON_RESERVAS_FUTURAS` si el recurso tiene reservas activas o futuras. En ese caso hay que cancelarlas primero.

Consultar disponibilidad:

```bash
curl -sS "$BASE_URL/recursos/$RESOURCE_ID/disponibilidad?fechaInicio=2030-01-15T09:00:00&fechaFin=2030-01-15T10:00:00"
```

### Reservas

Crear una reserva (sin sesión, pero con CSRF):

```bash
NUEVA_RESERVA=$(curl -sS -b "$COOKIE_JAR" \
  -H "Content-Type: application/json" \
  -H "X-XSRF-TOKEN: $CSRF_TOKEN" \
  -d "{\"usuarioId\":$USER_ID,\"recursoId\":$RESOURCE_ID,\"fechaInicio\":\"2030-01-15T09:00:00\",\"fechaFin\":\"2030-01-15T10:00:00\"}" \
  "$BASE_URL/reservas")
printf '%s\n' "$NUEVA_RESERVA"
RESERVA_ID=$(printf '%s' "$NUEVA_RESERVA" | sed -n 's/^{"id":\([0-9]*\).*/\1/p')
```

Si repites el `POST` anterior con el mismo recurso e intervalo, la API responde `409 RECURSO_NO_DISPONIBLE`.

Listar con filtros:

```bash
curl -sS "$BASE_URL/reservas?page=0&size=5&sort=fechaInicio,asc"
curl -sS "$BASE_URL/reservas?estado=ACTIVA&usuarioId=$USER_ID&recursoId=$RESOURCE_ID&page=0&size=5"
```

Consultar una reserva:

```bash
curl -sS "$BASE_URL/reservas/$RESERVA_ID"
```

Consultar por usuario y por recurso:

```bash
curl -sS "$BASE_URL/reservas/usuario/$USER_ID"
curl -sS "$BASE_URL/reservas/recurso/$RESOURCE_ID"
```

Consultar por rango, con solapamiento semiabierto:

```bash
curl -sS "$BASE_URL/reservas/rango?fechaInicio=2030-01-15T00:00:00&fechaFin=2030-01-16T00:00:00"
```

Comprobar que el recurso ya no está disponible en ese intervalo:

```bash
curl -sS "$BASE_URL/recursos/$RESOURCE_ID/disponibilidad?fechaInicio=2030-01-15T09:00:00&fechaFin=2030-01-15T10:00:00"
```

Cancelar la reserva (sin sesión, pero con CSRF):

```bash
curl -sS -b "$COOKIE_JAR" -X DELETE \
  -H "X-XSRF-TOKEN: $CSRF_TOKEN" \
  "$BASE_URL/reservas/$RESERVA_ID"
```

Cancelarla de nuevo devuelve `400 RESERVA_CANCELADA`.

### Errores frecuentes

| Código | `error` | Cuándo aparece |
|---|---|---|
| `400` | `VALIDACION`, `FECHAS_INVALIDAS`, `FECHA_PASADA`, `LIMITE_RESERVAS_ACTIVAS`, `RESERVA_CANCELADA`, `RESERVA_FINALIZADA` | Datos inválidos o una regla de negocio incumplida |
| `401` | `NO_AUTENTICADO` | Falta la sesión de administrador |
| `403` | `ACCESO_DENEGADO` | Falta el token CSRF o la sesión no tiene permisos |
| `404` | `USUARIO_NO_ENCONTRADO`, `RECURSO_NO_ENCONTRADO`, `RESERVA_NO_ENCONTRADA` | El identificador no existe |
| `409` | `RECURSO_NO_DISPONIBLE`, `RECURSO_CON_RESERVAS_FUTURAS` | Solapamiento o recurso bloqueado por reservas vigentes |
| `500` | `ERROR_INTERNO` | Fallo inesperado del servidor |

Al terminar los ejemplos, cierra la sesión y elimina el archivo de cookies:

```bash
curl -sS -o /dev/null -b "$COOKIE_JAR" -X POST \
  -H "X-XSRF-TOKEN: $CSRF_TOKEN" \
  "$BASE_URL/auth/logout"
rm -f "$COOKIE_JAR"
```

## Integración continua

Cada push y pull request ejecuta [`.github/workflows/ci.yml`](.github/workflows/ci.yml) en GitHub Actions. El workflow tiene tres jobs: `pipeline` corre, en orden, compilación, pruebas unitarias e integración, mutation testing con PIT, cobertura con JaCoCo, pruebas de aceptación y E2E por HTTP; `frontend` corre lint, typecheck, pruebas de componentes y build de la SPA; y `serenity-e2e` levanta el stack con Docker Compose y ejecuta las pruebas E2E de navegador con Serenity y los flujos Gherkin de `e2eGherkinTest` en modo headless. Si una etapa falla, el job termina en rojo y el paso indica cuál falló.

Las tareas locales equivalentes son:

```bash
./gradlew test
./gradlew acceptanceTest
./gradlew e2eTest
./gradlew serenityE2ETest
./gradlew e2eGherkinTest
cd frontend && npm test
```

Los reportes de cada etapa quedan como artefactos de la ejecución del workflow, incluidos el reporte HTML de Serenity (`target/site/serenity`), el reporte de Cucumber (`build/reports/cucumber`) y los videos de los flujos Gherkin (`build/e2e-videos`).

El workflow [`zap-baseline.yml`](.github/workflows/zap-baseline.yml) ejecuta además el análisis DAST con OWASP ZAP sobre la API y la interfaz, y publica los reportes HTML y JSON como el artefacto `reporte-seguridad-zap`. El procedimiento reproducible vive en [`security/zap`](security/zap/README.md).

## Comandos de referencia

| Objetivo | Comando |
|---|---|
| Levantar base y API | `docker compose up --build` |
| Levantar base, API y SPA | `docker compose --profile frontend up --build` |
| Ver logs de la API | `docker compose logs -f api` |
| Detener los servicios | `docker compose down` |
| Borrar la base y volver a sembrar | `docker compose down -v` |
| Levantar la API sin contenedor | `./gradlew bootRun` |
| Compilar y ejecutar todas las verificaciones | `./gradlew build` |
| Suite rápida de backend (H2) | `./gradlew test` |
| Pruebas de aceptación | `./gradlew acceptanceTest` |
| Pruebas E2E por HTTP | `./gradlew e2eTest` |
| Pruebas contra PostgreSQL real | `TEST_DB_URL=jdbc:postgresql://localhost:5432/sistema_reservas_test ./gradlew postgresTest` |
| Mutation testing | `./gradlew pitest` |
| Cobertura JaCoCo | `./gradlew jacocoTestReport jacocoTestCoverageVerification` |
| Instalar dependencias de la SPA | `cd frontend && npm install` |
| Levantar Vite en desarrollo | `cd frontend && npm run dev` |
| Lint, tipos, pruebas y build de la SPA | `cd frontend && npm run lint && npm run typecheck && npm test && npm run build` |
| Instalar Chromium para Playwright | `./gradlew installPlaywrightBrowsers` |
| Pruebas E2E de navegador con Serenity | `./gradlew serenityE2ETest` |
| Flujos Gherkin E2E de login | `./gradlew e2eGherkinTest` |
| Verificar referencias y symlinks | `bash ai-specs/scripts/check-references.sh` |

`./gradlew build` ejecuta `check`, que a su vez corre PIT, JaCoCo, aceptación y E2E por HTTP; por eso tarda más que `./gradlew test`. Las pruebas E2E de navegador (`serenityE2ETest` y `e2eGherkinTest`) se ejecutan aparte porque exigen el stack de Docker Compose levantado. Para el ciclo de desarrollo usa las tareas acotadas y reserva `build` para antes de cerrar el cambio.

## Solución de problemas

| Síntoma | Causa probable | Qué hacer |
|---|---|---|
| `Connection refused` en `http://localhost:8080` | La API todavía está arrancando o el puerto está ocupado | Revisa `docker compose ps` y `docker compose logs api`; cambia `API_PORT` si hace falta |
| `password authentication failed for user` | La contraseña de `.env` no coincide con el volumen existente | Ajusta `DB_PASSWORD` o recrea la base con `docker compose down -v` |
| `401 NO_AUTENTICADO` | Falta la sesión de administrador | Ejecuta el login y conserva el mismo `COOKIE_JAR` |
| `403 ACCESO_DENEGADO` | Falta el token CSRF o la sesión no tiene permisos | Vuelve a pedir `GET /auth/csrf` y envía `X-XSRF-TOKEN` |
| `409 RECURSO_NO_DISPONIBLE` | El intervalo se solapa con otra reserva o el recurso no está disponible | Consulta la disponibilidad y elige otro intervalo |
| `409 RECURSO_CON_RESERVAS_FUTURAS` | Intentas poner en mantenimiento un recurso con reservas vigentes | Cancela primero las reservas activas o futuras |
| `check-references.sh` falla con `\r` | El checkout de Windows convirtió el script a CRLF | Ejecútalo desde WSL o Git Bash, o normaliza el archivo a LF |
| Playwright no encuentra Chromium | Falta el navegador de pruebas | Ejecuta `./gradlew installPlaywrightBrowsers` (en Linux puede hacer falta `playwright install --with-deps chromium`) |

Antes de cambiar código o documentación, lee **[`docs/base-standards.md`](docs/base-standards.md)**: es el punto de entrada de las reglas del proyecto, para las personas del equipo y para los agentes de IA, y el índice del resto de los estándares.

## Qué hay adentro

| Ruta | Qué es |
|---|---|
| `src/` | Código de la API y migraciones de base de datos |
| `frontend/` | Interfaz web en React, Vite y TypeScript |
| `security/` | Escaneo DAST con OWASP ZAP: script reproducible, reglas y reportes de seguridad |
| `Dockerfile`, `docker-compose.yml` | Entorno local completo: API, PostgreSQL con datos semilla y frontend opcional (`--profile frontend`) |
| `.env.example` | Valores de desarrollo para las variables de Compose, la API y la SPA |
| `.devcontainer/` | Entorno de desarrollo con JDK 21 y PostgreSQL 16 |
| `specs/` | Especificación, plan, investigación, modelo de datos y tareas de cada funcionalidad |
| `.specify/memory/constitution.md` | Principios de arquitectura y calidad que todo el código cumple |
| `checklists/` | Listas de verificación de requisitos |
| `backlog/backlog.md` | Marco del backlog —iniciativa, DoR, DoD, épicas, perfiles y tabla de seguimiento— y las historias completas |
| `docs/` | Estándares del proyecto. `base-standards.md` es el punto de entrada |
| `docs/api-spec.yml` | Contrato OpenAPI versionado de la API |
| `ai-specs/` | La capa agéntica y su fuente canónica: las *skills*, los *agents* y los *scripts* |
| `CLAUDE.md`, `AGENTS.md` | Symlinks a `docs/base-standards.md` |
| `.claude/`, `.opencode/` | Symlinks generados hacia `ai-specs/`, uno por agente activo. No se editan a mano: los mantiene la skill `syncing-agent-symlinks` |

El estado de cada historia vive en el tablero de Trello, registrado en la sección "Integraciones" de `docs/base-standards.md`; su contenido está también en `backlog/backlog.md`.

Para verificar que las referencias entre documentos y los symlinks están sanos:

```bash
bash ai-specs/scripts/check-references.sh
```
