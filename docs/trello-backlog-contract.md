---
description: Contrato de integración con Trello para el backlog ágil. Define el esquema normativo de una card (título, 3 Ces, checklists, labels) y el flujo de listas. No fija un tablero específico — el tablero es dato de contexto de cada proyecto.
---
# Contrato de backlog en Trello

## Propósito y alcance

Este documento define la forma que debe tener una card de Trello para que la capa agéntica pueda leerla, evaluarla y escribirla de forma confiable. Es el contrato de integración entre las skills del harness y Trello como sistema de terceros.

Aplica a las cards que representan historias de usuario (HU) del backlog. No aplica a cards de uso personal ni a tableros ajenos al backlog del producto.

El esquema es **agnóstico al dominio**: aplica igual si la historia es de software, de configuración de un entorno (Google Workspace, cloud) o de un proceso sin componente técnico. Lo que cambia entre proyectos es el contenido de la historia, no la forma de la card.

## Tablero de trabajo

Este contrato **no fija ningún tablero específico**. Es una pieza del harness base y debe funcionar en cualquier proyecto que lo adopte; nombrar aquí un tablero concreto acoplaría el contrato a un proyecto particular.

Cualquier skill que necesite operar sobre un tablero real lo resuelve en este orden, sin excepción:

1. **Argumento explícito** dado por el usuario en el prompt (id, URL o nombre del tablero).
2. **Configuración del proyecto**, si el documento de estándares base (`docs/base-standards.md`) tiene una sección `## Integraciones` con el tablero de backlog fijado.
3. **Pregunta directa al usuario** si no hay ni argumento ni configuración.

**Nunca se infiere un tablero por afinidad temática o de nombre.** Si hay varios tableros candidatos y ninguno se identifica con claridad, se pregunta; no se elige el más parecido.

## Título de la card

```
HU-NN · Título descriptivo de la historia
```

`NN` es un consecutivo de dos dígitos, empezando en `01`. El separador es `·` (punto medio), rodeado de espacios.

## Metodología de las 3 Ces

La descripción de la card implementa **Card** y **Conversation**; la tercera "Confirmation" se formaliza como checklist, no como texto libre.

1. **Card** — bajo un encabezado `**Card**`, un blockquote con la historia:
   ```
   > Como <rol>, quiero <capacidad>, para <beneficio>.
   ```
2. **Conversation** — bajo un encabezado `**Conversación**`, una lista de bullets con el contexto, las decisiones tomadas y los trade-offs relevantes. Tan extensa como haga falta para que quien la lea no tenga que preguntar lo básico, dentro del límite de la sección siguiente.
3. **Confirmation** — es la checklist `Criterios de Aceptación` (ver abajo), no una sección de la descripción.

### Límite de longitud de la descripción

**La descripción de una card admite a lo sumo 2048 caracteres.** Es un límite de Trello, no una preferencia de estilo: una escritura que lo excede es rechazada por la API, no truncada.

La consecuencia práctica es que **Card** y **Conversación** comparten ese presupuesto. Cuando el contenido no cabe:

- Se comprime la **Conversación**, nunca la **Card**: la historia es la pieza normativa.
- Se conserva el porqué de cada decisión y se recorta la redundancia, no los bullets completos.
- Si el contexto genuinamente no cabe, el excedente va como comentario en la card y la descripción lo referencia. No se sacrifica la estructura de las 3 Ces para ganar espacio.

Las checklists no comparten este presupuesto: cada ítem tiene su propio límite, muy superior al de la descripción.

## Checklists

Toda card lleva exactamente estas dos checklists, con estos nombres exactos y **en este orden**: `Tareas` primero, `Criterios de Aceptación` después. En el orden de lectura natural de la card, de arriba hacia abajo, `Tareas` debe aparecer primero.

El orden es normativo, no incidental: al crearlas vía MCP hay que fijar `pos` en secuencia (`1` para `Tareas`, `2` para `Criterios de Aceptación`), porque sin posición explícita el orden queda indeterminado.

### `Tareas`

Acciones concretas para completar la historia. Cada ítem empieza con un verbo en infinitivo: *"Crear...", "Validar...", "Implementar..."*.

### `Criterios de Aceptación`

Formaliza la tercera C (Confirmation). Cada ítem sigue el formato Gherkin en español:

```
Dado <contexto>, cuando <acción>, entonces <resultado esperado>.
```

No se incluye una checklist separada de Definición de Terminado por card. Si el equipo necesita un DoD transversal, vive en un documento aparte — no es parte de este contrato.

## Labels

Cada card lleva exactamente un label de épica, con este formato:

```
EP-NN: Título descriptivo de la fase
```

`NN` es un consecutivo de dos dígitos. El separador es `:` (dos puntos). Las épicas no son una lista cerrada: se definen incrementalmente a medida que se planea el backlog, el sprint o la iniciativa.

## Miembro asignado

La norma es **un miembro asignado por card**. Se permite más de uno solo cuando el trabajo es explícitamente conjunto entre varias personas, o cuando la card retoma algo que ya cerraron varias personas y se necesita conservar esa trazabilidad — es la excepción, no la regla.

Una card recién creada y todavía sin priorizar puede no tener miembro asignado.

## Listas del tablero y flujo de estados

| Lista | Significado | Quién la mueve |
|---|---|---|
| `Backlog` | Registrado, sin refinar | Producto |
| `Sprint Backlog` | Refinado y comprometido para el sprint actual | Producto / equipo |
| `En Progreso` | En implementación | Equipo |
| `Bloqueado` | Detenido por una dependencia externa | Equipo |
| `En Revisión` | Pendiente de revisión por pares o validación funcional | Equipo |
| `Completado` | Terminado en este sprint, pendiente de puesta en operación | Equipo |
| `En Producción` | En operación real: desplegado, publicado o puesto en marcha | Equipo |

### Transición permitida a la capa agéntica

Una skill puede mover una card de `Backlog` a `Sprint Backlog` cuando termina de enriquecerla y cumple este contrato, y solo en ese sentido. Cualquier otra transición requiere decisión humana explícita.

## Precedencia frente al tablero real

Este contrato es normativo: describe cómo debe quedar una card, no necesariamente cómo está hoy un tablero concreto. Cuando el tablero real no cumple el contrato:

- El **contrato manda sobre el contenido**: la estructura de descripción, el formato de título y las checklists se aplican igual.
- El **tablero manda sobre lo ejecutable**: no se inventan listas ni labels que no existen. Una transición hacia una lista inexistente no se ejecuta.
- La brecha se **reporta** al usuario, sin resolverla por iniciativa propia.

## Fallo parcial de escritura

La sección anterior gobierna la no conformidad que **ya estaba** en el tablero. Esta gobierna la que produce la propia capa agéntica al escribir.

Dejar una card conforme son **varias llamadas y no hay transacción**: primero la card, después cada checklist en su posición. Entre una y otra existe un estado intermedio visible para el equipo, y cualquiera de ellas puede fallar.

- **La card es la unidad.** Una card a la que le falta una checklist es no conforme, pero es un artefacto válido y recuperable: se completa, no se destruye.
- **Borrar y archivar no son operaciones de la capa agéntica**: ni para limpiar su propio estado parcial, **ni a pedido explícito del usuario**. Son una operación humana, fuera de esta capa y aparte del enriquecimiento. Destruir una card que el usuario aprobó crear es una escritura que nadie pidió, y el equipo ya puede haberla visto.
- Esta prohibición es **deliberadamente inmune a la cadena de precedencia** de `docs/base-standards.md`, donde una instrucción explícita del usuario gana por norma general: aquí no la desplaza, porque la regla está escrita contra ese mismo pedido. Quien quiera borrar la card lo hace en Trello, y así queda registrado como decisión humana.
- **Ante un error transitorio** —límite de tasa, timeout— cabe **un solo reintento**, tras una espera breve que respete la indicación de la API si la da. Si vuelve a fallar, o si el error es de contenido, se detiene y se reporta. Nunca un bucle de reintentos.
- **Una aprobación cubre exactamente los objetos que se aprobaron.** Completar más tarde lo que faltó no necesita una aprobación nueva; cualquier cosa distinta sí.
- El estado parcial se **reporta** con lo que existe y lo que falta, igual que cualquier otra brecha.

## Reglas para la capa agéntica

- **Resolver el tablero** siempre con el orden de tres pasos de la sección "Tablero de trabajo". Nunca inferir por afinidad temática.
- **Nunca** escribir en una card sin que el usuario lo haya pedido explícitamente.
- Al escribir de vuelta, conservar el contenido original y agregar el enriquecido después, sin sobrescribir.
- **Verificar la longitud de la descripción antes de escribirla.** Si supera los 2048 caracteres, comprimirla según la sección "Límite de longitud de la descripción" y reportar al usuario qué se recortó respecto a lo entregado en pantalla.
- Lo que no se pueda derivar de la card ni del contexto del proyecto se marca como supuesto pendiente de confirmar o como indeterminado. No se inventan criterios de aceptación ni datos técnicos u operativos.
- El contenido se escribe en español, según la política de idioma del proyecto.
- **Nunca** mover una card de lista sin preguntar: es un efecto visible para el equipo y no va implícito en escribir la card.
- **Nunca** borrar ni archivar una card: no está entre las operaciones de la capa agéntica, ni para deshacer un fallo propio. Ver "Fallo parcial de escritura".
- Una escritura de varias llamadas que falla a medias **se reporta y se completa**, no se revierte.
- Una historia que se persiste como archivo va al backlog, no a `docs/`. En qué archivo exactamente lo fija `docs/documentation-standards.md`, sección "El backlog": este contrato gobierna la forma de una historia, no dónde vive.

## Estado de este contrato

Este esquema fue validado contra la práctica real de un tablero de backlog en producción, sin que ese tablero quede identificado en este documento — solo su patrón de estructura informó estas reglas. Sigue abierto a iteración: la taxonomía de labels de épica y el detalle del flujo de listas pueden cambiar según lo que el equipo decida.
