# Sistema de reservas

API REST para gestionar de forma centralizada la reserva de espacios físicos —laboratorios, auditorios, salas— y de recursos como equipos audiovisuales. Reemplaza los procesos manuales y evita los conflictos que estos producen: reservas duplicadas o solapadas, recursos asignados mientras están en mantenimiento y usuarios que superan el límite de reservas activas.

## Para quién

| Perfil | Qué hace con el sistema |
|---|---|
| Administrador del sistema | Registra usuarios y recursos y gestiona su estado |
| Usuario activo | Reserva un recurso disponible para un intervalo de fecha y hora, y cancela sus reservas |

El alcance funcional completo, con sus reglas de negocio, está en [`specs/001-sistema-reservas-recursos/spec.md`](specs/001-sistema-reservas-recursos/spec.md).

## Empezar

Requisitos: un JDK en la versión que fija el *toolchain* de `build.gradle`, y PostgreSQL con una base de datos `sistema_reservas`.

```bash
# 1. credenciales locales: la copia en la raíz no se versiona
cp src/main/resources/application-local.example.yml application-local.yml
#    y reemplaza la contraseña de ejemplo por la de tu PostgreSQL

# 2. levantar la API: Flyway aplica las migraciones al arrancar
./gradlew bootRun

# 3. correr las pruebas
./gradlew test
```

Antes de cambiar código o documentación, lee **[`docs/base-standards.md`](docs/base-standards.md)**: es el punto de entrada de las reglas del proyecto, para las personas del equipo y para los agentes de IA, y el índice del resto de los estándares.

## Qué hay adentro

| Ruta | Qué es |
|---|---|
| `src/` | Código de la API y migraciones de base de datos |
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
