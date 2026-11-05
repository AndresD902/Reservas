# Buenas prácticas oficiales de Anthropic para escribir skills

Destilado en español de la guía oficial de Anthropic. Fuente completa: [code.claude.com — Skill authoring best practices](https://code.claude.com/docs/en/skills.md) y [agentskills.io/specification](https://agentskills.io/specification). Ante cualquier duda de detalle, esa es la fuente autoritativa.

## Principios centrales

**La concisión importa.** El contexto es un bien compartido: tu skill convive con el system prompt, el historial de conversación, los metadatos de otras skills y la petición real del usuario. Al inicio de la sesión solo se cargan `name` y `description` de todas las skills; el cuerpo completo se lee solo cuando la skill se activa, y los archivos de apoyo solo cuando hacen falta. Aun así, cada token del cuerpo compite con el resto del contexto una vez cargado.

**Claude ya es inteligente.** Agrega solo el contexto que Claude no tiene ya. Antes de escribir una explicación, pregúntate: ¿de verdad hace falta? ¿puedo asumir que Claude lo sabe? ¿este párrafo justifica su costo en tokens?

**Ajusta el grado de libertad según la fragilidad de la tarea:**

| Grado | Cuándo usarlo | Forma |
|---|---|---|
| Alto | Hay varios enfoques válidos, la decisión depende del contexto | Instrucciones en texto, heurísticas |
| Medio | Existe un patrón preferido pero cabe variación | Pseudocódigo o script con parámetros |
| Bajo | La operación es frágil, la consistencia es crítica | Script específico, sin parámetros o casi ninguno |

Analogía: Claude explorando un camino. Un puente angosto con precipicios a los lados necesita instrucciones exactas (bajo grado de libertad) — una migración de base de datos que debe correr en una secuencia precisa. Un campo abierto sin peligros permite dar dirección general y confiar en que Claude encuentre la mejor ruta (alto grado de libertad) — una revisión de código donde el contexto determina el mejor enfoque.

## Estructura y frontmatter

Límites duros de la especificación:

| Campo | Límite |
|---|---|
| `name` | 64 caracteres máximo |
| `description` | 1024 caracteres máximo |

**Convención de nombres: usa gerundio.** Es una recomendación explícita de la fuente oficial (*"we recommend using gerund form (verb + -ing) for Skill names"*): `Processing PDFs`, `Analyzing spreadsheets`, `Writing documentation`. Alternativas aceptables: frases nominales (`PDF Processing`) o de acción (`Process PDFs`). Evita nombres vagos (`Helper`, `Utils`) o genéricos (`Documents`, `Data`).

## Descripciones efectivas

El campo `description` decide si Claude activa la skill entre potencialmente cientos disponibles. Cubre **sobre qué opera** y **cuándo usarla**, nunca en primera persona (se inyecta en el system prompt — la inconsistencia de punto de vista dificulta el descubrimiento).

**Nombrar el objeto no es resumir el flujo.** "Extrae texto y tablas de PDFs" dice sobre qué opera y es correcto; "despacha un subagente por tarea con revisión entre tareas" enumera el procedimiento y hace que Claude siga la descripción en lugar de leer la skill. El caso documentado está en `SKILL.md`, sección "Optimización de búsqueda de Claude (CSO)".

```yaml
# ✅ Bien: qué hace + disparadores específicos
description: Extrae texto y tablas de archivos PDF, rellena formularios, combina documentos. Úsala cuando trabajes con archivos PDF o el usuario mencione PDFs, formularios o extracción de documentos.

# ❌ Mal: vago, sin disparadores
description: Ayuda con documentos
```

## Progressive disclosure

`SKILL.md` funciona como un índice que dirige a Claude hacia material detallado según haga falta — igual que la tabla de contenidos de una guía de onboarding.

**Guía práctica:**
- Mantén el cuerpo de `SKILL.md` bajo 500 líneas.
- Divide el contenido en archivos separados al acercarte a ese límite.
- **Mantén las referencias a un solo nivel de profundidad desde `SKILL.md`.** Si un archivo de apoyo referencia a otro, y ese a otro más, Claude puede leer solo parcialmente (con `head -100`, por ejemplo) y perder información. Todos los archivos de referencia deben enlazarse directamente desde `SKILL.md`.
- Para archivos de referencia de más de 100 líneas, incluye una tabla de contenidos al inicio.

**Tres patrones de organización:**

1. **Guía general con referencias** — `SKILL.md` cubre lo básico y enlaza a archivos separados para lo avanzado (`FORMS.md`, `REFERENCE.md`, `EXAMPLES.md`).
2. **Organización por dominio** — para skills con varios dominios, un archivo de referencia por dominio (`finance.md`, `sales.md`), así Claude solo carga lo relevante a la pregunta.
3. **Detalle condicional** — contenido básico en línea, con enlaces a lo avanzado solo cuando aplica (`REDLINING.md` para cambios con seguimiento, `OOXML.md` para el detalle de formato).

## Desarrollo iterativo con Claude

El proceso más efectivo involucra a dos instancias de Claude: una ("Claude A") ayuda a diseñar y refinar la skill; otra ("Claude B") la usa en tareas reales. Es exactamente el patrón de subagentes que aplicamos en este harness.

1. Completa una tarea sin skill, usando prompting normal — nota qué contexto das repetidamente.
2. Identifica el patrón reutilizable de eso.
3. Pide a Claude A que escriba la skill.
4. Revisa que no haya explicaciones innecesarias.
5. Prueba con Claude B en tareas reales — observa si encuentra la información correcta, aplica las reglas bien, y completa la tarea con éxito.
6. Si Claude B falla o se le escapa algo, vuelve a Claude A con detalles concretos de la observación.

Este ciclo observar → refinar → volver a probar mejora la skill según comportamiento real de agentes, no según suposiciones.

## Anti-patrones a evitar

- **Rutas de Windows.** Usa siempre `/` en las rutas, incluso en Windows — las rutas con `\` fallan en sistemas Unix.
- **Demasiadas opciones.** No presentes varios enfoques cuando uno basta. Da un valor por defecto claro, con una vía de escape si hace falta ("Usa pdfplumber para extracción de texto. Para PDFs escaneados que necesiten OCR, usa pdf2image con pytesseract").
- **Información con fecha de caducidad.** No documentes algo que quedará obsoleto ("antes de agosto de 2025 usa la API vieja"). Si hace falta contexto histórico, ponlo en una sección aparte de "patrones antiguos", plegada o claramente marcada.
- **Terminología inconsistente.** Elige un término y úsalo siempre (`endpoint`, no mezclar con `URL`/`ruta`/`API route`).

## Herramientas MCP

Si una skill usa herramientas MCP, usa siempre el nombre completamente calificado (`ServidorMCP:nombre_herramienta`) para evitar errores de "herramienta no encontrada" cuando hay varios servidores MCP disponibles.
