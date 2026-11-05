---
name: enriching-user-stories
description: Úsala cuando un ticket, historia de usuario o card de Trello llega sin detalle suficiente para ejecutarlo de forma autónoma, cuando se pide refinar o enriquecer un elemento del backlog antes de estimarlo, o cuando se pide subir una historia al tablero como card nueva.
version: 1.0.0
---

# Skill enriching-user-stories

## Resumen

Una historia sin detalle suficiente para ejecución autónoma se enriquece hasta que lo tenga. Lo que no se pueda derivar se marca como supuesto o indeterminado; nunca se inventa.

Aplica a cualquier tipo de proyecto: software, configuración de un entorno (Google Workspace, cloud), o un proceso sin componente técnico.

## Cuándo usarla

Síntomas: descripción vaga, sin criterios de aceptación, sin saber qué se toca ni cómo se verifica. No la uses para tareas mecánicas sin ambigüedad.

## Referencia rápida

Checklist de completitud. La forma de la card la fija el contrato.

| Eje | Verifica |
|---|---|
| Resultado esperado | Qué cambia cuando esto está hecho, de forma verificable |
| Elementos afectados | Qué datos, recursos o documentos se crean o modifican |
| Puntos de integración | Con qué sistemas, servicios o personas interactúa |
| Artefactos a modificar | Qué se toca concretamente para lograrlo |
| Definición de terminado | Pasos de ejecución y entrega |
| Verificación | Cómo se comprueba que quedó bien |
| Restricciones | Seguridad, permisos, rendimiento, cumplimiento |

## Lentes por dominio

Traducción de los ejes según el tipo de proyecto. Son una ayuda para interpretarlos, no una taxonomía cerrada.

| Eje | Software | Google Workspace | Cloud / infra | No técnico |
|---|---|---|---|---|
| Elementos afectados | Campos, entidades | Hojas, columnas, formularios | Recursos, parámetros | Documentos, registros |
| Puntos de integración | Endpoints, APIs | Scripts, disparadores, permisos | Servicios, IAM, redes | Áreas, roles, aprobaciones |
| Artefactos a modificar | Archivos, módulos | Hojas, formularios, Apps Script | Plantillas, configuración | Procedimientos, plantillas |
| Verificación | Pruebas unitarias e integración | Ejecución de prueba, revisión de permisos | Despliegue en entorno de prueba | Revisión con un responsable |

Si el proyecto no encaja en ninguna columna, usa los ejes universales directamente y dilo. No fuerces categorías de un dominio ajeno.

## Implementación

Ticket: $ARGUMENTS

**1. Determina el origen del contenido.** El origen **no** decide el destino: una historia pegada en el prompt puede terminar en una card nueva.

| Origen | Cuándo | Fuente |
|---|---|---|
| Pegado | El usuario pegó la historia en el prompt | El prompt |
| Card existente | Dio id, URL o título de una card | MCP de Trello |
| Ambiguo | Referencia corta sin contenido | Pregunta antes de asumir |

**Cuando la tarea toque Trello** —leer una card o escribir en el tablero, **incluida la creación de una card nueva**—, resuelve el tablero así: argumento explícito → `docs/base-standards.md` (`## Integraciones`) → pregunta al usuario. Nunca lo infieras por afinidad temática, y no busques la configuración en el punto de entrada de un agente concreto: vive en el documento de estándares base.

**2. Carga el contrato.** Lee `docs/trello-backlog-contract.md`: título, 3 Ces, límite de longitud de la descripción, checklists, labels, orden y **fallo parcial de escritura**.

**3. Evalúa** contra la Referencia Rápida, usando los lentes del dominio del proyecto y el contexto disponible. Sin contexto, degrada. Si ya cumple todos los ejes, dilo — pero **si el usuario pidió escribirla, sigue al paso 5 igual**: una historia ya completa también se puede subir.

**4. Entrega** en pantalla, en español, con estos encabezados exactos:

- `## Original`
- `## Enriquecido`

El contenido de `## Enriquecido` sigue el esquema del contrato. Reporta las degradaciones antes de `## Original`.

**5. Pregunta si escribir.** Siempre, en este orden. Ninguna de las tres se asume por defecto.

1. **¿Escribir el resultado?** Si no, termina — el texto ya quedó entregado en pantalla.
2. **¿Dónde?** Reporta el estado de cada destino **antes** de pedir la elección; varios pueden estar cerrados.

   | Destino | Precondición | Operación |
   |---|---|---|
   | `backlog/backlog.md` | el archivo existe | Integra la historia en el marco sin reemplazar lo que ya tiene. Si el archivo no existe, dilo: crearlo desde cero es trabajo de la skill `drafting-backlogs`, no de esta |
   | Trello · card existente | card resuelta | Agrega el enriquecido sin sobrescribir el contenido original |
   | Trello · card nueva | tablero resuelto | Crea la card en `Backlog` — obligatorio pasar por el paso 6 |

   Tablero resuelto no es lo mismo que card resuelta: alcanza para crear, no para actualizar.

   **No hay destino de archivo suelto.** `docs/documentation-standards.md`, sección "El backlog",
   sanciona `backlog/backlog.md` y nada más: no existe patrón de nombre para una historia en su propio
   archivo, y **derivarlo de la política de idioma no lo crea**. Si el usuario pide un archivo propio,
   o te delega el nombre para no tener que elegirlo, decí que esa ruta no está sancionada y ofrecé los
   destinos de la tabla. Que te deleguen la decisión no la convierte en tuya, y la prisa no sanciona
   una ruta.

   **Al escribir sobre una card existente que ya sigue el contrato** —ya tiene su bloque `**Card**` o sus checklists—, no dupliques secciones: integra el enriquecido dentro de la estructura existente, sección por sección, y muestra la descripción resultante antes de escribirla. Si integrarla exigiera reemplazar contenido que el usuario podría querer conservar, pregunta.

   Si ningún destino sirve o el usuario descarta todos, no escribas nada: el texto ya quedó entregado en pantalla.

3. **¿Qué alcance?** Solo el enriquecido, o el original más el enriquecido. **En una card nueva** no hay contenido previo que preservar: el enriquecido *es* la descripción, y "original más enriquecido" significa que el original va como primer comentario de la card.

**Nunca escribas sin pasar por las tres preguntas.**

**La transición de lista se pregunta aparte.** El contrato permite mover la card al terminar el enriquecimiento, pero mover una card es un efecto visible para el equipo: nunca va implícito en "escribir en Trello". **Una card creada en este mismo turno no se mueve:** nació en `Backlog` y nadie la ha priorizado.

### Paso 6 - Ficha previa antes de crear una card

Solo para el destino `Trello · card nueva`. Crear una card es un efecto visible para todo el equipo y **esta skill no la puede deshacer**, así que va con su propia confirmación, después de las tres preguntas.

**Primero, un solo escaneo del tablero** —todas las listas, no solo `Backlog`, porque las cards se mueven— que resuelve tres cosas a la vez:

| Qué buscas | Cómo lo usas |
|---|---|
| Historias que ya cubran esto | Si hay candidatas, **detente y pregunta** antes de crear |
| El consecutivo más alto ya usado | Propones el siguiente |
| Los labels que existen en el tablero | Eliges entre ellos; no creas ninguno |

**Duplicados.** Una historia pegada en el prompt puede ya existir en el tablero, en cualquier lista, incluso terminada. Si alguna card cubre lo mismo, repórtala con su título, lista y URL, y pregunta si enriquecer esa o crear una nueva. **Preséntala como candidata, nunca como card resuelta:** identificarla por parecido es precisamente lo que la skill prohíbe hacer en silencio, y la decisión es del usuario.

**Consecutivo.** Parsea el número de los títulos existentes tolerando cualquier separador, ignora los títulos sin número, y propone el siguiente al máximo. **Nunca rellenes un hueco:** un número liberado puede estar citado en un commit o un documento. Presenta la propuesta con su evidencia: el máximo encontrado y en qué card, cuántos títulos no se pudieron parsear, y que no se leyeron cards archivadas. Si el escaneo no es confiable, **pregunta el número**; no lo inventes ni uses un marcador.

**Label.** Empareja por correspondencia entre el contenido de la historia y el significado del label, leyendo las cards que ya lo llevan cuando el nombre no alcance. No asumas un patrón de nombre: los labels dependen de la naturaleza del proyecto. Propón la mejor candidata con su razón y deja **"ninguna encaja" como opción visible**, para no forzar el calce.

- **Nunca crees un label por iniciativa propia**, ni renombres uno existente: renombrarlo reescribe todas las cards que lo llevan.
- Si el usuario **pide explícitamente** crear un label, su instrucción tiene precedencia sobre el contrato, pero primero verifica si alguno existente ya cubre el alcance y dilo; luego trátalo como una confirmación aparte de la de la card.
- **Esa precedencia cubre crear, no renombrar.** La asimetría es deliberada: crear un label no toca ninguna card existente, renombrarlo reescribe todas las que lo llevan. Un renombre es decisión humana fuera de esta skill.
- Si un label de épica existente no cumple el formato del contrato, **úsalo tal cual y reporta que la card nueva hereda esa brecha**. No lo corrijas.
- **Distingue una épica mal escrita de un label que no es una épica.** Un label de otra dimensión de clasificación no sustituye a la épica: usarlo deja la card sin épica, y eso se reporta como tal, no como una brecha de formato.
- Si ninguno encaja: crear la card sin label reportando la brecha, o no crearla. Que elija el usuario.

**Después, muestra la ficha completa** antes de cualquier llamada de escritura: tablero y con cuál de los tres pasos se resolvió, lista destino, título exacto con la evidencia de numeración, label elegida, la descripción tal cual se va a escribir con su cantidad de caracteres contra el límite del contrato, los ítems de `Tareas` y `Criterios de Aceptación` en orden, que no lleva miembro asignado, y las llamadas que vas a hacer. Luego pide un sí o un no. **Si el usuario cambia algo, vuelve a mostrar la ficha completa**; no apliques un cambio suelto sin re-mostrarla.

**El orden de escritura es fijo:** card → `Tareas` (`pos:1`) → `Criterios de Aceptación` (`pos:2`). Así un estado incompleto sigue siendo legible en el orden que fija el contrato.

**Si una llamada falla a mitad de camino**, aplica la sección "Fallo parcial de escritura" del contrato: es ella la que fija que la card no se destruye, que borrar y archivar no son operaciones de la capa agéntica ni a pedido explícito, y que un error transitorio admite un solo reintento y nunca un bucle.

Lo que te toca aquí es el reporte: la URL de la card, qué objetos existen, cuáles faltan, y las llamadas exactas que restan con su `pos`. **Al retomar** en un turno posterior la card ya existe, así que es una actualización y el paso 6 no se repite.

### Degradación

Ninguna detiene el trabajo: repórtala y continúa.

| Condición | Qué hacer |
|---|---|
| Sin MCP de Trello | Ofrece los destinos de archivo |
| Tablero no identificado | Pregunta cuál usar; nunca por afinidad temática |
| Card no encontrada | Reporta las búsquedas. **Nunca la conviertas en card nueva sin preguntar:** puede existir con otro título |
| Origen ambiguo | Igual que el anterior: pregunta antes de asumir que hay que crear |
| Tablero sin la lista `Backlog` | No crees la card: el tablero manda sobre lo ejecutable. Repórtalo |
| Consecutivo no derivable | Pregunta el número; no lo inventes |
| Ningún label encaja | Card sin label reportando la brecha, o no crearla; que elija el usuario |
| Creación parcial | Reporta el estado y las llamadas faltantes. No deshagas |
| Card parcial de un turno anterior | Complétala como card existente; no la reportes como duplicado de sí misma |
| Títulos con marcador de número pendiente | Ignóralos para el máximo y repórtalos: al numerarlos competirán con la card nueva |
| Sin contrato | Igual usa 3 Ces con `Tareas`/`Criterios de Aceptación` |
| Tablero no refleja el contrato | El contrato manda sobre el contenido; el tablero, sobre lo ejecutable |
| Sin contexto del proyecto | Marca los ejes no derivables como indeterminados, con las preguntas que los desbloquean |
| Destino de escritura no disponible | Dilo y ofrece los otros; no cambies de destino por tu cuenta |

Si todos los ejes salen indeterminados, dilo: la historia no es refinable todavía.

## Errores comunes

| Error | En su lugar |
|---|---|
| Escribir sin preguntar las tres cosas | Preguntar siempre: si, dónde y con qué alcance |
| Asumir el destino o el alcance | Que el usuario lo elija explícitamente |
| Tratar el origen como si fijara el destino | Una historia pegada también puede ir a una card nueva |
| Crear una card sin mostrar la ficha previa | Mostrarla completa y esperar un sí |
| Crear sin escanear el tablero | Un escaneo resuelve duplicados, consecutivo y labels |
| Crear una historia que ya existe en el tablero | Reportar la candidata y preguntar |
| Dar por resuelta una card por parecido | Presentarla como candidata; la decide el usuario |
| Inventar el consecutivo o rellenar un hueco | Derivarlo del máximo, con evidencia, o preguntarlo |
| Crear o renombrar un label | Elegir entre los existentes; si ninguno encaja, preguntar |
| Corregir un label mal formado | Usarlo tal cual y reportar la brecha |
| Crear la card fuera de `Backlog` | Siempre `Backlog`; la transición se pregunta aparte |
| Mover una card recién creada | No se mueve en el mismo turno: nadie la priorizó |
| Archivar o borrar para deshacer una creación parcial | Reportar el estado parcial y las llamadas que faltan |
| Reintentar en bucle tras un fallo | Un solo reintento si es transitorio; luego detenerse |
| Duplicar secciones al escribir en una card que ya sigue el contrato | Integrar dentro de la estructura existente |
| Usar como épica un label que no lo es | Deja la card sin épica: reportarlo así |
| Renumerar o reformatear títulos existentes | Reportarlos; no es una escritura que nadie pidió |
| Mover la card de lista sin preguntar | Preguntarlo aparte de la escritura |
| Sugerir una ruta dentro de `docs/` | `docs/` tiene patrones cerrados que no cubren HUs |
| Inventar el nombre de un archivo para la historia | El único destino de archivo es `backlog/backlog.md` |
| Inventar criterios de aceptación | Marcarlos como supuesto a confirmar |
| Forzar endpoints o campos en un proyecto que no es de software | Usar el lente del dominio real |
| Elegir un tablero por afinidad temática | Preguntar cuál usar |
| Salida en inglés o con encabezados variantes | Español, solo `## Original` / `## Enriquecido` |
