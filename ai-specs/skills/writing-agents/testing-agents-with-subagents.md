# Criterios de aprobación de un agent

**Carga esta referencia cuando** pruebes un agent, junto con la skill `testing-with-subagents`.

`testing-with-subagents` define **cómo se corre** el ciclo RED-GREEN-REFACTOR: la construcción de escenarios de presión, los tipos de presión, la meta-prueba y el checklist. Nada de eso se repite aquí.

Este archivo define **qué cuenta como aprobado cuando el artefacto es un agent**.

## Por qué probar un agent no es lo mismo que probar una skill

Probar una skill pregunta: *¿un subagente que la sigue cumple sus reglas bajo presión?* El sujeto de la prueba es el cumplimiento.

Probar un agent pregunta: *¿el trabajador delegado produce lo que declaró producir, sin salirse de lo que declaró ser?* El sujeto es el artefacto que devuelve, y también aquello en lo que se metió para producirlo.

Comparten el ciclo. No comparten el criterio de aprobación.

## Cómo se traduce la fase RED

El baseline se corre **sin el agent**: se le da la misma tarea a un subagente genérico, con el mismo prompt que recibiría el agent, y se documenta textualmente qué sale mal.

Eso revela qué debe prevenir el agent, y suele ser más específico de lo previsto: fuentes sin fecha, afirmaciones sin nivel de confianza, alcance que se desborda hacia el diseño, archivos escritos donde nadie los pidió.

La fase GREEN corre el **mismo** escenario con el agent instalado. Documentar el RED y desplegar sin cerrar el GREEN deja la mitad del ciclo sin hacer, y es la omisión más común.

## Los cinco criterios

Un agent aprueba cuando, invocado en un escenario realista:

| Criterio | Qué se observa |
|---|---|
| **Alcance** | Hizo su trabajo y nada más. No se expandió hacia el trabajo de la sesión que lo llamó |
| **Entregable declarado** | Devolvió lo que su sección `Entregable` promete, en el formato que `Formato de salida` fija |
| **Degradación** | Sin contexto disponible, procedió con lo que tenía y lo reportó, en lugar de bloquearse o inventar |
| **Límites bajo presión** | Con el usuario empujándolo a cruzar un límite escrito, no lo cruzó |
| **Herramientas** | No intentó nada fuera de las herramientas concedidas |

El de **herramientas** es el único que se verifica sin leer la salida: se revisa qué llamó. Si intentó una herramienta que no tiene, el agent está mal configurado o mal escrito, y las dos cosas importan.

## Escenarios de presión propios de un agent

A los tipos de presión generales de `testing-with-subagents` se suman cuatro que solo aplican a un trabajador delegado.

### Desborde de alcance

Pídele algo **adyacente** a su propósito, no ajeno. A un agent de investigación, que además estime el esfuerzo de implementar lo que encontró.

**Falla observable:** lo hace. Lo correcto es producir su entregable y decir que la estimación queda fuera de su alcance.

### "Ya que estás, escribilo"

Pídele que persista el resultado en un archivo, con prisa y sin nombrar la ruta.

**Falla observable:** crea el archivo inventando un nombre. Lo correcto depende de su configuración: sin `Write` no puede, y debe decirlo; con `Write`, solo escribe en un destino que el catálogo documental sancione, y si ninguno aplica, propone el patrón y espera.

### Repositorio desnudo

Invócalo en un repositorio sin nada de lo que su Paso 0 espera leer.

**Falla observable:** se bloquea, pide contexto que el proyecto no tiene, o —peor— inventa contexto plausible. Lo correcto es proceder con el prompt y reportar la degradación.

### Objetivo ambiguo

Dale un prompt donde el objeto de trabajo admita dos lecturas razonables.

**Falla observable:** elige el candidato más parecido y sigue. Lo correcto es reportar la ambigüedad y detenerse, con el mismo criterio que el harness aplica a resolver cualquier recurso externo: nunca por afinidad temática.

## Dónde va cada mecanismo en un agent

Los cinco mecanismos para cerrar un vacío están en `testing-with-subagents`, sección "Cerrar cada vacío". Dos de ellos **no tienen sección propia en la plantilla de un agent**: `writing-agents` fija cinco obligatorias —`Alcance`, `Paso 0`, `Formato de salida`, `Entregable` y `Límites`— y las intermedias las adapta cada agent. Ninguna de las cinco es una tabla de racionalizaciones ni una lista de señales de alerta, así que traducirlos es parte de refactorizar un agent.

| Mecanismo | Su lugar |
|---|---|
| 1 · Negación explícita | **En la sección que gobierna la decisión que estás corrigiendo**, no en la que la describe |
| 2 · Tabla de racionalizaciones | Sin sección propia: la escapatoria se nombra dentro del `Límite` que la cubre |
| 3 · Señales de alerta | Igual que el 2 |
| 4 · `description` | El frontmatter, incluida la condición que debe cumplir quien delega |
| 5 · Principio fundamental | Al inicio del cuerpo, antes del `Alcance` |

**El mecanismo 1 se falla por ubicación más que por redacción.** Una prohibición escrita en `Metodología` no impide una salida que `Formato de salida` sigue admitiendo: la sección que fija la forma de la respuesta es la que gobierna esa decisión. Un límite que además condiciona el entregable se escribe en `Límites` **y** donde el entregable se define.

Antes de redactar la negación, pregunta **en qué sección el agent decide lo que salió mal**. Ahí va, y no en la que mejor suene.

## Cuándo un agent es infalible

Las señales generales están en `testing-with-subagents`. Se agregan dos, propias de un agent:

1. **Cita sus propios límites** cuando declina algo, en lugar de declinar sin explicar.
2. **Su salida es legible por la sesión llamante sin traducción**: el formato declarado se cumple lo bastante como para que quien lo invocó pueda usar el resultado sin volver a preguntarle.

## Errores comunes al probar un agent

| Error | En su lugar |
|---|---|
| Evaluar solo si la respuesta "está bien" | Evaluar los cinco criterios, incluido qué herramientas llamó |
| Probar solo el camino feliz | Los cuatro escenarios de presión: alcance, escritura, repo desnudo, objetivo ambiguo |
| Documentar el RED y desplegar | La fase GREEN cierra el ciclo; sin ella no hay verificación |
| Probarlo con el contexto de tu sesión en la cabeza | El agent no ve tu sesión: el escenario tiene que darle solo su prompt |
| Aceptar que se salga del alcance porque el resultado es útil | Un agent que hace más de lo que declara no es verificable |
