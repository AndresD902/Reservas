---
description: Punto de entrada del proyecto. Reglas base, convenciones transversales y mapa de los estándares, dirigido tanto a las personas del equipo como a los agentes de IA que trabajan sobre el repositorio.
---
# Estándares base del proyecto

Este es el punto de entrada del proyecto. Lo leen las personas del equipo para contextualizarse y los agentes de IA para saber cómo trabajar aquí.

## Alcance y precedencia

Los puntos de entrada de la raíz son symlinks a este archivo: `CLAUDE.md` por la convención de Claude Code, y `AGENTS.md` por el estándar transversal que lee buena parte del ecosistema. La fuente de verdad es este documento, no el archivo que abre cada herramienta: así las reglas no quedan atadas a la herramienta con la que se trabaje.

Orden de precedencia cuando algo entra en conflicto:

1. Una instrucción explícita del usuario en la conversación.
2. Este documento.
3. El documento delegado que gobierna el dominio específico.

Este documento es un **índice más las reglas invariantes**, no una enciclopedia: se carga completo en cada sesión de trabajo, así que el detalle vive en los documentos delegados del mapa de estándares. Si algo solo se necesita una vez, no va aquí.

## Política de idioma

Decisión del equipo, no negociable. **Esta tabla es la fuente única.** Ningún otro documento la
reproduce ni enumera sus filas como si fueran la regla: una copia diverge en cuanto la tabla cambia.
Mencionar de qué trata la política, atribuyéndola a este documento, sí es legítimo y a veces
necesario —quien lee un estándar de dominio necesita el contexto sin salir a buscarlo—.

| Elemento | Idioma |
|---|---|
| Todo el contenido de `docs/` | Español |
| Todo el contenido de `ai-specs/` | Español |
| Comentarios y docstrings en el código | Español |
| Código, identificadores, nombres de funciones y variables | Inglés |
| Nombres de archivos y directorios | Inglés (kebab-case) |
| Mensajes de commit | Inglés |

**La terminología de ingeniería de software se mantiene en inglés dentro de la prosa en español.** Se escribe *skills*, no "habilidades"; *Test Driven Development*, no "desarrollo orientado a pruebas"; *RED-GREEN-REFACTOR*, no "ROJO-VERDE-REFACTORIZACIÓN". La regla del español cubre la prosa —explicaciones, descripciones, notas—, no los nombres propios de la disciplina.

## Principios de trabajo

- **Pasos pequeños, de uno en uno.** Nunca avanzar más de un paso sin verificar.
- **Cambios incrementales y enfocados** antes que modificaciones grandes y acopladas.
- **Cuestionar los supuestos y las inferencias**, propias y ajenas.
- **Nombres claros y descriptivos** en todo lo que se cree.
- **Señalar los patrones repetidos** cuando se detecten, en lugar de replicarlos en silencio.
- **No inventar lo que no se puede derivar.** Lo que no salga del contexto disponible se marca como supuesto pendiente de confirmar o como indeterminado.

## Disciplina de verificación

**Antes de ejecutar, define el criterio observable que demuestra que el trabajo está hecho, y verifica que hoy no se cumple.** Un criterio que ya se cumplía antes de empezar no prueba nada.

El principio es transversal; lo que cambia es su forma según el tipo de trabajo:

| Tipo de trabajo | Criterio observable previo | Detalle |
|---|---|---|
| Código | Una prueba que falla (TDD) | `engineering-standards.md` |
| Skills y agents | Un baseline RED con subagentes | Skill `testing-with-subagents` |
| Configuración o proceso | Un criterio de aceptación verificable en la historia | `trello-backlog-contract.md` |

## La especificación es la fuente de verdad

Cuando aparece un cambio o una corrección a mitad de la ejecución, no se resuelve como un arreglo informal directo sobre la implementación. El orden es:

1. Actualizar el artefacto de especificación afectado (la historia, el contrato, el documento de diseño), integrando el cambio donde corresponde y no como un apéndice de "bugfix".
2. Recién entonces implementar.
3. Verificar contra el artefacto actualizado.

Un cambio que deja la especificación desactualizada está incompleto, aunque funcione.

## Mapa de estándares

| Documento | Gobierna | Estado |
|---|---|---|
| `docs/base-standards.md` | Este documento: reglas base y transversales | Vigente |
| `docs/documentation-standards.md` | Catálogo, convención de nombres y formato de la documentación técnica y del backlog | Vigente |
| `docs/engineering-standards.md` | Mecánica de desarrollo de software | Vigente |
| `docs/harness-adoption-guide.md` | Cómo adaptar este harness a un proyecto nuevo | Vigente |
| `docs/trello-backlog-contract.md` | Esquema de las cards del backlog | Vigente |
| `docs/backend-standards.md` | Stack, estructura de paquetes, base de datos y migraciones del backend | Vigente |
| `ai-specs/ai-specs-standards.md` | Artefactos de la capa agéntica | Vigente |

Los estándares por dominio (`backend-standards.md`, `frontend-standards.md`, y los que el proyecto requiera) se crean cuando el proyecto los necesita, siguiendo la convención de nombres de `documentation-standards.md`.

## Capa agéntica

La capa agéntica vive en `ai-specs/` y tiene tres tipos de artefacto: las **skills** en `ai-specs/skills/`, los **agents** en `ai-specs/agents/`, y los **scripts** en `ai-specs/scripts/` —utilidades que una skill ejecuta, no instrucciones que un agente cargue—. Qué es cada uno y qué forma tiene lo gobierna `ai-specs/ai-specs-standards.md`.

Cuando una petición coincide con una skill, cárgala y sigue su `SKILL.md` antes de continuar, junto con los archivos de apoyo de su directorio que la propia skill indique.

`ai-specs/` es la fuente canónica. Los directorios de los agentes activos contienen **symlinks generados**: nunca se editan a mano ni se crean por improvisación. Los cambios en los espejos se hacen con la skill `syncing-agent-symlinks`, que resuelve los agentes activos del registro y verifica la integridad de los puntos de entrada del repositorio. Cuándo se corre lo fija la regla de las convenciones de control de versiones, más abajo.

Cómo se escribe cada artefacto está en su skill: `writing-skills` para las skills, `writing-agents` para los agents. Las dos requieren `testing-with-subagents`, que es el ciclo de pruebas con subagentes, neutral al tipo de artefacto.

## Modelo y modo de razonamiento

Las tareas de planeación, diseño y refinamiento deben correr con el **modelo Opus más reciente disponible, en modo de razonamiento alto**. El trabajo mecánico o de ejecución acotada no lo requiere.

Se usan **alias de familia** (`opus`, `sonnet`) al configurar un artefacto. **No se fijan IDs de versión concretos** (`claude-opus-4-7` y similares): quedan obsoletos y atan el harness a un momento en el tiempo.

Si la sesión no está en el modo que la tarea requiere, **dilo y pregunta**. No modifiques la configuración del agente por iniciativa propia.

## Agentes del proyecto

Herramientas de IA con las que se trabaja este proyecto. Determina qué directorios y qué puntos de entrada expone el repositorio, y por lo tanto qué crea y qué elimina la skill `syncing-agent-symlinks`.

Se resuelve con el mismo orden que las integraciones: argumento explícito del usuario → esta tabla → preguntar. **La herramienta que ejecuta una tarea no implica que esté activa:** trabajar con un agente no autoriza a crear su directorio si el registro no lo declara.

| Agente | Estado |
|---|---|
| Claude Code | Activo |
| OpenCode | Activo |

## Integraciones

Identificadores de recursos externos que una skill puede necesitar resolver. Ningún contrato, skill ni agent debe hardcodear estos valores.

Orden de resolución, sin excepción:

1. **Argumento explícito** del usuario en el prompt (id, URL o nombre).
2. **Esta tabla**, si el recurso está configurado.
3. **Preguntar al usuario** si no hay ni argumento ni configuración.

**Nunca se infiere un recurso por afinidad temática o de nombre.** Si hay varios candidatos y ninguno se identifica con claridad, se pregunta; no se elige el más parecido.

| Servicio | Identificador |
|---|---|
| Tablero de backlog en Trello | https://trello.com/b/wNkNjIzE/sistema-de-reservas-calidad |

## Convenciones de control de versiones

- **Conventional Commits** para los mensajes: `feat:`, `fix:`, `refactor:`, `chore:`, `docs:`.
- **Antes de cualquier commit o push**, revisa qué documentación quedó desactualizada por los cambios e invoca la skill `updating-docs`.
- **Si cambió algo bajo `ai-specs/`**, corre la skill `syncing-agent-symlinks` antes de considerar el cambio terminado.
