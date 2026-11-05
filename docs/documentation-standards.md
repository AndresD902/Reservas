---
description: "Estándares de forma de la documentación versionada: el catálogo de documentos de docs/ con sus disparadores de actualización, la convención de nombres, la ubicación del backlog y las reglas de formato del Markdown del repositorio."
---
# Estándares de documentación técnica

## Propósito y alcance

Este documento define cómo se escribe y se mantiene la documentación versionada del proyecto: todo lo que vive en `docs/` —modelos de datos, contratos, especificaciones de API, guías operativas y estándares—, el `README.md`, que por convención universal vive en la raíz y no en `docs/` pero se rige por estas mismas reglas, y el backlog en `backlog/`, que tiene su propia sección más abajo.

Es un documento delegado desde `docs/base-standards.md`, que es el punto de entrada del proyecto y gobierna las reglas transversales. Aquí se desarrolla en detalle un único dominio: la forma de lo que el repositorio documenta y versiona. Los artefactos que definen el comportamiento de los agentes de IA —todo lo que vive bajo `ai-specs/`— se delegan por su parte a `ai-specs/ai-specs-standards.md`.

**La política de idioma es transversal y vive en `docs/base-standards.md`.** Aplica a este documento y a todo lo que se cree bajo `docs/` y `backlog/`, tanto al crear documentación nueva como al actualizar la existente.

## Catálogo de documentación

| Patrón | Contenido | Actualizar cuando |
|---|---|---|
| `*-data-model.md` | Entidades, campos, relaciones y restricciones del dominio | Cambia el esquema, o se agregan, eliminan o renombran entidades o campos |
| `api-spec.yml` | Contrato OpenAPI de los endpoints expuestos | Cambia una ruta, un payload, un código de respuesta o el esquema de autenticación |
| `*-standards.md` | Estándares y convenciones, sea de un dominio concreto (instalación, stack, servicio) o de alcance transversal a todo el proyecto | Cambian librerías o dependencias, se agregan migraciones de base de datos, se modifica un paso del proceso de instalación, o cambia una convención transversal del equipo |
| `*-contract.md` | Contratos de integración entre componentes o con terceros | Cambia una interfaz, un evento publicado o consumido, o el acuerdo con un sistema externo |
| `*-guide.md` | Guías operativas y de uso paso a paso | Cambia un flujo operativo, un procedimiento de despliegue o un procedimiento de troubleshooting |
| `*-product-brief.md` | Definición estratégica del producto: casos de uso, usuarios objetivo, propuesta de valor, supuestos y métricas | Cambia la definición del producto, sus usuarios objetivo o su propuesta de valor |

**Nota sobre `*-product-brief.md`:** este patrón se dispara por cambios en la definición del producto, no por cambios de código. Un diff de implementación normalmente **no** lo afecta, así que el proceso de actualización no debe modificarlo salvo que la definición estratégica haya cambiado de verdad.

## Convención de nombres

El `*` en los patrones anteriores es un prefijo libre que identifica el dominio, módulo o servicio al que pertenece el documento.

- El prefijo se escribe en **inglés** y en **kebab-case**, en línea con la política de idioma para nombres de archivo.
- El sufijo (`-data-model`, `-standards`, `-contract`, `-guide`) es fijo y determina el tipo de documento.
- El prefijo también puede identificar un **alcance transversal** en lugar de un dominio concreto, cuando el documento aplica a todo el proyecto: `base-standards.md` y `engineering-standards.md` son de este tipo.
- `api-spec.yml` es un nombre fijo y no lleva prefijo.

Ejemplos válidos:

```
docs/orders-data-model.md
docs/payments-contract.md
docs/backend-standards.md
docs/deployment-guide.md
docs/api-spec.yml
```

**Todo documento de `docs/` abre con un frontmatter YAML de un solo campo**, `description`, que dice en una o dos frases qué gobierna y para qué se abre. Es lo que permite decidir si hace falta leerlo sin leerlo entero.

```yaml
---
description: "Qué gobierna este documento y cuándo se consulta."
---
```

Si un cambio no encaja en ninguno de los patrones del catálogo, propón el nuevo patrón y espera aprobación antes de crear el archivo. No inventes convenciones nuevas sobre la marcha.

## El backlog

El backlog vive en `backlog/`, en la raíz del repositorio y **fuera de `docs/`**: una historia de usuario no es un documento técnico y los patrones del catálogo no la cubren.

| Ruta | Contenido | Actualizar cuando |
|---|---|---|
| `backlog/backlog.md` | El marco del backlog —cómo interpretarlo, la iniciativa, el DoR, el DoD, las épicas, los perfiles de usuario y la tabla de seguimiento— y, a continuación, las historias completas | Entra una iniciativa nueva, se suma o se refina una historia, cambia el DoD o el DoR, o se agrega una épica |

Esa fila es la convención completa. Tres reglas la cierran:

- **`backlog.md` es el único archivo del directorio.** No existe patrón de nombre para una historia suelta, y **derivar uno de la convención general no equivale a tenerlo sancionado**: si el catálogo no lo lista, no existe. Una historia va en ese mismo archivo, después del marco, o al tablero. Si alguna vez hicieran falta archivos por historia, el patrón se propone acá y se espera aprobación, igual que cualquier otro.
- **Lo crean y lo mantienen las skills del backlog**: `drafting-backlogs` escribe el marco y las historias iniciales, y `enriching-user-stories` integra una historia en un marco que ya existe. No lo mantiene `updating-docs`, cuyo modo sincronización sigue acotado a `docs/`: un cambio en el backlog no dispara el procedimiento de sincronización documental.
- **La forma de una historia la fija `docs/trello-backlog-contract.md`.** Este documento sanciona dónde vive el archivo; aquel, qué contiene una historia.

## Cuándo actualizar

Este documento define **qué** dispara una actualización; la columna "Actualizar cuando" del catálogo es ese criterio. El **procedimiento** para ejecutarla vive en la skill `updating-docs`, que es su fuente única: no se reproduce aquí para que las dos versiones no puedan divergir.

Dos criterios que aplican por encima del catálogo:

- Un cambio que no encaja en ningún patrón se **reporta como observación** junto con el patrón propuesto. No se documenta a la fuerza en el archivo más parecido.
- Un cambio que no requiere documentación no la requiere. No se fuerza una actualización para dejar constancia de actividad.

## Formato

Estas reglas aplican a todo el Markdown del repositorio, incluido el de `ai-specs/`.

- Usa encabezados jerárquicos y consistentes: un solo `#` por archivo, `##` para secciones y `###` para subsecciones. **Excepción: el cuerpo de un agent arranca en `##`.** No lleva `#` porque no es un documento con título, es un *system prompt* que se inyecta en otra sesión, y su identidad la da el `name` del frontmatter. Su plantilla la fija la skill `writing-agents`.
- **Los encabezados van en sentence case**: solo la primera palabra en mayúscula. El Title Case es convención ortográfica del inglés y no aplica a la prosa en español. Tres excepciones: los **nombres propios** (Anthropic, Claude, Trello), la **terminología de ingeniería en inglés** que la política de idioma mantiene sin traducir (*Test Driven Development*), y los **identificadores literales**, que conservan su forma exacta y van entre backticks (`` `Criterios de Aceptación` ``). Los nombres que este harness acuña y usa como tales —la **Ley de Hierro**— cuentan como nombres propios.
- Presenta datos estructurados en tablas, no en prosa ni en listas anidadas profundas.
- Declara siempre el lenguaje en los bloques de código.
- Al editar un archivo existente, respeta su estructura actual en lugar de reorganizarla sin necesidad.
