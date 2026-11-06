# Backlog del sistema de reservas

## Cómo interpretar este documento

Este archivo es el marco del backlog: la iniciativa, la Definición de listo, la Definición de terminado, las épicas, los perfiles de usuario y la tabla de seguimiento. Después del marco van las historias completas.

| Qué | Dónde vive |
|---|---|
| El contenido de cada historia: Card, Conversación, Tareas y Criterios de Aceptación | Este archivo y la card, con el mismo texto |
| El estado de una historia y el avance de sus checklists | Solo el tablero. Las checklists de este archivo no llevan estado para no duplicarlo |
| La forma de una card: título, 3 Ces, checklists, labels y límites | `docs/trello-backlog-contract.md` |
| Qué tablero es | `docs/base-standards.md`, sección "Integraciones" |
| Dónde vive este archivo y cuándo se actualiza | `docs/documentation-standards.md`, sección "El backlog" |

La columna "Estado en el tablero" de la tabla de seguimiento es una foto de la fecha de la última actualización de este archivo. Si difiere del tablero, manda el tablero.

## La iniciativa

**Problema de hoy.** La API de reservas de espacios y recursos está implementada en su núcleo —usuarios, recursos, creación, cancelación y consultas—, pero sin verificación: la única prueba es la de carga del contexto, `spec.md` conserva 3 dudas abiertas, falta la consulta por fecha de FR-016 y ninguna reserva pasa a `FINALIZADA`. Una reserva vencida sigue contando para el límite de 3 activas, así que un usuario con 3 reservas pasadas no puede volver a reservar.

**Qué entrega.** Un sistema que cumple FR-001 a FR-016 con evidencia: pruebas unitarias, de integración, de aceptación, de mutación y de concurrencia; una interfaz web para usuarios y administradores; y un entorno reproducible con integración continua.

**Fuente.** La especificación en `specs/001-sistema-reservas-recursos/` y el plan de trabajo de la sesión del 2026-11-05, ordenado en fases de la 0 a la 8.

**Fuera de alcance:**

- Autenticación y autorización: `spec.md` la excluye de esta fase.
- El despliegue a un entorno productivo: el plan de trabajo llega hasta el entorno local y la integración continua.

## Definición de listo (DoR)

Una historia entra a un sprint cuando cumple todo esto:

- La Card nombra un rol, una capacidad y un beneficio verificables.
- Sus Criterios de Aceptación están en Gherkin y describen comportamiento observable.
- No tiene indeterminados abiertos: cada "Indeterminado (bloquea el DoR)" de su Conversación está resuelto y registrado.
- No la afecta ninguna duda abierta (`NEEDS CLARIFICATION`) de `spec.md`.
- Su card cumple `docs/trello-backlog-contract.md`, incluida la etiqueta de épica.

## Definición de terminado (DoD)

Una historia se da por terminada cuando cumple todo esto:

- El código se escribió con Test Driven Development y sus pruebas pasan, según `docs/engineering-standards.md`.
- Cada Criterio de Aceptación está verificado por una prueba o una revisión registrada, y marcado en la card.
- Respeta la constitución en `.specify/memory/constitution.md`: capas, DTOs en la API, manejador global de errores y mutation testing en los servicios.
- Los artefactos de spec-kit afectados se actualizaron en el mismo commit, según `docs/documentation-standards.md`, sección "Los artefactos de Spec-Driven Development".
- `bash ai-specs/scripts/check-references.sh` termina en OK y la skill `updating-docs` corrió antes del commit.

## Épicas

Las etiquetas existen en el tablero y cada card lleva la suya. Se crearon y asignaron a mano, porque el MCP de Trello no puede crear etiquetas: una épica nueva se agrega igual, con el formato de `docs/trello-backlog-contract.md`.

| Etiqueta | Alcance |
|---|---|
| `EP-01: Fundaciones y especificación` | Especificación con spec-kit, estándares del proyecto, capa agéntica y resolución de dudas abiertas |
| `EP-02: Gestión de usuarios y recursos` | FR-001 a FR-006 |
| `EP-03: Ciclo de vida de reservas` | FR-007 a FR-016, finalización automática, contrato OpenAPI, paginación y filtros |
| `EP-04: Calidad y pruebas automatizadas` | Infraestructura de pruebas, pruebas unitarias, de integración, de aceptación y de mutación |
| `EP-05: Concurrencia y rendimiento` | Cero duplicados bajo concurrencia y carga de 500 usuarios |
| `EP-06: Interfaz web` | Reserva, consulta y cancelación para el usuario, y administración |
| `EP-07: Operación y entrega continua` | Entorno local, integración continua y README operativo |

## Perfiles de usuario

| Perfil | Quién es | Qué necesita del sistema |
|---|---|---|
| Administrador del sistema | Gestiona el inventario de recursos y quién puede reservar. Sale de `spec.md` | Registrar usuarios y recursos y controlar su estado |
| Usuario activo | Persona habilitada para reservar. Sale de `spec.md` | Reservar, consultar y cancelar sin conflictos ni dobles reservas |
| Equipo de desarrollo | Quien construye y verifica el sistema. Supuesto: no está en `spec.md`, se agregó para las historias técnicas | Evidencia automatizada de que el sistema cumple, y un entorno reproducible |
| Desarrollador que consume la API | Quien integra un cliente, como la interfaz web | Un contrato publicado de endpoints y errores |
| Persona nueva en el equipo | Quien llega al repositorio por primera vez | Operar el sistema sin preguntar |

## Tabla de seguimiento

El orden es una sugerencia para planificar, no una dependencia entre historias: cada una se sostiene sola. El 1 agrupa lo que conviene resolver primero; "—" marca las historias ya implementadas.

| Historia | Épica | Estado en el tablero | Orden sugerido |
|---|---|---|---|
| HU-01 · Especificación del sistema de reservas con spec-kit | EP-01 | Completado | — |
| HU-02 · Estándares de trabajo y capa agéntica del proyecto | EP-01 | Completado | — |
| HU-03 · Registro y gestión de estado de usuarios | EP-02 | En Revisión | — |
| HU-04 · Registro y gestión de estado de recursos | EP-02 | En Revisión | — |
| HU-05 · Creación de reservas con reglas de negocio y control de concurrencia | EP-03 | En Revisión | — |
| HU-06 · Cancelación de reservas | EP-03 | En Revisión | — |
| HU-07 · Consulta de reservas por usuario y recurso, y disponibilidad | EP-03 | En Revisión | — |
| HU-08 · Resolución de las dudas abiertas de la especificación | EP-01 | Completado | 1 |
| HU-09 · Consulta de reservas por rango de fechas | EP-03 | En Progreso | 3 |
| HU-10 · Finalización automática de reservas vencidas | EP-03 | Backlog | 2 |
| HU-11 · Contrato OpenAPI de la API | EP-03 | Backlog | 3 |
| HU-12 · Paginación y filtros en listados | EP-03 | Backlog | 3 |
| HU-13 · Suite de pruebas ejecutable sin base de datos externa | EP-04 | Backlog | 1 |
| HU-14 · Reglas de negocio verificadas con pruebas unitarias y de mutación | EP-04 | Backlog | 2 |
| HU-15 · Endpoints y contrato de errores verificados con pruebas de integración | EP-04 | Backlog | 2 |
| HU-16 · Escenarios de la especificación ejecutables como pruebas de aceptación | EP-04 | Backlog | 2 |
| HU-17 · Cero reservas duplicadas bajo concurrencia | EP-05 | Backlog | 2 |
| HU-18 · Prueba de carga con 500 usuarios simultáneos | EP-05 | Backlog | 3 |
| HU-19 · Reservar un recurso desde la interfaz web | EP-06 | Backlog | 3 |
| HU-20 · Consultar y cancelar mis reservas desde la interfaz web | EP-06 | Backlog | 3 |
| HU-21 · Administrar usuarios y recursos desde la interfaz web | EP-06 | Backlog | 3 |
| HU-22 · Entorno local con un solo comando | EP-07 | Backlog | 3 |
| HU-23 · Pipeline de integración continua | EP-07 | Backlog | 3 |
| HU-24 · README operativo con arquitectura, variables y ejemplos curl | EP-07 | Backlog | 3 |

## Historias

### HU-01 · Especificación del sistema de reservas con spec-kit

Épica: `EP-01: Fundaciones y especificación`

**Card**

> Como equipo de desarrollo, quiero la especificación funcional y técnica del sistema documentada con spec-kit, para implementar contra requisitos, reglas y decisiones explícitas.

**Conversación**

- Artefactos en `specs/001-sistema-reservas-recursos/`: `spec.md`, `plan.md`, `research.md`, `data-model.md` y `tasks.md`. Principios en `.specify/memory/constitution.md`; verificación de requisitos en `checklists/requirements.md`.
- Decisiones registradas en `research.md`: bloqueo pesimista ante 500 usuarios simultáneos, Flyway para el esquema, errores homogéneos con `@RestControllerAdvice` e intervalos semiabiertos `[inicio, fin)` para el solapamiento.
- `spec.md` conserva 3 dudas abiertas (`NEEDS CLARIFICATION`). Resolverlas queda fuera de esta historia.
- El 2026-11-05 se alinearon `plan.md`, `tasks.md` y `data-model.md` con el código: Spring Boot 4.x, Java 21, T-001 a T-012 marcadas y T-016 agregada para la consulta por fecha de FR-016.

**Tareas**

- Redactar la especificación funcional con FR-001 a FR-016, escenarios, criterios de éxito y casos borde
- Definir la constitución del proyecto
- Documentar el plan técnico, la investigación y el modelo de datos
- Descomponer la implementación en tareas
- Alinear plan, tareas y modelo de datos con el código implementado

**Criterios de Aceptación**

- Dado el repositorio, cuando se abre `specs/001-sistema-reservas-recursos/`, entonces existen `spec.md`, `plan.md`, `research.md`, `data-model.md` y `tasks.md`.
- Dado `data-model.md`, cuando se compara con `V1__init_schema.sql`, entonces tablas, campos, restricciones e índices coinciden.
- Dado `plan.md`, cuando se compara con `build.gradle`, entonces el lenguaje, el framework, la base de datos y el mapeo declarados coinciden.
- Dado `tasks.md`, cuando se revisa el código, entonces cada tarea marcada como hecha está implementada.

### HU-02 · Estándares de trabajo y capa agéntica del proyecto

Épica: `EP-01: Fundaciones y especificación`

**Card**

> Como equipo de desarrollo, quiero reglas de trabajo versionadas y una capa agéntica compartida por Claude Code y OpenCode, para que personas y agentes trabajen con la misma fuente de verdad.

**Conversación**

- Se adoptó el harness: `docs/` con los estándares base, de documentación, de ingeniería y de backend, más el contrato de Trello; `ai-specs/` con 7 skills, 1 agent y el script `check-references.sh`.
- Claude Code y OpenCode quedaron activos. OpenCode recibe `AGENTS.md` y las skills, pero no los agents: rechaza su frontmatter y deja de arrancar (verificado ejecutándolo).
- `syncing-agent-symlinks` 1.1.0 admite tipos no espejados por agente; se probó con subagentes en ciclo RED-GREEN-REFACTOR. `check-references.sh` lee los espejos del mapa de la skill y se cambió con TDD.
- `docs/documentation-standards.md` fija cuándo se actualiza cada artefacto de spec-kit: la falta de esa regla causó el desfase de `tasks.md`.
- La deuda detectada vive en `docs/backend-standards.md`, sección "Desviaciones conocidas".

**Tareas**

- Adoptar el harness: agentes, integraciones, estándares de dominio y README
- Integrar OpenCode como agente activo
- Extender `syncing-agent-symlinks` con tipos no espejados y probarla con subagentes
- Generalizar `check-references.sh` para leer los espejos del mapa, con TDD
- Definir cuándo se actualizan los artefactos de spec-kit

**Criterios de Aceptación**

- Dado el repositorio, cuando se ejecuta `bash ai-specs/scripts/check-references.sh`, entonces termina en OK sin hallazgos.
- Dado OpenCode instalado, cuando se abre en el repositorio, entonces arranca y lista las 7 skills del proyecto.
- Dado el commit de adopción, cuando se inspecciona en git, entonces `CLAUDE.md`, `AGENTS.md` y los 15 espejos están registrados como symlinks (modo `120000`).

### HU-03 · Registro y gestión de estado de usuarios

Épica: `EP-02: Gestión de usuarios y recursos`

**Card**

> Como administrador del sistema, quiero registrar, consultar y activar o inactivar usuarios, para que solo los usuarios activos puedan reservar.

**Conversación**

- Cubre FR-001 a FR-003. Endpoints: `POST /usuarios`, `GET /usuarios`, `GET /usuarios/{id}` y `PUT`/`PATCH /usuarios/{id}/estado`.
- Un usuario tiene nombre, documento, correo y estado (`ACTIVO`, `INACTIVO`). Documento y correo son únicos: lo valida el servicio y lo refuerza la base de datos.
- Errores con el formato común: `DOCUMENTO_DUPLICADO` y `CORREO_DUPLICADO` (400), usuario inexistente (404), datos inválidos (400, `VALIDACION`).
- Implementada sin pruebas y sin compilación verificada (el entorno no tiene JDK): los criterios quedan sin marcar hasta que una prueba los verifique.

**Tareas**

- Crear la tabla `usuarios` en la migración inicial y la entidad JPA
- Implementar el repositorio, los DTOs y el mapper de usuarios
- Implementar `UsuarioService` con registro, consulta y cambio de estado
- Exponer los endpoints en `UsuarioController`

**Criterios de Aceptación**

- Dado un usuario con datos válidos, cuando se registra, entonces se responde 201 con el usuario creado.
- Dado un documento ya registrado, cuando se registra otro usuario con él, entonces se responde 400 con el error `DOCUMENTO_DUPLICADO`.
- Dado un correo ya registrado, cuando se registra otro usuario con él, entonces se responde 400 con el error `CORREO_DUPLICADO`.
- Dado un usuario activo, cuando se cambia su estado a `INACTIVO`, entonces la consulta lo devuelve `INACTIVO`.
- Dado un id inexistente, cuando se consulta el usuario, entonces se responde 404 con el cuerpo de error estándar.

### HU-04 · Registro y gestión de estado de recursos

Épica: `EP-02: Gestión de usuarios y recursos`

**Card**

> Como administrador del sistema, quiero registrar, actualizar y cambiar el estado de los recursos, para que solo los recursos disponibles se puedan reservar.

**Conversación**

- Cubre FR-004 a FR-006. Endpoints: `POST /recursos`, `GET /recursos`, `GET /recursos/{id}`, `PUT /recursos/{id}` y `PUT`/`PATCH /recursos/{id}/estado`.
- Un recurso tiene nombre, descripción, tipo, capacidad (mayor que 0) y estado (`DISPONIBLE`, `MANTENIMIENTO`, `FUERA_DE_SERVICIO`). La base de datos refuerza capacidad y estado con restricciones `CHECK`.
- Qué pasa con las reservas futuras de un recurso que entra en mantenimiento es una duda abierta de `spec.md` y queda fuera de esta historia.
- Implementada sin pruebas y sin compilación verificada (el entorno no tiene JDK): los criterios quedan sin marcar hasta que una prueba los verifique.

**Tareas**

- Crear la tabla `recursos` en la migración inicial y la entidad JPA
- Implementar el repositorio, los DTOs y el mapper de recursos
- Implementar `RecursoService` con registro, actualización, consulta y cambio de estado
- Exponer los endpoints en `RecursoController`

**Criterios de Aceptación**

- Dado un recurso con datos válidos, cuando se registra, entonces se responde 201 con el recurso creado.
- Dado un recurso con capacidad 0, cuando se registra, entonces se responde 400 con el error `VALIDACION`.
- Dado un recurso existente, cuando se actualizan sus datos, entonces la consulta devuelve los valores nuevos.
- Dado un recurso `DISPONIBLE`, cuando se cambia su estado a `MANTENIMIENTO`, entonces la consulta lo devuelve en `MANTENIMIENTO`.
- Dado un id inexistente, cuando se consulta el recurso, entonces se responde 404 con el cuerpo de error estándar.

### HU-05 · Creación de reservas con reglas de negocio y control de concurrencia

Épica: `EP-03: Ciclo de vida de reservas`

**Card**

> Como usuario activo, quiero reservar un recurso disponible para un intervalo de fecha y hora, para asegurar su uso exclusivo durante ese tiempo.

**Conversación**

- Cubre FR-007 a FR-013 con `POST /reservas`, y la tarea T-009 de `tasks.md`.
- Reglas, en el orden en que se validan: fin posterior al inicio y sin fechas pasadas, usuario `ACTIVO`, recurso `DISPONIBLE`, máximo 3 reservas activas por usuario, y sin solapamiento con una reserva activa del mismo recurso.
- El solapamiento usa intervalos semiabiertos `[inicio, fin)`: una reserva que empieza justo cuando termina otra no choca. Solo cuentan las reservas `ACTIVA`.
- Concurrencia: dentro de la transacción se bloquean el usuario y el recurso con `PESSIMISTIC_WRITE`, para que dos solicitudes simultáneas no confirmen el mismo intervalo. La base de datos refuerza `fecha_fin > fecha_inicio`.
- Implementada sin pruebas y sin compilación verificada (el entorno no tiene JDK): los criterios quedan sin marcar hasta que una prueba los verifique.

**Tareas**

- Crear la tabla `reservas` con sus restricciones e índices
- Implementar la consulta de solapamiento y el conteo de reservas activas
- Implementar `ReservaService.crear` con las reglas de negocio
- Aplicar bloqueo pesimista sobre usuario y recurso al crear la reserva
- Exponer `POST /reservas` en `ReservaController`

**Criterios de Aceptación**

- Dado un usuario activo y un recurso disponible sin reservas en el intervalo, cuando reserva, entonces se responde 201 con la reserva en estado `ACTIVA`.
- Dado un usuario `INACTIVO`, cuando intenta reservar, entonces se responde 400 con el error `USUARIO_INACTIVO`.
- Dado un recurso en `MANTENIMIENTO` o `FUERA_DE_SERVICIO`, cuando se intenta reservar, entonces se responde 409 con el error `RECURSO_NO_DISPONIBLE`.
- Dado un usuario con 3 reservas activas, cuando intenta una cuarta, entonces se responde 400 con el error `LIMITE_RESERVAS_ACTIVAS`.
- Dada una reserva activa del recurso, cuando se pide un intervalo que se solapa con ella, entonces se responde 409 con el error `RECURSO_NO_DISPONIBLE`.
- Dada una reserva activa que termina a las 10:00, cuando se pide otra desde las 10:00 en el mismo recurso, entonces se crea.
- Dado un intervalo con fin igual o anterior al inicio, cuando se reserva, entonces se responde 400 con el error `FECHAS_INVALIDAS`.
- Dado un intervalo que empieza en el pasado, cuando se reserva, entonces se responde 400 con el error `FECHA_PASADA`.

### HU-06 · Cancelación de reservas

Épica: `EP-03: Ciclo de vida de reservas`

**Card**

> Como usuario activo, quiero cancelar una reserva activa, para liberar el recurso cuando ya no lo necesito.

**Conversación**

- Cubre FR-014 y FR-015 con `DELETE /reservas/{id}`, que no borra la reserva: la pasa a `CANCELADA` y la devuelve.
- Solo se cancela una reserva `ACTIVA`. Una `FINALIZADA` o ya `CANCELADA` se rechaza. La reserva se bloquea con `PESSIMISTIC_WRITE` durante la cancelación.
- Hoy ninguna reserva pasa a `FINALIZADA`, porque no existe esa transición: el rechazo de cancelar una finalizada está implementado pero no puede ocurrir todavía. Cómo se finaliza una reserva queda fuera de esta historia.
- Implementada sin pruebas y sin compilación verificada (el entorno no tiene JDK): los criterios quedan sin marcar hasta que una prueba los verifique.

**Tareas**

- Implementar `ReservaService.cancelar` con las reglas de cancelación
- Bloquear la reserva durante la cancelación
- Exponer `DELETE /reservas/{id}` en `ReservaController`

**Criterios de Aceptación**

- Dada una reserva `ACTIVA`, cuando se cancela, entonces se devuelve con estado `CANCELADA`.
- Dada una reserva `CANCELADA`, cuando se intenta cancelar otra vez, entonces se responde 400 con el error `RESERVA_CANCELADA`.
- Dada una reserva `FINALIZADA`, cuando se intenta cancelar, entonces se responde 400 con el error `RESERVA_FINALIZADA`.
- Dado un id inexistente, cuando se intenta cancelar, entonces se responde 404 con el cuerpo de error estándar.

### HU-07 · Consulta de reservas por usuario y recurso, y disponibilidad

Épica: `EP-03: Ciclo de vida de reservas`

**Card**

> Como usuario activo, quiero consultar las reservas y la disponibilidad de un recurso en un rango, para planificar antes de reservar.

**Conversación**

- Cubre FR-016 en parte. Endpoints: `GET /reservas/{id}`, `GET /reservas/usuario/{usuarioId}`, `GET /reservas/recurso/{recursoId}` y `GET /recursos/{id}/disponibilidad?fechaInicio=&fechaFin=` (ISO 8601).
- La disponibilidad aplica la misma regla de solapamiento que la creación: solo bloquean las reservas `ACTIVA`, con intervalos semiabiertos.
- La consulta de reservas por fecha que también exige FR-016 queda fuera de esta historia (tarea T-016).
- Implementada sin pruebas y sin compilación verificada (el entorno no tiene JDK): los criterios quedan sin marcar hasta que una prueba los verifique.

**Tareas**

- Implementar las consultas de reservas por id, usuario y recurso
- Implementar la verificación de disponibilidad de un recurso en un rango
- Exponer los endpoints de consulta en `ReservaController` y `RecursoController`

**Criterios de Aceptación**

- Dado un usuario con reservas, cuando se consultan por su id, entonces se devuelven ordenadas por fecha de inicio.
- Dado un recurso con reservas, cuando se consultan por su id, entonces se devuelven ordenadas por fecha de inicio.
- Dado un recurso con una reserva activa de 9:00 a 10:00, cuando se consulta la disponibilidad de 9:30 a 10:30, entonces se informa que no está disponible.
- Dado un recurso cuya única reserva en el rango está `CANCELADA`, cuando se consulta la disponibilidad, entonces se informa que está disponible.
- Dado un rango con fin anterior al inicio, cuando se consulta la disponibilidad, entonces se responde 400.

### HU-08 · Resolución de las dudas abiertas de la especificación

Épica: `EP-01: Fundaciones y especificación`

**Card**

> Como equipo de desarrollo, quiero resolver y registrar las dudas abiertas de la especificación, para implementar y probar contra reglas sin ambigüedad.

**Conversación**

- `spec.md` tiene 3 `NEEDS CLARIFICATION`: cómo pasa una reserva a `FINALIZADA`, si los extremos del intervalo cuentan para el solapamiento, y qué pasa con las reservas futuras de un recurso que entra en `MANTENIMIENTO`.
- Los extremos ya están decididos de hecho: `research.md` adopta intervalos semiabiertos `[inicio, fin)` y el código los aplica. Falta registrarlo en `spec.md`.
- Para `FINALIZADA`, el plan de trabajo propone un job programado configurable. Es una propuesta: la confirma el responsable funcional.
- Indeterminado (bloquea el DoR): si las reservas futuras de un recurso en mantenimiento se cancelan automáticamente o si se impide el cambio de estado. Es una decisión de negocio.
- Cada decisión se integra en los artefactos de spec-kit afectados, según `docs/documentation-standards.md`.

**Tareas**

- Registrar en `spec.md` la decisión de intervalos semiabiertos
- Decidir con el responsable funcional cuándo y cómo una reserva pasa a `FINALIZADA`
- Decidir qué ocurre con las reservas futuras de un recurso que entra en mantenimiento
- Actualizar `spec.md`, `plan.md`, `tasks.md`, `data-model.md` y `checklists/requirements.md` con cada decisión

**Criterios de Aceptación**

- Dado `spec.md`, cuando se busca `NEEDS CLARIFICATION`, entonces no queda ninguna.
- Dada cada decisión tomada, cuando se revisan `plan.md` y `tasks.md`, entonces la reflejan y hay tareas para el trabajo que implica.
- Dado un recurso con reservas futuras que pasa a mantenimiento, cuando se lee la especificación, entonces describe un único comportamiento esperado.

### HU-09 · Consulta de reservas por rango de fechas

Épica: `EP-03: Ciclo de vida de reservas`

**Card**

> Como usuario activo, quiero consultar las reservas de un rango de fechas, para ver la ocupación de un período sin revisar recurso por recurso.

**Conversación**

- Cubre la parte de FR-016 que falta y la tarea T-016 de `tasks.md`.
- Ruta y parámetros definidos: `GET /reservas/rango?fechaInicio=<ISO-8601>&fechaFin=<ISO-8601>`.
- No se combina con filtros por usuario o recurso: esos casos ya tienen endpoints específicos.
- Una reserva entra en el resultado si se solapa con el rango, con la misma semántica semiabierta `[inicio, fin)` que la creación.
- Se devuelven las reservas de cualquier estado; la respuesta incluye `estado` para que el cliente filtre.
- El fin debe ser estrictamente posterior al inicio; si no, se responde 400 con el error `FECHAS_INVALIDAS`.

**Tareas**

- Definir la ruta y los parámetros del endpoint
- Implementar la consulta por rango en `ReservaRepository`
- Implementar la validación del rango en `ReservaService`
- Exponer el endpoint en `ReservaController`
- Cubrir el servicio y el endpoint con pruebas unitarias
- Marcar T-016 en `tasks.md`

**Criterios de Aceptación**

- Dadas reservas dentro y fuera del rango, cuando se consulta el rango, entonces se devuelven solo las que se solapan con él.
- Dado un rango con fin igual o anterior al inicio, cuando se consulta, entonces se responde 400.
- Dado un rango sin reservas, cuando se consulta, entonces se devuelve una lista vacía.

### HU-10 · Finalización automática de reservas vencidas

Épica: `EP-03: Ciclo de vida de reservas`

**Card**

> Como usuario activo, quiero que mis reservas pasen a `FINALIZADA` cuando termina su intervalo, para que las reservas pasadas no me impidan reservar de nuevo.

**Conversación**

- Hoy ninguna reserva pasa a `FINALIZADA`. Una reserva vencida sigue `ACTIVA` y cuenta para el límite de 3: un usuario con 3 reservas pasadas no puede volver a reservar.
- También deja sin efecto la regla de no cancelar una reserva finalizada.
- El plan de trabajo propone un job programado con frecuencia configurable.
- Supuesto: una reserva vence cuando su `fecha_fin` es anterior al momento de la ejecución. Debe coincidir con la decisión que se registre en `spec.md`.
- Indeterminado (bloquea el DoR): la frecuencia por defecto del job.

**Tareas**

- Implementar la transición de reservas vencidas a `FINALIZADA`
- Programar su ejecución con frecuencia configurable
- Registrar la frecuencia en la configuración de la aplicación
- Actualizar `spec.md` y `tasks.md`

**Criterios de Aceptación**

- Dada una reserva `ACTIVA` cuyo fin ya pasó, cuando se ejecuta la finalización, entonces queda `FINALIZADA`.
- Dada una reserva `ACTIVA` en curso o futura, cuando se ejecuta la finalización, entonces sigue `ACTIVA`.
- Dada una reserva `CANCELADA` vencida, cuando se ejecuta la finalización, entonces sigue `CANCELADA`.
- Dado un usuario con 3 reservas activas ya vencidas, cuando se ejecuta la finalización y luego reserva, entonces se crea la nueva reserva.

### HU-11 · Contrato OpenAPI de la API

Épica: `EP-03: Ciclo de vida de reservas`

**Card**

> Como desarrollador que consume la API, quiero un contrato OpenAPI publicado, para integrar sin leer el código.

**Conversación**

- El plan de trabajo pide exponer OpenAPI/Swagger.
- El contrato documenta los códigos de error y la estructura común `status`, `error`, `message`.
- El catálogo de `docs/documentation-standards.md` prevé el contrato en `docs/api-spec.yml`.
- Indeterminado (bloquea el DoR): si el contrato se escribe a mano o se genera desde el código (por ejemplo, con springdoc-openapi) y se exporta a esa ruta.

**Tareas**

- Decidir cómo se produce el contrato
- Documentar cada endpoint con sus requests, responses y errores
- Publicar la interfaz de consulta del contrato
- Versionar el contrato en `docs/api-spec.yml`

**Criterios de Aceptación**

- Dada la API en ejecución, cuando se abre la interfaz del contrato, entonces aparecen todos los endpoints de usuarios, recursos y reservas.
- Dado un endpoint con errores de negocio, cuando se consulta su contrato, entonces documenta sus códigos y la estructura de error común.
- Dado un cambio en una ruta, cuando se revisa `docs/api-spec.yml`, entonces refleja la ruta nueva.

### HU-12 · Paginación y filtros en listados

Épica: `EP-03: Ciclo de vida de reservas`

**Card**

> Como administrador del sistema, quiero listados paginados y filtrables, para trabajar con volúmenes grandes de usuarios, recursos y reservas.

**Conversación**

- El plan de trabajo dice "añadir paginación y filtros donde aplique".
- Hoy los listados devuelven todo sin paginar. `RecursoRepository` ya tiene consultas por estado y por tipo que ningún servicio usa.
- Indeterminado (bloquea el DoR): qué listados se paginan, el tamaño de página por defecto y el máximo, y qué filtros ofrece cada uno.
- Supuesto: se usan los parámetros estándar de Spring Data (`page`, `size`, `sort`).

**Tareas**

- Definir qué listados se paginan y con qué filtros
- Implementar la paginación en los listados elegidos
- Implementar los filtros definidos
- Documentar los parámetros en el contrato de la API

**Criterios de Aceptación**

- Dado un listado con más elementos que el tamaño de página, cuando se pide la primera página, entonces se devuelve solo esa página con el total de elementos.
- Dado un filtro definido, cuando se aplica, entonces solo se devuelven los elementos que lo cumplen.
- Dado un tamaño de página mayor que el máximo, cuando se pide, entonces se aplica el máximo.

### HU-13 · Suite de pruebas ejecutable sin base de datos externa

Épica: `EP-04: Calidad y pruebas automatizadas`

**Card**

> Como equipo de desarrollo, quiero que `./gradlew test` corra sin un PostgreSQL externo, para ejecutar las pruebas en cualquier máquina y en CI.

**Conversación**

- Hoy la única prueba es `ReservasApplicationTests` y necesita una base de datos real.
- El plan de trabajo propone un perfil `application-test.yml` con H2 en modo PostgreSQL o con Testcontainers.
- Indeterminado (bloquea el DoR): H2 o Testcontainers. Testcontainers aplica las migraciones sobre PostgreSQL real pero exige Docker; H2 no lo exige pero su dialecto puede divergir.
- Dependencias de prueba propuestas: JUnit 5, Mockito, AssertJ, REST Assured, Cucumber-JVM, Testcontainers y PIT.
- Incluye builders de datos y limpieza entre pruebas, y corregir `gradlew`, que está versionado sin permiso de ejecución.

**Tareas**

- Elegir entre H2 en modo PostgreSQL y Testcontainers
- Crear el perfil de pruebas `application-test.yml`
- Agregar las dependencias de prueba a `build.gradle`
- Implementar builders de datos y limpieza entre pruebas
- Versionar `gradlew` con permiso de ejecución

**Criterios de Aceptación**

- Dada una máquina sin PostgreSQL, cuando se ejecuta `./gradlew test`, entonces la suite pasa.
- Dada la suite, cuando se ejecuta, entonces las migraciones de Flyway se aplican sobre la base de pruebas.
- Dadas dos pruebas que crean datos, cuando se ejecutan en cualquier orden, entonces ninguna ve los datos de la otra.
- Dado un clon nuevo en Linux o macOS, cuando se ejecuta `./gradlew`, entonces no falla por permisos.

### HU-14 · Reglas de negocio verificadas con pruebas unitarias y de mutación

Épica: `EP-04: Calidad y pruebas automatizadas`

**Card**

> Como equipo de desarrollo, quiero cada regla de negocio de los servicios cubierta por pruebas unitarias y de mutación, para que cualquier regresión en las reglas haga fallar el build.

**Conversación**

- Cubre las tareas T-013 y T-015 de `tasks.md`. La constitución exige mutation testing en todo servicio de negocio.
- Casos mínimos de `ReservaServiceTest`: usuario inactivo o inexistente, recurso no disponible o inexistente, fechas inválidas o pasadas, límite de 3 activas, solapamiento parcial, total y en límites exactos, cancelación de `ACTIVA`, `CANCELADA` y `FINALIZADA`, y valores nulos.
- Metas del plan de trabajo: cobertura de líneas de al menos 90 % y mutation score de al menos 80 % en los servicios.
- Supuesto: la cobertura se mide con JaCoCo y la mutación con PIT (`gradle-pitest-plugin`).
- Supuesto: alcanza también a `UsuarioService` y `RecursoService`, porque la constitución lo exige a todo servicio de negocio.

**Tareas**

- Escribir `ReservaServiceTest` con JUnit 5 y Mockito para cada regla y caso borde
- Escribir las pruebas unitarias de `UsuarioService` y `RecursoService`
- Configurar PIT en `build.gradle` con umbral de mutation score
- Configurar la medición de cobertura con umbral
- Marcar T-013 y T-015 en `tasks.md`

**Criterios de Aceptación**

- Dada cada regla de negocio de FR-007 a FR-015, cuando se elimina del código, entonces al menos una prueba unitaria falla.
- Dados los servicios, cuando se mide la cobertura, entonces es de al menos 90 % de líneas.
- Dados los servicios, cuando se ejecuta PIT, entonces el mutation score es de al menos 80 %.
- Dado un mutation score por debajo del umbral, cuando se ejecuta el build, entonces falla.

### HU-15 · Endpoints y contrato de errores verificados con pruebas de integración

Épica: `EP-04: Calidad y pruebas automatizadas`

**Card**

> Como equipo de desarrollo, quiero pruebas de integración de todos los endpoints, para garantizar que la API responde con los códigos y el formato de error acordados.

**Conversación**

- Cubre la tarea T-014 de `tasks.md`, con `@SpringBootTest` y MockMvc.
- Cubre éxito y error de los endpoints de usuarios, recursos y reservas, las validaciones de Bean Validation y la disponibilidad.
- El contrato de error es `status`, `error`, `message` para 400, 404, 409 y 500, como exige la constitución.

**Tareas**

- Escribir las pruebas de integración de `UsuarioController`, `RecursoController` y `ReservaController`
- Probar cada rama de `GlobalExceptionHandler`
- Probar las validaciones de Bean Validation de los requests
- Marcar T-014 en `tasks.md`

**Criterios de Aceptación**

- Dado cada endpoint, cuando se invoca con datos válidos, entonces responde con su código de éxito.
- Dado un error 400, 404, 409 o 500, cuando ocurre, entonces el cuerpo tiene `status`, `error` y `message`.
- Dado un request que viola una anotación de validación, cuando se envía, entonces se responde 400 con el error `VALIDACION`.
- Dado `GlobalExceptionHandler`, cuando se mide la cobertura, entonces todas sus ramas están cubiertas.

### HU-16 · Escenarios de la especificación ejecutables como pruebas de aceptación

Épica: `EP-04: Calidad y pruebas automatizadas`

**Card**

> Como equipo de desarrollo, quiero los escenarios de la especificación como pruebas de aceptación ejecutables, para demostrar con evidencia trazable que el sistema cumple FR-001 a FR-016.

**Conversación**

- El plan de trabajo propone Cucumber-JVM sobre JUnit 5, con features en Gherkin.
- Features: registro y cambio de estado de usuarios y recursos, reserva exitosa, rechazos por usuario inactivo, recurso no disponible y solapamiento, límite de 3 activas, cancelación, y consultas por usuario, recurso y fecha.
- Cada escenario se etiqueta con el FR que verifica, para que la trazabilidad sea consultable.
- Supuesto: corren con una tarea `acceptanceTest`, separadas de la suite rápida.
- Los escenarios de un comportamiento que todavía no existe se escriben igual y quedan como pendientes, sin bloquear al resto.

**Tareas**

- Configurar Cucumber-JVM con JUnit 5
- Escribir las features a partir de los escenarios de `spec.md`
- Implementar los step definitions
- Etiquetar cada escenario con su FR
- Crear la tarea `acceptanceTest` en `build.gradle`

**Criterios de Aceptación**

- Dada la suite de aceptación, cuando se ejecuta `./gradlew acceptanceTest`, entonces todos los escenarios implementados pasan.
- Dado cada requisito de FR-001 a FR-016, cuando se buscan sus escenarios por etiqueta, entonces tiene al menos uno.
- Dado un escenario de un comportamiento todavía no implementado, cuando se ejecuta la suite, entonces aparece como pendiente y no como fallido.

### HU-17 · Cero reservas duplicadas bajo concurrencia

Épica: `EP-05: Concurrencia y rendimiento`

**Card**

> Como usuario activo, quiero que de varias solicitudes simultáneas por el mismo recurso y horario solo se confirme una, para no perder una reserva que el sistema ya me confirmó.

**Conversación**

- Verifica el criterio de éxito "Cero Duplicados" de `spec.md` y el principio 3 de la constitución.
- El bloqueo pesimista sobre usuario y recurso ya está implementado: esta historia demuestra que funciona.
- El plan de trabajo propone una prueba de integración con N hilos que envían a la vez solicitudes para el mismo recurso y horario.
- Supuesto: corre contra PostgreSQL real, porque son sus bloqueos los que se quieren verificar.

**Tareas**

- Escribir la prueba de concurrencia con N hilos simultáneos
- Ejecutarla contra PostgreSQL
- Verificar en la base de datos que no quedan reservas solapadas
- Documentar los parámetros y el resultado de la prueba

**Criterios de Aceptación**

- Dadas N solicitudes simultáneas para el mismo recurso y horario, cuando se procesan, entonces exactamente 1 se confirma con 201 y N-1 reciben 409 `RECURSO_NO_DISPONIBLE`.
- Dada la prueba terminada, cuando se consulta la base de datos, entonces hay una sola reserva activa para ese recurso e intervalo.
- Dadas solicitudes simultáneas de un mismo usuario por recursos distintos, cuando se procesan, entonces nunca supera 3 reservas activas.

### HU-18 · Prueba de carga con 500 usuarios simultáneos

Épica: `EP-05: Concurrencia y rendimiento`

**Card**

> Como equipo de desarrollo, quiero un escenario de carga reproducible con 500 usuarios simultáneos, para demostrar que el sistema mantiene la integridad bajo la carga prevista.

**Conversación**

- `research.md` eligió el bloqueo pesimista pensando en 500 usuarios sobre el mismo recurso.
- El plan de trabajo propone un script con k6, Gatling o JMeter.
- Indeterminado (bloquea el DoR): la herramienta, y si existe un tiempo de respuesta máximo aceptable. `spec.md` solo fija criterios de integridad.
- Supuesto: se ejecuta contra el entorno local levantado con contenedores.

**Tareas**

- Elegir la herramienta de carga
- Escribir el script del escenario de 500 usuarios
- Ejecutar el escenario y registrar los resultados
- Verificar la integridad de los datos al terminar

**Criterios de Aceptación**

- Dados 500 usuarios que reservan a la vez el mismo recurso y horario, cuando termina el escenario, entonces hay exactamente 1 reserva confirmada y 499 respuestas 409.
- Dado el escenario terminado, cuando se consulta la base de datos, entonces no hay reservas duplicadas ni solapadas.
- Dado el script versionado, cuando otra persona lo ejecuta siguiendo sus instrucciones, entonces obtiene el mismo resultado de integridad.

### HU-19 · Reservar un recurso desde la interfaz web

Épica: `EP-06: Interfaz web`

**Card**

> Como usuario activo, quiero buscar un recurso, ver su disponibilidad y reservarlo desde una interfaz web, para no depender de llamadas directas a la API.

**Conversación**

- Indeterminado (bloquea el DoR): el stack de la interfaz. El plan de trabajo recomienda React, Vite, TypeScript, React Router, TanStack Query y Tailwind, y pide preguntar antes si se prefiere Thymeleaf.
- Sin autenticación en esta fase, como deja `spec.md`: el usuario se elige con un selector.
- Los errores 400, 404 y 409 se muestran con mensajes claros, y hay estados de carga, vacío y error.
- Las pruebas E2E se escriben con Playwright.

**Tareas**

- Implementar el selector de usuario
- Implementar el listado y el detalle de recursos con su disponibilidad
- Implementar el formulario de reserva con validación de fechas
- Mostrar los errores de la API con mensajes claros
- Escribir la prueba E2E del flujo con Playwright

**Criterios de Aceptación**

- Dado un usuario activo y un recurso disponible, cuando completa el formulario con un intervalo libre, entonces ve la reserva confirmada.
- Dado un intervalo ocupado, cuando intenta reservar, entonces ve un mensaje que explica el conflicto.
- Dado un formulario con fin anterior al inicio, cuando intenta enviarlo, entonces se le indica el error antes de llamar a la API.
- Dado el flujo de reserva, cuando se ejecuta la prueba E2E, entonces pasa contra el backend.

### HU-20 · Consultar y cancelar mis reservas desde la interfaz web

Épica: `EP-06: Interfaz web`

**Card**

> Como usuario activo, quiero ver mis reservas y cancelar las activas desde la interfaz web, para gestionarlas sin ayuda.

**Conversación**

- Indeterminado (bloquea el DoR): el stack de la interfaz. El plan de trabajo recomienda React, Vite, TypeScript, React Router, TanStack Query y Tailwind, y pide preguntar antes si se prefiere Thymeleaf.
- Solo las reservas `ACTIVA` ofrecen la acción de cancelar.
- Las pruebas E2E se escriben con Playwright.

**Tareas**

- Implementar la pantalla de mis reservas
- Mostrar el estado de cada reserva
- Implementar la cancelación con confirmación
- Escribir la prueba E2E del flujo con Playwright

**Criterios de Aceptación**

- Dado un usuario con reservas, cuando abre la pantalla de mis reservas, entonces ve todas con su estado.
- Dada una reserva `ACTIVA`, cuando la cancela y confirma, entonces aparece como `CANCELADA`.
- Dada una reserva `CANCELADA` o `FINALIZADA`, cuando se muestra, entonces no ofrece la acción de cancelar.
- Dado el flujo de cancelación, cuando se ejecuta la prueba E2E, entonces pasa contra el backend.

### HU-21 · Administrar usuarios y recursos desde la interfaz web

Épica: `EP-06: Interfaz web`

**Card**

> Como administrador del sistema, quiero gestionar usuarios, recursos y sus estados desde la interfaz web, para administrar el sistema sin llamadas directas a la API.

**Conversación**

- Indeterminado (bloquea el DoR): el stack de la interfaz. El plan de trabajo recomienda React, Vite, TypeScript, React Router, TanStack Query y Tailwind, y pide preguntar antes si se prefiere Thymeleaf.
- Indeterminado (bloquea el DoR): cómo se distingue al administrador sin autenticación en esta fase.
- Las pruebas E2E se escriben con Playwright.

**Tareas**

- Implementar el listado, el detalle y el alta de usuarios
- Implementar el listado, el detalle, el alta y la edición de recursos
- Implementar el cambio de estado de usuarios y recursos
- Escribir las pruebas E2E con Playwright

**Criterios de Aceptación**

- Dado el panel de administración, cuando registra un usuario válido, entonces aparece en el listado.
- Dado un documento ya registrado, cuando intenta registrar otro usuario con él, entonces ve el error.
- Dado un recurso, cuando cambia su estado a `MANTENIMIENTO`, entonces el listado lo muestra en ese estado.
- Dados los flujos de administración, cuando se ejecutan las pruebas E2E, entonces pasan contra el backend.

### HU-22 · Entorno local con un solo comando

Épica: `EP-07: Operación y entrega continua`

**Card**

> Como equipo de desarrollo, quiero levantar la base de datos, el backend y la interfaz con un solo comando, para empezar a trabajar sin configurar nada a mano.

**Conversación**

- El plan de trabajo propone un `docker-compose.yml` con PostgreSQL, el backend y el frontend como servicio opcional.
- Datos semilla para el entorno local, vía Flyway o `data.sql`.
- Supuesto: los datos semilla no llegan a producción; si van por Flyway, viven en una ubicación que solo carga el perfil local.
- Las credenciales salen de variables de entorno, como hoy `DB_PASSWORD`: ninguna real se versiona.

**Tareas**

- Escribir `docker-compose.yml` con PostgreSQL y el backend
- Agregar el frontend como servicio opcional
- Crear los datos semilla del entorno local
- Documentar las variables de entorno necesarias

**Criterios de Aceptación**

- Dado un clon nuevo con Docker, cuando se ejecuta `docker compose up`, entonces la API responde y la base tiene el esquema migrado.
- Dado el entorno levantado, cuando se consultan usuarios y recursos, entonces aparecen los datos semilla.
- Dado el repositorio, cuando se buscan credenciales reales, entonces no hay ninguna versionada.

### HU-23 · Pipeline de integración continua

Épica: `EP-07: Operación y entrega continua`

**Card**

> Como equipo de desarrollo, quiero un pipeline que compile y ejecute todas las pruebas en cada cambio, para detectar regresiones antes de integrar.

**Conversación**

- El plan de trabajo propone las etapas: compilación, pruebas, PIT, aceptación y E2E.
- Supuesto: GitHub Actions, porque el repositorio está en GitHub. Si fuera otra plataforma, cambian los archivos de configuración, no las etapas.
- Cada etapa que no pasa hace fallar el pipeline, incluido el umbral de mutation score.

**Tareas**

- Crear el workflow de CI
- Configurar las etapas de compilación, pruebas unitarias e integración
- Agregar PIT con su umbral
- Agregar las pruebas de aceptación y E2E
- Publicar los reportes de cada etapa

**Criterios de Aceptación**

- Dado un push o un pull request, cuando se ejecuta el pipeline, entonces corre todas las etapas.
- Dada una prueba que falla, cuando corre el pipeline, entonces termina en rojo e indica la etapa.
- Dado un mutation score bajo el umbral, cuando corre el pipeline, entonces falla.
- Dado un pipeline terminado, cuando se revisa, entonces sus reportes están disponibles.

### HU-24 · README operativo con arquitectura, variables y ejemplos curl

Épica: `EP-07: Operación y entrega continua`

**Card**

> Como persona nueva en el equipo, quiero un README con la arquitectura, las variables de entorno, los comandos y ejemplos de uso de la API, para operar el sistema sin preguntar.

**Conversación**

- El `README.md` actual ya explica qué es el sistema, cómo empezar y qué hay en cada carpeta: esta historia lo amplía, no lo reemplaza.
- Se rige por `docs/documentation-standards.md` y conserva el puntero a `docs/base-standards.md`.

**Tareas**

- Documentar la arquitectura por capas
- Documentar las variables de entorno
- Documentar los comandos de build, pruebas y entorno local
- Agregar ejemplos `curl` de cada recurso de la API

**Criterios de Aceptación**

- Dado el README, cuando una persona nueva lo sigue, entonces levanta el sistema y hace una reserva sin ayuda.
- Dado cada ejemplo `curl`, cuando se ejecuta contra el entorno local, entonces funciona tal como está escrito.
- Dado el README, cuando se ejecuta `check-references.sh`, entonces no reporta referencias rotas.
