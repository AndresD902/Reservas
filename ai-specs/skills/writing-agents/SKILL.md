---
name: writing-agents
description: Úsala al crear un agent nuevo bajo ai-specs/agents, al editar uno existente, o al verificar que un agent funciona antes de exponerlo. También cuando haya que decidir qué herramientas concederle o cómo debe obtener el contexto del proyecto.
version: 1.0.0
---

# Skill writing-agents

## Resumen

Un agent es un **trabajador delegado**: corre en su propia ventana de contexto, con su propio conjunto de herramientas, y su mensaje final es lo que devuelve a la sesión que lo invocó.

Eso define todo lo demás. No ve la conversación que lo llamó, así que tiene que saber conseguir su contexto. No puede pedir aclaraciones a mitad de camino, así que tiene que saber degradar. Y lo que devuelve es su única salida, así que tiene que estar declarado.

**Si dudas entre una skill y un agent, es una skill.** El criterio completo está en `ai-specs/ai-specs-standards.md`.

## Jerarquía de autoridad

| Nivel | Fuente | Ejemplos |
|---|---|---|
| **Plataforma** | Documentación oficial (ver `anthropic-subagent-practices.md`) | `name` obligatorio; sin `tools` el agent hereda todas |
| **Contrato del proyecto** | `ai-specs/ai-specs-standards.md` | Qué campos usa este harness, ubicación, convención de nombres |
| **Convención de este harness** | Esta skill | La plantilla de secciones de abajo, el paso de contexto obligatorio, escribir en español |

El contrato de frontmatter **no se repite aquí**: vive en `ai-specs/ai-specs-standards.md`. Esta skill cubre el cuerpo, las decisiones de configuración y cómo se prueba.

## Estructura del cuerpo

Convención de este proyecto. El cuerpo se escribe **en segunda persona**: le hablas al agent, no describes al agent.

```markdown
Eres [rol]. [Una o dos frases de identidad y propósito.]

## Alcance
En qué fase del trabajo operas y de qué eres agnóstico.

## Paso 0 - Contexto del proyecto
Qué leer para anclarte al proyecto real, y qué hacer si no hay nada.

## Metodología
Cómo procedes. Numerado si el orden importa.

## Formato de salida
La forma exacta del mensaje que devuelves.

## Entregable
Qué entregas y dónde queda.

## Límites
Qué no haces.
```

`Alcance`, `Paso 0`, `Formato de salida`, `Entregable` y `Límites` son **obligatorias**. Las intermedias se adaptan al agent.

### `## Paso 0 - Contexto del proyecto` es obligatorio

Un agent no ve la sesión que lo invocó. Si necesita saber algo del proyecto, tiene que ir a buscarlo, y hay que decirle dónde.

Tres cosas van siempre en esta sección:

1. **La declaración explícita** de que no ve la conversación llamante: solo su prompt y lo que lea del repositorio.
2. **Qué leer**, concreto: `docs/base-standards.md`, los `*-standards.md` del dominio, y lo que aplique a su trabajo.
3. **La salida degradada.** Un repositorio sin ese contexto es el caso normal de un harness recién clonado: el agent procede con lo que tenga y lo dice, **no se bloquea ni pide contexto que el proyecto no tiene**.

### `## Entregable` declara el valor de retorno

El mensaje final del agent **es** su valor de retorno. Si no dice qué devuelve, la sesión llamante recibe lo que salga.

Si el entregable es un archivo, la ruta tiene que estar sancionada por el catálogo de `docs/documentation-standards.md`. **Si no hay patrón que lo cubra, el agent no crea el archivo:** propone el patrón y espera aprobación. Inventar una convención de nombres sobre la marcha está prohibido por ese mismo catálogo.

### `## Límites` contiene el alcance

Un agent sin límites escritos se expande hacia el trabajo de la sesión que lo llamó. Los límites son la contraparte en prosa de lo que `tools` impide por construcción.

## Concesión de herramientas

**Sin `tools`, el agent hereda todas las de la sesión.** Es el único campo cuya omisión amplía el alcance en silencio, así que se declara siempre.

Al armar la lista, separa dos grupos:

| Grupo | Para qué | Típicamente |
|---|---|---|
| Herramientas de contexto | Ejecutar el Paso 0 | `Read`, `Glob`, `Grep` |
| Herramientas de trabajo | Lo que el agent hace de verdad | Lo que su propósito exija |

**No concedas `Write` por costumbre.** Concédelo solo si el agent tiene un destino sancionado por el catálogo documental. Un agent sin `Write` no puede incumplir su propio límite de "no escribo archivos"; uno con `Write` depende de su disciplina.

## Modelo y campos opcionales

`model` va como **alias de familia** (`opus`, `sonnet`), nunca un ID de versión fijado. La regla y su porqué están en `docs/base-standards.md`, sección "Modelo y modo de razonamiento".

Para elegir: el trabajo de análisis, síntesis y diseño va con `opus`; la ejecución mecánica y acotada no lo necesita. La regla transversal está en `docs/base-standards.md`.

`color` es libre. Los demás campos que la plataforma admite —`effort`, `permissionMode`, `maxTurns`, `skills`, `disallowedTools`— están fuera del subconjunto que este harness usa: si un agent necesita uno, **se justifica en el propio artefacto**. En particular, no uses `effort` para pedir razonamiento profundo si la instrucción en prosa alcanza.

## La Ley de Hierro

```
NINGÚN AGENT SIN UNA PRUEBA FALLIDA PRIMERO
```

Aplica igual a agents nuevos y a ediciones de agents existentes, con la misma excepción que rige para las skills: solo los artefactos puramente de referencia, sin reglas que un agente tenga incentivo de saltarse, quedan fuera. Un agent nunca es ese caso — tiene alcance, límites y herramientas que puede exceder.

**REQUIERE** dos lecturas:

- La skill `testing-with-subagents` para **cómo se corre** el ciclo RED-GREEN-REFACTOR.
- `testing-agents-with-subagents.md`, en este directorio, para **qué cuenta como aprobado en un agent**.

La fase GREEN no es opcional. Documentar el baseline RED y desplegar sin volver a correr el escenario con el agent instalado deja la mitad del ciclo sin hacer.

## Errores comunes

| Error | En su lugar |
|---|---|
| Omitir `tools` | Declararlo siempre: omitirlo concede todo |
| Conceder `Write` por defecto | Solo con un destino que el catálogo documental sancione |
| Fijar un ID de modelo | Alias de familia |
| Cuerpo sin `Paso 0` | Un agent que no ve la sesión llamante necesita saber dónde buscar |
| Paso 0 que se bloquea sin contexto | Degradar y decirlo: un repo sin contexto es el caso normal |
| No declarar el entregable | El mensaje final es el valor de retorno: hay que decir qué es |
| Escribir un archivo sin patrón en el catálogo | Proponer el patrón y esperar aprobación |
| Cuerpo sin `Límites` | Sin ellos el agent se expande hacia el trabajo de la sesión |
| `description` que resume el procedimiento | Las condiciones que lo activan, más bloques `<example>` |
| `description` en primera persona | Tercera persona: se inyecta para que otra sesión decida delegar |
| Copiar la estructura de otro agent sin más | La plantilla de arriba es la convención; el precedente puede estar desactualizado |
| Usar `effort` o `permissionMode` sin justificar | Subconjunto mínimo, o justificación en el artefacto |
| Documentar el RED y desplegar sin GREEN | El ciclo se cierra corriendo el escenario con el agent instalado |

## Checklist

**Fase RED:**
- [ ] Corriste la tarea con un subagente genérico, **sin** el agent, y documentaste textualmente qué salió mal

**Fase GREEN:**
- [ ] `name` igual al nombre del archivo, sin extensión
- [ ] `description` nunca en primera persona, con las condiciones que lo activan y bloques `<example>`
- [ ] `tools` declarado, con el mínimo necesario, separando contexto de trabajo
- [ ] `model` como alias de familia
- [ ] Cuerpo en segunda persona y en español
- [ ] `Alcance`, `Paso 0`, `Formato de salida`, `Entregable` y `Límites` presentes
- [ ] El Paso 0 declara que no ve la sesión llamante, dice qué leer y cómo degradar
- [ ] Corriste el mismo escenario **con** el agent y cumple

**Fase REFACTOR:**
- [ ] Cerraste los vacíos nuevos que apareció el GREEN y volviste a verificar

**Despliegue:**
- [ ] Corriste `syncing-agent-symlinks`
- [ ] Corriste `updating-docs` si la documentación quedó desactualizada
