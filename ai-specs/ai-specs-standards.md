---
description: "Gobierna los artefactos de la capa agéntica bajo ai-specs: tipos de artefacto, cuándo corresponde una skill y cuándo un agent, el contrato de frontmatter de cada uno, y el proceso de aprendizaje a partir de la retroalimentación del usuario."
---
# Estándares de especificaciones de IA

Este documento gobierna los artefactos bajo `ai-specs/`, que definen cómo los agentes de IA deben comportarse, documentar, planificar y programar en este proyecto.

## Alcance y precedencia

Es un documento delegado desde `docs/base-standards.md`, que es el punto de entrada del proyecto y gobierna las reglas transversales. Aquí se desarrolla un único dominio: la capa agéntica — qué artefactos existen, qué forma tienen y bajo qué condiciones se cambian.

Dos cosas quedan fuera y se delegan a su vez:

- **Cómo se escribe** cada artefacto: la skill `writing-skills` para las skills, `writing-agents` para los agents. Las dos requieren `testing-with-subagents` para el ciclo de pruebas.
- **Cómo se expone** a las herramientas de IA: la skill `syncing-agent-symlinks`, que resuelve los agentes activos del registro de `base-standards.md`.

Las reglas de formato del Markdown son las de `docs/documentation-standards.md`, y la política de idioma es la de `base-standards.md`. Ninguna se reproduce aquí.

## Tipos de artefacto

| Tipo | Ubicación | Unidad | Se expone a los agentes |
|---|---|---|---|
| `skills` | `ai-specs/skills/<nombre>/SKILL.md` | Un directorio por skill, con sus archivos de apoyo | Sí |
| `agents` | `ai-specs/agents/<nombre>.md` | Un archivo por agent | Sí |
| `scripts` | `ai-specs/scripts/` | Un archivo por script | **No** |

Los nombres van en **inglés y kebab-case**, según la política de idioma. Las skills usan **gerundio** (`writing-skills`, `updating-docs`); los agents, una frase nominal que nombra el rol (`product-strategy-analyst`).

`ai-specs/` es la fuente canónica y la única que se edita. Un tipo de artefacto nuevo se agrega primero a esta tabla y después a la de `syncing-agent-symlinks`; nunca al revés y nunca por improvisación.

## Skill o agent: cuál corresponde

La distinción no es de tamaño ni de complejidad, sino de **dónde corre el trabajo**.

Una **skill** es procedimiento, conocimiento o disciplina que sigue la sesión en curso: se carga en el contexto actual y no tiene contexto propio. Un **agent** es un trabajador delegado, con su propia ventana de contexto, su propio conjunto de herramientas y, si hace falta, otro modelo.

| La señal | El artefacto |
|---|---|
| Es una guía, una regla o un procedimiento que la sesión debe seguir | Skill |
| Se activa por coincidencia semántica con lo que pide el usuario | Skill |
| El trabajo necesita un conjunto de herramientas distinto o restringido | Agent |
| El trabajo genera material intermedio que no aporta a la sesión | Agent |
| Hace falta una perspectiva independiente, o varias en paralelo | Agent |

No son alternativas excluyentes: un agent puede cargar skills, y una skill puede indicar que se delegue a un agent.

**En caso de duda, una skill.** Es más barata de escribir, de probar y de mantener, y no arrastra la complejidad de un contexto separado.

## Contrato de frontmatter

Ambos tipos llevan frontmatter YAML. El `description` se escribe **en español y nunca en primera persona** —se inyecta en el system prompt para que otra sesión decida si cargar el artefacto—, y dice **cuándo** usarlo, sin resumir su procedimiento.

### Skills

| Campo | Obligatorio | Notas |
|---|---|---|
| `name` | Sí | Coincide con el nombre del directorio |
| `description` | Sí | Condiciones que la activan. El detalle está en `writing-skills` |
| `author` | Convención del proyecto | No es un campo de la plataforma |
| `version` | Convención del proyecto | Versionado semántico del artefacto **desde la v1.0.0 del harness**. Todos arrancan en `1.0.0` en esa versión, sin importar cuántas iteraciones tuvieran antes: lo previo fue construcción, no releases, y vive en el historial de git |

### Agents

| Campo | Obligatorio | Notas |
|---|---|---|
| `name` | Sí | Coincide con el nombre del archivo, sin la extensión |
| `description` | Sí | Cuándo invocarlo. Admite bloques `<example>` con contexto y comentario. El detalle está en `writing-agents` |
| `tools` | Sí | Lista explícita con el mínimo necesario. Es el único campo cuya omisión amplía el alcance en silencio: sin él, el agent hereda todas las de la sesión |
| `model` | Sí | **Alias de familia** (`opus`, `sonnet`), nunca un ID de versión fijado |
| `color` | Opcional | Identificación visual |

La plataforma admite más campos en un agent —entre otros `disallowedTools`, `permissionMode`, `maxTurns`, `skills` y `effort`— y este harness usa el subconjunto mínimo. Usar alguno de los demás es legítimo cuando el agent lo necesita, pero se justifica en el propio artefacto.

## Scripts

`ai-specs/scripts/` guarda utilidades ejecutables que las skills invocan: validadores, generadores, verificaciones repetitivas.

**Nunca se exponen a los directorios de los agentes.** Un script no es una instrucción que un agente deba descubrir y cargar: es una herramienta que una skill decide ejecutar, y la skill que lo usa lo referencia por su ruta canónica.

Un script que no está referenciado desde ninguna skill es código huérfano: se reporta, no se conserva por si acaso.

## Cambiar un artefacto existente

Los artefactos de esta capa se cambian **a pedido, no por iniciativa propia**. Un agente que edita una skill porque le pareció mejorable está reescribiendo las reglas con las que se lo evalúa.

| Antipatrón | En su lugar |
|---|---|
| Editar un artefacto sin aprobación explícita | Proponer el cambio y esperar |
| Proponer sin decir qué lo originó | Citar la retroalimentación o el hallazgo concreto que lo motiva |
| Proponer "mejorar la skill X" | Nombrar la sección y el enunciado exactos que cambian |
| Arrastrar artefactos no relacionados | Un cambio por vez, dentro del alcance de lo que se pidió |
| Aplicar el cambio y no decirlo | Reportar qué quedó modificado, con su archivo |

**Señalar no es cambiar.** Detectar que un artefacto tiene un defecto —una afirmación falsa, una contradicción con otro— se reporta como observación aunque nadie lo haya pedido, y aunque el cambio que lo causó ya tenga su commit hace tiempo. Lo que requiere aprobación es la edición, no el hallazgo.

Dos reglas vecinas que **no se reproducen aquí**: crear un archivo con un nombre que el catálogo no cubre lo gobierna `docs/documentation-standards.md`, y desplegar un artefacto sin una prueba fallida previa lo gobierna la Ley de Hierro, en `writing-skills` y `writing-agents`.
