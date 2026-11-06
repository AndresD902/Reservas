# Sistema de reservas

API REST para gestionar de forma centralizada la reserva de espacios físicos —laboratorios, auditorios, salas— y de recursos como equipos audiovisuales. Reemplaza los procesos manuales y evita los conflictos que estos producen: reservas duplicadas o solapadas, recursos asignados mientras están en mantenimiento y usuarios que superan el límite de reservas activas.

## Para quién

| Perfil | Qué hace con el sistema |
|---|---|
| Administrador del sistema | Registra usuarios y recursos y gestiona su estado |
| Usuario activo | Reserva un recurso disponible para un intervalo de fecha y hora, y cancela sus reservas |

El alcance funcional completo, con sus reglas de negocio, está en [`specs/001-sistema-reservas-recursos/spec.md`](specs/001-sistema-reservas-recursos/spec.md).

## Empezar

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

Antes de cambiar código o documentación, lee **[`docs/base-standards.md`](docs/base-standards.md)**: es el punto de entrada de las reglas del proyecto, para las personas del equipo y para los agentes de IA, y el índice del resto de los estándares.

## Qué hay adentro

| Ruta | Qué es |
|---|---|
| `src/` | Código de la API y migraciones de base de datos |
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
