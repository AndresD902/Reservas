# Criterios de aprobación de una skill

## Contenido

| Sección | Qué trae |
|---|---|
| Ejemplo: blindar una skill de TDD | Las tres iteraciones reales hasta lograr el blindaje |
| Probando todos los tipos de skill | Cómo se prueba cada tipo —disciplina, técnica, patrón, referencia— y su criterio de éxito |
| Blindar skills contra la racionalización | Dónde aterriza cada uno de los cinco mecanismos dentro de un `SKILL.md` |
| Impacto en el mundo real | Resultados de aplicar TDD a la propia skill de TDD |

**Carga esta referencia cuando** pruebes una skill, junto con la skill `testing-with-subagents`.

`testing-with-subagents` define **cómo se corre** el ciclo RED-GREEN-REFACTOR: la construcción de escenarios de presión, los tipos de presión, la meta-prueba y el checklist. Nada de eso se repite aquí.

Este archivo define **qué cuenta como aprobado cuando el artefacto es una skill**: cómo se prueba cada tipo de skill, y cómo se blinda una skill de disciplina contra la racionalización.

## Ejemplo: blindar una skill de TDD

### Prueba inicial (falló)
```markdown
Escenario: 200 líneas listas, olvidó TDD, agotado, planes de cena
El agente eligió: C (escribir pruebas después)
Racionalización: "Pruebas después logra los mismos objetivos"
```

### Iteración 1 - agregar contramedida
```markdown
Se agregó la sección: "Por qué importa el orden"
Se volvió a probar: el agente SIGUIÓ eligiendo C
Nueva racionalización: "Espíritu, no letra"
```

### Iteración 2 - agregar principio fundamental
```markdown
Se agregó: "Violar la letra es violar el espíritu"
Se volvió a probar: el agente eligió A (borrarlo)
Citó: el principio nuevo directamente
Meta-prueba: "La skill era clara, debería seguirla"
```

**Blindaje logrado.**

## Probando todos los tipos de skill

Distintos tipos de skill necesitan distintos enfoques de prueba:

### Skills que imponen disciplina (reglas/requisitos)

**Ejemplos:** TDD, verificación antes de completar, diseñar antes de programar.

**Pruébalas con:**
- Preguntas académicas: ¿entienden las reglas?
- Escenarios de presión: ¿cumplen bajo estrés?
- Múltiples presiones combinadas: tiempo + costo hundido + agotamiento
- Identifica racionalizaciones y agrega contramedidas explícitas

**Criterio de éxito:** el agente sigue la regla bajo presión máxima.

### Skills de técnica (guías de cómo hacer algo)

**Ejemplos:** espera basada en condiciones, rastreo de causa raíz, programación defensiva.

**Pruébalas con:**
- Escenarios de aplicación: ¿pueden aplicar la técnica correctamente?
- Escenarios de variación: ¿manejan los casos límite?
- Pruebas de información faltante: ¿tienen vacíos las instrucciones?

**Criterio de éxito:** el agente aplica con éxito la técnica a un escenario nuevo.

### Skills de patrón (modelos mentales)

**Ejemplos:** reducir complejidad, ocultamiento de información.

**Pruébalas con:**
- Escenarios de reconocimiento: ¿reconocen cuándo aplica el patrón?
- Escenarios de aplicación: ¿pueden usar el modelo mental?
- Contraejemplos: ¿saben cuándo NO aplicarlo?

**Criterio de éxito:** el agente identifica correctamente cuándo y cómo aplicar el patrón.

### Skills de referencia (documentación/APIs)

**Ejemplos:** documentación de API, referencias de comandos, guías de librerías.

**Pruébalas con:**
- Escenarios de recuperación: ¿pueden encontrar la información correcta?
- Escenarios de aplicación: ¿pueden usar correctamente lo que encontraron?
- Pruebas de vacíos: ¿están cubiertos los casos de uso comunes?

**Criterio de éxito:** el agente encuentra y aplica correctamente la información de referencia.

## Blindar skills contra la racionalización

Las skills que imponen disciplina (como TDD) necesitan resistir la racionalización. Los agentes son inteligentes y encontrarán vacíos bajo presión.

**Los cinco mecanismos para cerrar un vacío están en `testing-with-subagents`, sección "Cerrar cada vacío".** No se repiten aquí. Lo que sigue es dónde aterrizan cuando el artefacto es una skill.

### Dónde va cada mecanismo en un `SKILL.md`

| Mecanismo | Su lugar |
|---|---|
| 1 · Negación explícita | Junto al enunciado de la regla que se está evadiendo |
| 2 · Entrada en la tabla de racionalizaciones | Una sección `## Racionalizaciones comunes`, o la tabla que la skill ya tenga |
| 3 · Entrada en las señales de alerta | Una sección `## Señales de alerta - DETENTE` |
| 4 · Actualizar la `description` | El frontmatter, con el síntoma de estar a punto de violar la regla |
| 5 · Principio fundamental | Temprano en el cuerpo, antes del procedimiento |

Los mecanismos 2 y 3 son los que dan a una skill sus dos secciones características de blindaje. Así se ven en una skill de disciplina:

```markdown
| Excusa | Realidad |
|--------|---------|
| "Demasiado simple para probar" | El código simple se rompe. La prueba toma 30 segundos. |
| "Probaré después" | Que las pruebas pasen de inmediato no prueba nada. |
| "Probar después logra los mismos objetivos" | Pruebas-después = "¿qué hace esto?". Pruebas-primero = "¿qué debería hacer esto?". |
```

```markdown
## Señales de alerta - DETENTE y empieza de nuevo

- Código antes de la prueba
- "Ya lo probé manualmente"
- "Probar después logra el mismo propósito"
- "Es sobre el espíritu, no el ritual"
- "Esto es diferente porque..."

**Todo esto significa: borra el código. Empieza de nuevo con TDD.**
```

## Impacto en el mundo real

De aplicar TDD a la propia skill de TDD:
- 6 iteraciones de RED-GREEN-REFACTOR para blindarla
- Las pruebas base revelaron más de 10 racionalizaciones únicas
- Cada REFACTOR cerró vacíos específicos
- GREEN final: 100% de cumplimiento bajo presión máxima
- El mismo proceso funciona para cualquier skill que imponga disciplina
