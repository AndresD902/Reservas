---
name: writing-skills
description: Úsala al crear nuevas skills, editar skills existentes, o verificar que las skills funcionan antes de desplegarlas.
version: 1.0.0
---

# Skill writing-skills

## Resumen

**Escribir skills ES Test Driven Development (TDD) aplicado a la documentación de procesos.**

Escribes casos de prueba (escenarios de presión con subagentes), los ves fallar (comportamiento base), escribes la skill (documentación), ves que las pruebas pasan (los agentes cumplen), y refactorizas (cierras vacíos).

**Principio central:** si no viste a un agente fallar sin la skill, no sabes si la skill enseña lo correcto.

## Jerarquía de autoridad

Este documento mezcla reglas de tres orígenes distintos. Cada una está marcada con su nivel para que sepas qué es innegociable y qué es una decisión de este proyecto:

| Nivel | Fuente | Ejemplos |
|---|---|---|
| **Especificación** | [agentskills.io/specification](https://agentskills.io/specification) | `name` ≤64 caracteres, `description` ≤1024 caracteres — límites duros |
| **Buenas prácticas oficiales** | Guía de Anthropic (ver `anthropic-best-practices.md`) | Cuerpo del `SKILL.md` bajo 500 líneas, progressive disclosure, nombres en gerundio, descripciones específicas |
| **Convención de este proyecto** | Este harness | La plantilla de secciones de abajo, el ritual de pruebas con subagentes de la skill `testing-with-subagents`, escribir en español |

**Cuál gana en un conflicto:** la especificación es innegociable y nada la desplaza. Entre las
otras dos, **gana la convención de este proyecto**, porque es la que rige aquí — pero el apartamiento
se declara en el archivo de apoyo que recoge la práctica oficial, con su porqué, en vez de dejar
los dos textos contradiciéndose. La convención de proyecto es obligatoria **aquí**, no es un mandato
de Anthropic. El ritual de pruebas con subagentes en particular es una técnica pesada de un tercer origen (un paquete de skills llamado "superpowers", del que este harness heredó `writing-skills` sin heredar sus skills base) — funciona bien en la práctica, pero no la presentes como requisito oficial.

## ¿Qué es una skill?

Una **skill** es una guía de referencia para técnicas, patrones o herramientas probadas. Las skills ayudan a futuras instancias de Claude a encontrar y aplicar enfoques efectivos.

**Las skills son:** Técnicas reutilizables, patrones, herramientas, guías de referencia.

**Las skills NO son:** Narrativas sobre cómo resolviste un problema una vez.

## Mapeo de TDD para skills

| Concepto de TDD | Creación de Skills |
|---|---|
| **Caso de prueba** | Escenario de presión con subagente |
| **Código de producción** | Documento de la skill (`SKILL.md`) |
| **La prueba falla (RED)** | El agente viola la regla sin la skill (base) |
| **La prueba pasa (GREEN)** | El agente cumple con la skill presente |
| **Refactor** | Cerrar vacíos manteniendo el cumplimiento |

Todo el proceso de creación de skills sigue RED-GREEN-REFACTOR. El ciclo completo —escenarios de presión, tipos de presión, meta-prueba— vive en la skill `testing-with-subagents`, que es neutral al tipo de artefacto. Lo que cuenta como aprobado **para una skill** está en `testing-skills-with-subagents.md`, en este directorio.

## Cuándo crear una skill

**Crea una cuando:**
- La técnica no te resultó intuitivamente obvia
- La referenciarías de nuevo en varios proyectos
- El patrón aplica ampliamente (no es específico del proyecto)
- Otros se beneficiarían de ella

**No la crees para:**
- Soluciones puntuales
- Prácticas estándar bien documentadas en otro lugar
- Convenciones específicas de un dominio (van al `*-standards.md` que lo gobierna, según el catálogo de `docs/documentation-standards.md`; `base-standards.md` es índice y reglas invariantes, no enciclopedia)
- Restricciones mecánicas (si se puede aplicar con regex/validación, automatízalo)

## Tipos de skill

### Técnica
Método concreto con pasos a seguir.

### Patrón
Forma de pensar sobre los problemas.

### Referencia
Documentación de API, guías de sintaxis, documentación de herramientas.

## Estructura de directorios

```
ai-specs/skills/
  nombre-skill/
    SKILL.md              # Referencia principal (requerido)
    archivo-de-apoyo.*     # Solo si es necesario
```

**Espacio de nombres plano** — todas las skills en un único espacio de nombres consultable, sin jerarquía entre ellas. No existe el concepto de "sub-skill": lo que cumple ese rol son los archivos de apoyo dentro del propio directorio, cargados solo cuando hacen falta.

**Archivos separados para:**
1. **Referencia extensa** (más de 100 líneas) — documentación de API, sintaxis exhaustiva
2. **Herramientas reutilizables** — scripts, utilidades, plantillas

**Mantener en línea:** principios y conceptos, patrones de código (menos de 50 líneas), todo lo demás.

## Estructura de SKILL.md

**Frontmatter (YAML) — nivel especificación + convención de proyecto:**
- Dos campos requeridos por la especificación: `name` y `description`. Este harness suma `author` y `version` como convención del proyecto — el contrato completo está en `ai-specs/ai-specs-standards.md`, sección "Contrato de frontmatter"
- `name`: máximo 64 caracteres. Solo letras, números y guiones
- `description`: máximo 1024 caracteres (límite duro de este campo, no del frontmatter completo). Dice **cuándo** usarla, y **nunca resume el proceso, el flujo ni la secuencia de pasos** (ver la sección CSO)
  - Comienza con "Úsala cuando..." para enfocarte en las condiciones que la activan
  - Incluye síntomas, situaciones y contextos específicos
  - Nombrar en una frase sobre qué opera es legítimo y ayuda al descubrimiento; enumerar sus pasos, no. **La existencia de la obligación es un disparador; su contenido es cuerpo de la skill**
  - Nunca en primera persona: se inyecta para que otra sesión decida si cargarla
- `arguments` (opcional): lista de nombres de parámetros si la skill recibe entrada estructurada — ver "Argumentos de Entrada"

```markdown
---
name: nombre-de-skill-con-guiones
description: Úsala cuando [condiciones y síntomas específicos que la activan]
author: [autor del proyecto]
version: 1.0.0
---

# Nombre de la skill

## Resumen
¿Qué es esto? Principio central en 1-2 frases.

## Cuándo usarla
Lista con SÍNTOMAS y casos de uso. Cuándo NO usarla.

## Patrón central (para técnicas/patrones)
Comparación de código antes/después.

## Referencia rápida
Tabla o lista de viñetas para escaneo rápido de operaciones comunes.

## Implementación
Código en línea para patrones simples. Enlace a archivo para referencia extensa o herramientas reutilizables.

## Errores comunes
Qué sale mal + soluciones.

## Impacto en el mundo real (opcional)
Resultados concretos.
```

**Esta plantilla es convención de este proyecto, no un requisito de la especificación** — que solo exige `name` y `description`; el resto del cuerpo es libre. Úsala como punto de partida, no como camisa de fuerza: ninguna de las skills actuales del harness la sigue al pie de la letra.

## Argumentos de entrada

Nivel especificación + comportamiento verificado de la plataforma. Una skill puede recibir entrada del usuario de dos formas:

| Sintaxis | Comportamiento |
|---|---|
| `$ARGUMENTS` | Toda la cadena de argumentos, tal cual la escribió el usuario |
| `$0`, `$1`, … | Argumento posicional (0-based); respeta el quoting del shell |
| `arguments: [nombre1, nombre2]` en frontmatter | Habilita argumentos nombrados `$nombre1`, `$nombre2` |
| Sin ningún placeholder en el cuerpo | El harness anexa `ARGUMENTS: <valor>` al final automáticamente |

**Nota importante:** en Claude Code, los custom commands (`.claude/commands/*.md`) y las skills (`.claude/skills/*/SKILL.md`) se fusionaron — son el mismo mecanismo y comparten esta sustitución de argumentos.

**Cuándo usar cada una:**
- `$ARGUMENTS` — cuando la entrada es texto libre de forma y longitud variable (un ticket completo, una consulta abierta).
- `arguments:` nombrados — cuando hay parámetros discretos con semántica fija y conocida de antemano (un nombre de componente, un origen y un destino).

## Optimización de búsqueda de Claude (CSO)

**Crítico para el descubrimiento:** el futuro Claude necesita ENCONTRAR tu skill.

### 1. Campo de descripción enriquecido

**Propósito:** Claude lee la descripción para decidir qué skills cargar para una tarea dada. Haz que responda: "¿Debería leer esta skill ahora mismo?"

**CRÍTICO: la descripción no resume el flujo de trabajo**

**Dónde está la línea.** Nombrar en una frase sobre qué opera la skill es legítimo y ayuda a
encontrarla. Enumerar sus pasos, su orden o su procedimiento, no: **la existencia de la obligación es
un disparador; su contenido es cuerpo de la skill.**

**Por qué esto importa:** las pruebas revelaron que cuando una descripción resume el flujo de trabajo de la skill, Claude puede seguir la descripción en lugar de leer el contenido completo. Una descripción que decía "revisión de código entre tareas" hizo que Claude realizara UNA sola revisión, aunque el diagrama de flujo de la skill mostraba claramente DOS revisiones (cumplimiento de la especificación y luego calidad del código). Cuando la descripción se cambió a simplemente "Úsala al ejecutar planes de implementación con tareas independientes" (sin resumen del flujo de trabajo), Claude leyó correctamente el proceso completo.

```yaml
# ❌ MAL: resume el flujo de trabajo — Claude sigue el resumen y no lee la skill
description: Úsala al ejecutar planes — despacha un subagente por tarea con revisión de código entre tareas

# ✅ BIEN: las condiciones que la activan
description: Úsala al ejecutar planes de implementación con tareas independientes en la sesión actual

# ✅ BIEN: nombra sobre qué opera, sin enumerar pasos
description: Extrae texto y tablas de archivos PDF. Úsala cuando trabajes con PDFs, formularios o extracción de documentos
```

**Contenido:**
- Usa disparadores concretos, síntomas y situaciones que señalen que esta skill aplica
- Describe el *problema*, no los *síntomas específicos de una tecnología*, salvo que la skill sea específica de esa tecnología
- Nunca en primera persona: se inyecta en el system prompt para que otra sesión decida si cargarla

```yaml
# ❌ MAL: vago, primera persona
description: Puedo ayudarte con pruebas asíncronas cuando fallan de forma intermitente

# ✅ BIEN: condiciones concretas, sin primera persona
description: Úsala cuando las pruebas tengan condiciones de carrera, dependencias de tiempo, o pasen/fallen de forma inconsistente
```

### 2. Cobertura de palabras clave

Usa palabras que Claude buscaría: mensajes de error reales, síntomas ("inestable", "colgado"), sinónimos, y herramientas o comandos concretos.

### 3. Nomenclatura descriptiva

**Usa gerundio** (`writing-skills`, `testing-with-subagents`) o voz activa con verbo primero. Evita nombres vagos (`helper`, `utilities`) o genéricos.

### 4. Eficiencia de tokens

**Problema:** las skills de getting-started y las referenciadas frecuentemente se cargan en cada conversación. Cada token cuenta.

**Cuerpo del SKILL.md: bajo 500 líneas** — nivel de buenas prácticas oficiales, no límite duro. Si lo superas, mueve la referencia extensa a un archivo de apoyo.

```bash
wc -l ai-specs/skills/<nombre-skill>/SKILL.md
```

**Técnicas:**
- Referencia otras skills en vez de repetir su contenido
- Comprime ejemplos: uno excelente vale más que varios mediocres
- No expliques lo obvio a partir del comando o del contexto

### 5. Referencias cruzadas a otras skills y archivos de apoyo

Usa el nombre del archivo o de la skill, con marcadores explícitos de requisito:
- ✅ Bien: `**REQUIERE:** Usa testing-with-subagents para el ciclo de pruebas completo`
- ✅ Bien: para otra skill del harness: `**REQUIERE:** Usa updating-docs para sincronizar la documentación afectada`
- ❌ Mal: `Ver skills/testing/algo` (no queda claro si es obligatoria)
- ❌ Mal: `@archivo.md` (fuerza la carga inmediata, consume contexto antes de necesitarlo)

## Ejemplos de código

**Un ejemplo excelente vale más que muchos mediocres.** Elige el lenguaje más relevante al dominio de la skill.

**Un buen ejemplo:** completo y ejecutable, comentado explicando el PORQUÉ, basado en un escenario real, listo para adaptar.

**No hagas:** implementarlo en varios lenguajes, crear plantillas de rellenar espacios en blanco, escribir ejemplos artificiales.

## Organización de archivos

### Skill autocontenida
```
defense-in-depth/
  SKILL.md    # Todo en línea
```
Cuándo: todo el contenido cabe, no se necesita referencia extensa.

### Skill con herramienta reutilizable
```
condition-based-waiting/
  SKILL.md    # Resumen + patrones
  example.ts  # Ayudantes funcionales para adaptar
```
Cuándo: la herramienta es código reutilizable, no solo narrativa.

### Skill con referencia extensa
```
pptx/
  SKILL.md       # Resumen + flujos de trabajo
  pptxgenjs.md   # Referencia de API extensa
  scripts/       # Herramientas ejecutables
```
Cuándo: el material de referencia es demasiado extenso para incluirlo en línea.

## La Ley de Hierro (igual que en TDD)

```
NINGUNA SKILL SIN UNA PRUEBA FALLIDA PRIMERO
```

Esto aplica tanto a skills NUEVAS como a EDICIONES de skills existentes.

¿Escribiste la skill antes de probarla? Bórrala. Empieza de nuevo.
¿Editaste la skill sin probarla? La misma violación.

**Sin excepciones para skills con reglas que se puedan violar:**
- No para "adiciones simples" ni "solo agregar una sección"
- No conserves cambios sin probar como "referencia"
- Borrar significa borrar

**La única excepción real:** skills puramente de referencia, sin reglas que un agente tenga incentivo de saltarse (documentación de API, guías de sintaxis). Para esas, la revisión de que la información es correcta y se encuentra basta — el criterio completo está en la skill `testing-with-subagents`, sección "Cuándo usarla".

**REQUIERE** dos lecturas: la skill `testing-with-subagents` para el ciclo, los tipos de presión y la meta-prueba; y `testing-skills-with-subagents.md` para lo que cuenta como aprobado en una skill y cómo blindarla contra la racionalización.

## Anti-Patrones

### ❌ Ejemplo narrativo
"En la sesión 2025-10-03, encontramos que projectDir vacío causaba..."
**Por qué es malo:** demasiado específico, no reutilizable.

### ❌ Dilución multi-lenguaje
`example-js.js`, `example-py.py`, `example-go.go`
**Por qué es malo:** calidad mediocre, carga de mantenimiento.

### ❌ Etiquetas genéricas
`helper1`, `helper2`, `step3`, `pattern4`
**Por qué es malo:** las etiquetas deberían tener significado semántico.

## DETENTE: antes de pasar a la siguiente skill

**Después de escribir CUALQUIER skill, DEBES DETENERTE y completar el proceso de despliegue.**

**NO hagas:**
- Crear múltiples skills en lote sin probar cada una
- Pasar a la siguiente skill antes de que la actual esté verificada
- Omitir las pruebas porque "hacerlo en lote es más eficiente"

## Lista de verificación para la creación de skills

**Fase RED:**
- [ ] Crea escenarios de presión (3 o más presiones combinadas para skills de disciplina)
- [ ] Ejecuta los escenarios SIN la skill — documenta el comportamiento base textualmente
- [ ] Identifica patrones en las racionalizaciones/fallas

**Fase GREEN:**
- [ ] El nombre usa solo letras, números y guiones
- [ ] Frontmatter completo: `name`, `description` (máx. 1024 caracteres), `author` y `version`
- [ ] La descripción comienza con "Úsala cuando...", nunca en primera persona, y no resume el flujo
- [ ] Palabras clave para la búsqueda a lo largo del texto
- [ ] Resumen claro con el principio central
- [ ] Aborda las fallas base específicas identificadas en RED
- [ ] Código en línea o enlace a un archivo separado
- [ ] Un ejemplo excelente (no multi-lenguaje)
- [ ] Ejecuta los escenarios CON la skill — verifica que los agentes ahora cumplen

**Fase REFACTOR:**
- [ ] Identifica NUEVAS racionalizaciones a partir de las pruebas
- [ ] Agrega contramedidas explícitas, con los cinco mecanismos de `testing-with-subagents` (aplica a toda skill con reglas que se puedan violar, no solo a las de disciplina)
- [ ] Construye una tabla de racionalizaciones a partir de todas las iteraciones
- [ ] Vuelve a probar hasta que sea infalible

**Despliegue:**
- [ ] Ejecuta `syncing-agent-symlinks`. **Toda edición bajo `ai-specs/` cuenta**, no solo los renombrados y los movimientos: la skill también verifica que los puntos de entrada del repositorio sigan siendo symlinks, y eso puede haberse roto sin que tú movieras nada
- [ ] Ejecuta `updating-docs` si la documentación quedó desactualizada
- [ ] Recién entonces, el commit de la skill en git

## Flujo de descubrimiento

Cómo el futuro Claude encuentra tu skill:

1. **Encuentra un problema** ("las pruebas son inestables")
2. **Busca entre las skills disponibles**
3. **Encuentra la SKILL** (la descripción coincide)
4. **Escanea el resumen** (¿es esto relevante?)
5. **Lee los patrones** (tabla de referencia rápida)

**Optimiza para este flujo** — pon los términos buscables temprano y con frecuencia.

## La conclusión

**Crear skills ES TDD para documentación de procesos.**

Misma Ley de Hierro: ninguna skill sin una prueba fallida primero.
Mismo ciclo: RED (base) → GREEN (escribir la skill) → REFACTOR (cerrar vacíos).

Si sigues TDD para el código, síguelo para las skills. Es la misma disciplina aplicada a la documentación.
