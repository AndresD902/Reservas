# Buenas prácticas oficiales de Anthropic para escribir agents

Destilado en español de la documentación oficial de subagents. Fuente completa: [code.claude.com — Subagents](https://code.claude.com/docs/en/sub-agents.md). Ante cualquier duda de detalle, esa es la fuente autoritativa.

## Qué es un subagent en la plataforma

Un subagent es un archivo Markdown con frontmatter YAML. En un proyecto vive en `.claude/agents/<nombre>.md`; en este harness la fuente canónica es `ai-specs/agents/<nombre>.md` y ese directorio se expone por symlink, según `syncing-agent-symlinks`.

Al invocarse, corre en **su propia ventana de contexto**: no ve la conversación de la sesión que lo llamó, solo el prompt que recibe. Su mensaje final es el valor que devuelve a esa sesión.

## Campos de frontmatter

Obligatorio: `name`. El resto es opcional, pero omitir algunos tiene consecuencias.

| Campo | Efecto |
|---|---|
| `name` | Identificador del agent |
| `description` | Cuándo invocarlo. Es lo que la sesión llamante usa para decidir si delegarle |
| `tools` | Lista explícita de herramientas. **Sin él, el agent hereda todas las de la sesión** |
| `disallowedTools` | Lista negativa, alternativa a `tools` |
| `model` | Modelo con el que corre |
| `permissionMode` | Modo de permisos con el que opera |
| `maxTurns` | Techo de turnos |
| `skills` | Skills que el agent carga |
| `mcpServers` | Servidores MCP disponibles |
| `hooks` | Hooks propios |
| `memory` | Configuración de memoria |
| `isolation` | Aislamiento de ejecución |
| `effort` | Esfuerzo de razonamiento |
| `background` | Ejecución en segundo plano |
| `color` | Identificación visual |

**Este harness usa el subconjunto mínimo**: `name`, `description`, `tools`, `model`, `color`. Usar cualquiera de los demás es legítimo cuando el agent lo necesita, y se justifica en el propio artefacto — la regla está en `ai-specs/ai-specs-standards.md`.

## Por qué `tools` importa más que los otros

Es el único campo cuya omisión **amplía** silenciosamente lo que el agent puede hacer. Los demás, omitidos, dejan un comportamiento por defecto razonable; `tools` omitido concede todo el arsenal de la sesión a un trabajador que quizá solo necesitaba leer.

Conceder el mínimo no es una preferencia de estilo: es lo que hace que el alcance declarado del agent sea verificable. Un agent que dice "no escribo archivos" y tiene `Write` disponible depende de su propia disciplina; uno sin `Write` no puede incumplirlo.

## `description` y la decisión de delegar

La sesión llamante decide si invocar el agent leyendo su `description`. Vale la misma regla que para las skills: describe **las condiciones que lo activan**, no el flujo interno. Un `description` que resume el procedimiento invita a que la sesión lo imite en lugar de delegar.

El formato de agents admite bloques `<example>` con `Context`, el turno del usuario, la respuesta del asistente y un `<commentary>` que explica por qué corresponde ese agent. Son ejemplos de *cuándo invocarlo*, no de qué produce.

## Modelo

Se usan alias de familia (`opus`, `sonnet`). Los IDs de versión concretos quedan obsoletos y atan el artefacto a un momento en el tiempo — la regla completa está en `docs/base-standards.md`.
