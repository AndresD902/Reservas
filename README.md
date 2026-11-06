# Sistema de reservas

API REST para gestionar de forma centralizada la reserva de espacios físicos —laboratorios, auditorios, salas— y de recursos como equipos audiovisuales. Reemplaza los procesos manuales y evita los conflictos que estos producen: reservas duplicadas o solapadas, recursos asignados mientras están en mantenimiento y usuarios que superan el límite de reservas activas.

## Para quién

| Perfil | Qué hace con el sistema |
|---|---|
| Administrador del sistema | Registra usuarios y recursos y gestiona su estado |
| Usuario activo | Reserva un recurso disponible para un intervalo de fecha y hora, y cancela sus reservas |

El alcance funcional completo, con sus reglas de negocio, está en [`specs/001-sistema-reservas-recursos/spec.md`](specs/001-sistema-reservas-recursos/spec.md).

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

`.env` no se versiona. `.env.example` solo contiene valores de desarrollo. Las variables `DB_NAME`, `DB_USERNAME`, `DB_PASSWORD`, `POSTGRES_PORT`, `API_PORT` y `FRONTEND_PORT` se leen del entorno.

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

## Interfaz web

La SPA vive en `frontend/` y sigue [`docs/frontend-standards.md`](docs/frontend-standards.md). Se puede levantar con Docker Compose (`docker compose --profile frontend up --build`, en `http://localhost:5173`) o con Vite en modo desarrollo, que requiere Node 24 y la API en `http://localhost:8080` (por ejemplo, con `docker compose up --build`).

```bash
cd frontend
npm install
npm run dev
```

Abre `http://localhost:5173`. Vite proxya `/api` hacia la API; `VITE_API_PROXY_TARGET` permite cambiar ese destino.

```bash
npm run lint
npm run typecheck
npm test
npm run build
```

Las pruebas E2E usan Playwright, levantan la API y Vite automáticamente y necesitan PostgreSQL y un JDK 21 para el backend:

```bash
cd frontend
npx playwright install chromium
npm run test:e2e
```

Si la API ya está corriendo en `http://localhost:8080`, el script la reutiliza.

## Integración continua

Cada push y pull request ejecuta [`.github/workflows/ci.yml`](.github/workflows/ci.yml) en GitHub Actions. El workflow tiene tres jobs: `pipeline` corre, en orden, compilación, pruebas unitarias e integración, mutation testing con PIT, cobertura con JaCoCo, pruebas de aceptación y E2E por HTTP; `frontend` corre lint, typecheck, pruebas de componentes y build de la SPA; y `browser-e2e` ejecuta Playwright contra PostgreSQL real, la API y Vite. Si una etapa falla, el job termina en rojo y el paso indica cuál falló.

Las tareas locales equivalentes son:

```bash
./gradlew test
./gradlew acceptanceTest
./gradlew e2eTest
cd frontend && npm test && npm run test:e2e
```

Los reportes de cada etapa quedan como artefactos de la ejecución del workflow, incluidos el reporte HTML de Playwright y las trazas de las pruebas de navegador que fallan.

Antes de cambiar código o documentación, lee **[`docs/base-standards.md`](docs/base-standards.md)**: es el punto de entrada de las reglas del proyecto, para las personas del equipo y para los agentes de IA, y el índice del resto de los estándares.

## Qué hay adentro

| Ruta | Qué es |
|---|---|
| `src/` | Código de la API y migraciones de base de datos |
| `frontend/` | Interfaz web en React, Vite y TypeScript |
| `Dockerfile`, `docker-compose.yml` | Entorno local completo: API, PostgreSQL con datos semilla y frontend opcional (`--profile frontend`) |
| `.devcontainer/` | Entorno de desarrollo con JDK 21 y PostgreSQL 16 |
| `specs/` | Especificación, plan, investigación, modelo de datos y tareas de cada funcionalidad |
| `.specify/memory/constitution.md` | Principios de arquitectura y calidad que todo el código cumple |
| `checklists/` | Listas de verificación de requisitos |
| `backlog/backlog.md` | Marco del backlog —iniciativa, DoR, DoD, épicas, perfiles y tabla de seguimiento— y las historias completas |
| `docs/` | Estándares del proyecto. `base-standards.md` es el punto de entrada |
| `ai-specs/` | La capa agéntica y su fuente canónica: las *skills*, los *agents* y los *scripts* |
| `CLAUDE.md`, `AGENTS.md` | Symlinks a `docs/base-standards.md` |
| `.claude/`, `.opencode/` | Symlinks generados hacia `ai-specs/`, uno por agente activo. No se editan a mano: los mantiene la skill `syncing-agent-symlinks` |

El estado de cada historia vive en el tablero de Trello, registrado en la sección "Integraciones" de `docs/base-standards.md`; su contenido está también en `backlog/backlog.md`.

Para verificar que las referencias entre documentos y los symlinks están sanos:

```bash
bash ai-specs/scripts/check-references.sh
```
