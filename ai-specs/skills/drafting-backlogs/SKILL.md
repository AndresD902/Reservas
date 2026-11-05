---
name: drafting-backlogs
description: Úsala cuando haya que convertir una propuesta, un solution design, un diagrama de flujo o un documento equivalente en un backlog inicial. También cuando una iniciativa nueva deba sumar épicas e historias a un backlog que ya existe, o cuando se pida armar el marco de un backlog desde cero.
version: 1.0.0
---

# Skill drafting-backlogs

## Resumen

Convierte un documento que describe **qué hay que construir o cambiar** en dos entregables: el
**marco del backlog** y una **propuesta de historias de usuario**.

El resultado es un borrador para revisión humana, no un backlog terminado. Lo que no se derive del
documento fuente se marca como supuesto o como indeterminado, nunca se inventa.

Aplica a cualquier tipo de proyecto: software, configuración de un entorno, o un proceso sin
componente técnico.

## Principio

**Cada historia se sostiene sola.** Un backlog es un conjunto de piezas negociables, no un plan de
ejecución encadenado. En el momento en que una historia declara que depende de otra, el equipo pierde
la libertad de priorizar, y el backlog deja de ser un backlog para volverse un cronograma disfrazado.

## Cuándo usarla

Cuando existe un documento de partida y no existe todavía un backlog derivado de él, o cuando una
iniciativa nueva tiene que integrarse a uno existente.

No la uses para refinar **una** historia: eso es `enriching-user-stories`.

## Qué no hace esta skill

| Tarea | Dónde vive |
|---|---|
| La forma de una historia: título, 3 Ces, checklists, labels, límites de longitud | `docs/trello-backlog-contract.md` |
| Refinar una historia individual, o crear la card en el tablero | La skill `enriching-user-stories` |
| Resolver cuál es el tablero de trabajo | `docs/base-standards.md`, sección `## Integraciones` |
| La definición estratégica del producto: mercado, propuesta de valor, métricas | El agent `product-strategy-analyst`, que entrega un `*-product-brief.md` |

**Ninguno de esos contenidos se reproduce aquí.** Se referencian.

## Las dos salidas

### 1 · El marco, en `backlog/backlog.md`

Siete secciones, en este orden. Es la estructura, no una sugerencia: un marco que varía en cada
generación no sirve para comparar iniciativas ni para que alguien sepa dónde buscar.

| # | Sección | Contenido |
|---|---|---|
| 1 | Cómo interpretar este documento | Qué gobierna este archivo y qué gobierna el tablero; a qué documento se remite la forma de una card |
| 2 | La iniciativa | El problema observable de hoy, qué entrega la iniciativa, y qué queda **fuera de alcance** declarado |
| 3 | Definición de Listo (DoR) | Qué debe cumplir una historia para entrar a un sprint |
| 4 | Definición de Terminado (DoD) | Qué debe cumplirse para dar una historia por cerrada |
| 5 | Épicas | Tabla de labels `EP-NN: Título` con el alcance de cada una |
| 6 | Perfiles de usuario | Quién es cada rol y qué necesita del producto |
| 7 | Tabla de seguimiento | Una fila por historia, con su épica y su estado en el tablero |

**Las historias completas van en el mismo archivo, después de la séptima sección**, cada una bajo su
propio encabezado. La tabla de seguimiento es un índice —una fila por historia—, no la historia: si
el marco fuera lo único que el archivo contiene, persistir en archivo perdería el contenido.

**El DoD y el DoR se derivan de este proyecto.** Una lista genérica copiada de una plantilla no
significa nada: si el DoD dice "hay pruebas" en un proyecto sin código, o si el DoR no menciona los
indeterminados que bloquean historias reales, el documento es decorativo. Se construyen a partir del
documento fuente y de los estándares vigentes del repositorio.

**Si `backlog/backlog.md` ya existe, se extiende, no se reemplaza.** El DoD, el DoR y los perfiles
sobreviven entre iniciativas; lo que se agrega son épicas, historias y filas de seguimiento.

### 2 · Las historias

Cumplen `docs/trello-backlog-contract.md`. Cada una nace con su label de épica y su lugar en la tabla
de seguimiento.

La numeración `HU-NN` **es provisional hasta verificarla contra el destino real**. Si las historias
van a un tablero, el consecutivo se deriva del máximo existente, con evidencia — eso lo hace
`enriching-user-stories`, no esta skill.

## La regla dura: las historias no se acoplan

Es el modo de falla por defecto al generar en lote, y el que más daño hace.

**No escribas nada que declare que una historia depende de otra.** Sin excepciones:

- No una columna "Depende de" en el índice.
- No un campo "Prerrequisito", "Bloqueada por" o "Requiere" en la card.
- No "según lo definido en HU-02" dentro de la Conversación.
- No un orden presentado como obligatorio en vez de sugerido.

Mencionar otra historia **solo para delimitar alcance** sí es correcto: *"el escalamiento por umbral
queda fuera de esta historia"*. La diferencia es que eso recorta el trabajo propio; una dependencia
condiciona el trabajo ajeno.

| Excusa | Realidad |
|---|---|
| "Es que técnicamente no se puede hacer HU-04 sin HU-02" | Puede que sí, con un dato de prueba. Y si de verdad no se puede, el equipo lo va a descubrir al planificar: es su decisión, no tuya |
| "La dependencia es información útil, no una imposición" | Escrita en la card se lee como restricción. Si el orden importa, va como orden sugerido en la tabla de seguimiento, nunca dentro de la historia |
| "Solo puse una columna informativa en el índice" | Una columna "Depende de" es una tabla de dependencias. El nombre de la columna no cambia lo que hace |
| "Marqué la dependencia pero dije que era flexible" | La advertencia no es el permiso |

**Señales de alerta — DETENTE:** estás escribiendo las palabras *depende*, *requiere*, *prerrequisito*,
*bloquea* o *después de* refiriéndote a otra HU; estás numerando historias para que el número indique
orden de ejecución; estás justificando por qué en este caso la dependencia sí hace falta.

## Implementación

**1 · Resuelve la entrada.** El documento fuente puede venir pegado en el prompt, como ruta a un
archivo, o nombrado sin contenido. En el tercer caso, pregunta: no supongas cuál es.

**2 · Determina si el marco ya existe.** Busca `backlog/backlog.md`. Si existe, lo vas a extender, y
el DoD, el DoR y los perfiles vigentes son los suyos, no unos nuevos.

**3 · Construye el marco.** Las siete secciones. Deriva el DoD y el DoR del documento fuente y de los
estándares del repositorio; si el proyecto tiene código, `docs/engineering-standards.md` aporta.

**4 · Descompón en historias.** Corta por resultado observable para un rol, no por capa técnica ni
por orden de construcción. Cada historia tiene que poder demostrarse sola.

**5 · Separa lo que no sabés.** Dos listas distintas, y la diferencia importa:

| | Qué es | Qué se hace |
|---|---|---|
| **Supuesto** | Una lectura que adoptaste para poder avanzar | Se declara, con qué cambia si es falsa |
| **Indeterminado** | Un hueco que no admite una lectura defendible | Se declara como pregunta, y bloquea el DoR de la historia que afecte |

Inventar un valor concreto para un indeterminado —un umbral, un plazo, un rol aprobador— convierte
una decisión de negocio en una decisión tuya. No lo hagas.

**6 · Entrega para revisión.** Todo en el chat: el marco y las historias. **No escribas nada
todavía.**

**7 · Pregunta el destino.** Tres opciones, y son del usuario:

| Destino | Cómo |
|---|---|
| Archivo | `backlog/backlog.md`, y nada más: el marco, y las historias completas a continuación |
| Tablero | **REQUIERE:** `enriching-user-stories`, que resuelve el tablero, verifica la numeración y crea las cards |
| Nada | El borrador queda en la conversación |

**Un archivo por historia no existe.** `docs/documentation-standards.md`, sección "El backlog",
sanciona `backlog/backlog.md` y nada más. No hay patrón de nombre para una historia suelta, y
**derivar uno de la convención general no equivale a tenerlo sancionado: si el catálogo no lo lista,
no existe.**

| Excusa | Realidad |
|---|---|
| "El usuario pidió las historias en archivos separados" | Pedirlos no crea el patrón. Entregá el marco y decí que un archivo por historia no está sancionado |
| "El nombre lo derivé de la política de idioma: inglés, kebab-case" | Eso te da la *forma* de un nombre, no el permiso para usarlo. La política de idioma no es un catálogo |
| "No hay convención nueva que registrar" | Si estás eligiendo cómo se llama un archivo, estás creando una convención. Esa frase es la señal de alerta, no la exención |
| "Un cambio que no requiere documentación no la requiere" | Cierto y fuera de lugar: el que inventa una ruta no es un cambio que *no* requiere documentación, es uno que requiere aprobación previa |
| "Me delegó el nombre y tenía apuro" | La prisa no sanciona una ruta. Escribí `backlog/backlog.md` y reportá qué quedó adentro |

**Señales de alerta — DETENTE:** estás componiendo el nombre de un archivo de historia; estás
justificando ese nombre con la política de idioma; vas a crear un directorio que el catálogo no
nombra; escribiste que no hay convención que registrar.

## Degradación

| Situación | Qué hacer |
|---|---|
| El documento fuente es demasiado vago para descomponer | Dilo, y entrega lo que sí se deriva: la iniciativa y los indeterminados. No rellenes con historias genéricas |
| No hay estándares de dominio en el repositorio | El DoD se apoya solo en lo que exista. Un DoD corto y verdadero vale más que uno largo e inventado |
| El usuario pide subir todo al tablero sin revisar | El borrador es una propuesta: pídele que confirme al menos las épicas y el recorte antes de crear cards |
| Ya existe un backlog con otra estructura | No lo reescribas para que calce con las siete secciones. Repórtalo y pregunta |

## Errores comunes

| Error | En su lugar |
|---|---|
| Declarar dependencias entre historias | Cada historia se sostiene sola; el orden sugerido va en la tabla de seguimiento |
| DoD y DoR de plantilla genérica | Derivados de este proyecto y de sus estándares vigentes |
| Inventar el valor de un indeterminado | Declararlo como pregunta que bloquea el DoR |
| Reproducir el esquema de la card | Referenciar `docs/trello-backlog-contract.md` |
| Escribir archivos o cards sin preguntar | Entregar en el chat y preguntar el destino |
| Inventar el nombre de un archivo por historia | El único destino de archivo es `backlog/backlog.md` |
| Reemplazar un `backlog.md` existente | Extenderlo: el DoD, el DoR y los perfiles sobreviven |
| Cortar las historias por capa técnica | Cortar por resultado observable para un rol |
| Dar la numeración `HU-NN` por definitiva | Es provisional hasta verificarla contra el destino real |
