---
name: syncing-agent-symlinks
description: Úsala ante cualquier cambio bajo ai-specs, incluidas las ediciones de contenido que no mueven nada, cuando se agreguen, eliminen, renombren o muevan artefactos, cuando los directorios de un agente expongan symlinks obsoletos o rotos, cuando cambie qué agentes usa el proyecto, cuando los puntos de entrada del repositorio puedan estar clobbeados o ausentes tras un clon, o cuando deba verificarse la exposición multi-agente de la capa agéntica.
version: 1.1.0
---

# Skill syncing-agent-symlinks

Mantiene sincronizadas las estructuras orientadas al agente con `ai-specs` como fuente canónica.

"Agent" en el nombre de esta skill se refiere al agente *consumidor* (Claude Code, Cursor, y los que el proyecto use), no al tipo de artefacto `agents`.

La skill cubre **dos alcances**, y una ejecución completa atiende los dos:

1. Los **tipos de artefacto** de `ai-specs` que se exponen a los agentes activos.
2. Los **puntos de entrada** del repositorio, en la raíz.

Ambos dependen de qué agentes usa el proyecto, así que eso se resuelve primero.

## Paso 0 - Resolver los agentes activos

**Este paso va antes que cualquier otro y no se omite.** Todo lo demás —qué directorios existen, qué se crea, qué se elimina— depende de su resultado.

Orden de resolución, sin excepción:

1. **Argumento explícito** del usuario en el prompt.
2. **La sección `## Agentes del proyecto` de `docs/base-standards.md`**, si está configurada.
3. **Preguntar al usuario** si no hay ni argumento ni configuración.

**La herramienta que ejecuta esta skill no implica que esté activa.** Correr bajo un agente no autoriza a crear su directorio: eso lo decide el registro. Un agente marcado como no usado no recibe espejos, aunque sus directorios ya existan.

El registro manda sobre el mapa de convenciones de abajo: el mapa dice *cómo* se expone cada agente, el registro dice *a cuáles*. Si entran en conflicto, gana el registro, porque `base-standards.md` tiene precedencia sobre un documento delegado.

## Mapa de convenciones por agente

Conocimiento genérico: qué ruta usa cada agente. No es configuración del proyecto.

| Agente | Punto de entrada | `skills` | `agents` | Verificado |
|---|---|---|---|---|
| Claude Code | `CLAUDE.md` | `.claude/skills/<unidad>` | `.claude/agents/<unidad>.md` | Sí, documentación oficial |
| OpenCode | `AGENTS.md` | `.opencode/skills/<unidad>` | No se espeja en `.opencode/agents/`: OpenCode rechaza el frontmatter canónico de un agent y no arranca | Sí, documentación oficial y prueba de ejecución |

**Un agente activo que no esté en esta tabla no se deriva por analogía.** No se asume que use `.<agente>/skills/` porque Claude Code lo haga: se le dice al usuario que la convención no está verificada y se le piden las rutas. Confirmadas, se agregan como fila nueva y recién entonces se sincroniza.

Ese bloqueo es **por agente, no de la ejecución**: los agentes cuya convención sí está verificada se sincronizan igual, y el bloqueado queda pendiente en el reporte. Su punto de entrada tampoco se deriva: no es que no necesite uno, es que **no se puede determinar** hasta que el usuario lo confirme.

**Una celda `No se espeja` es una convención verificada, no una que falte.** Dice que ese tipo, para ese agente, no se expone, y por qué. No es el Escenario D: no se bloquea ni se piden rutas. La ruta que nombra la celda no es un espejo: no se crea, y lo que haya en ella se registra en el inventario para reportarlo, porque una entrada ahí es justamente lo que la celda dice que rompe al agente.

`AGENTS.md` no es de ningún agente en particular: es el punto de entrada transversal, y el estándar
abierto que buena parte del ecosistema lee de forma nativa. Claude Code no está en ese grupo —lee
`CLAUDE.md`—, y por eso el harness mantiene los dos.

**Que una herramienta lea un punto de entrada no la vuelve un agente activo.** Lo que el registro
gobierna es qué espejos se crean, no qué archivo lee cada herramienta: exponer las reglas a quien
sepa leerlas es el propósito del punto de entrada, y no autoriza a crear el directorio de nadie.

## Tipos de artefacto

Esta tabla es el alcance autoritativo de los artefactos. Ejecuta el flujo **una vez por tipo espejado**.

| tipo | fuente canónica | unidad | descubrimiento | una unidad válida requiere | espejado |
|------|------------------|--------|----------------|-----------------------------|----------|
| `skills` | `ai-specs/skills` | directorio | `ai-specs/skills/*/SKILL.md` | un `SKILL.md` dentro del directorio | Sí |
| `agents` | `ai-specs/agents` | archivo | `ai-specs/agents/*.md` | frontmatter YAML con `name`, igual al nombre del archivo sin extensión | Sí |
| `scripts` | `ai-specs/scripts` | archivo | `ai-specs/scripts/*` | — | **No** |

Forma canónica del enlace de un tipo espejado:

```
<espejo>/<unidad> -> ../../ai-specs/<tipo>/<unidad>
```

El `../../` corresponde a un espejo de dos niveles (`.claude/skills/`). Si el mapa define un espejo a otra profundidad, la ruta relativa se ajusta a esa profundidad: no es una constante.

Para `agents` la unidad incluye la extensión `.md`; para `skills` no.

**`scripts` es un tipo conocido que deliberadamente no se espeja.** No es un directorio fuera de alcance: está gobernado por `ai-specs/ai-specs-standards.md`, y su exclusión es una decisión, no un olvido. Un script es una herramienta que una skill ejecuta, no una instrucción que un agente deba descubrir. En el reporte aparece como "conocido, no espejado", nunca como observación fuera de alcance.

**Nunca amplíes el alcance por improvisación.** Si un directorio bajo `ai-specs` no está en esta tabla (por ejemplo `ai-specs/templates`), está fuera de alcance: repórtalo como observación, no lo enlaces. Para gestionarlo, primero se agrega su fila aquí y a `ai-specs-standards.md`, y después se ejecuta el flujo.

## Puntos de entrada del repositorio

Segundo alcance, con forma distinta: aquí un único archivo canónico —`docs/base-standards.md`— se expone bajo varios nombres en la raíz.

Qué puntos de entrada existen sale del **mapa de convenciones cruzado con el registro**: el de cada agente activo, más `AGENTS.md` como convención transversal. La lista resultante es cerrada: **no agregues puntos de entrada por analogía.**

Forma del enlace, sin `../` porque vive en la raíz:

```
<punto-de-entrada> -> docs/base-standards.md
```

**El modo de falla dominante es que el symlink haya sido reemplazado por un archivo regular.** Ocurre en clones de Windows sin `core.symlinks=true` —Git materializa el enlace como un archivo de texto con la ruta de destino— y cuando una herramienta escribe el archivo en lugar de seguirlo. Es una falla silenciosa: el archivo existe, el repositorio se ve normal y las reglas del proyecto no se cargan.

Por eso **`test -e` no basta: un archivo regular clobbeado lo aprueba.** La verificación obligatoria es `test -L`, y aplica igual a los symlinks de espejo, que sufren el mismo clobber.

Tratamiento:

- **Reporta siempre** el estado exacto: symlink válido, archivo regular, ausente, o apuntando a un destino equivocado.
- **No sobrescribas por iniciativa propia** un archivo regular en esa ruta: puede tener contenido que alguien escribió a propósito. Reporta el comando de reparación y espera confirmación.
- Un punto de entrada **ausente sí se crea**, porque no hay nada que se pueda perder.
- Un punto de entrada **de un agente no activo** se reporta y se propone su eliminación; no se borra en silencio.

## Reglas de alcance y seguridad

- La fuente canónica de los artefactos es `ai-specs`; la de los puntos de entrada es `docs/base-standards.md`.
- En los espejos, gestiona únicamente las entradas que sean symlinks al espacio de nombres canónico.
- No elimines entradas que no sean symlinks a menos que el usuario lo pida explícitamente.
- Nunca sobrescribas automáticamente un directorio o archivo real; repórtalo como conflicto.
- Las entradas sin contraparte canónica y sin destino canónico son `external` y se dejan intactas (por ejemplo `.cursor/rules/`).
- **Un directorio de espejo ausente, de un agente activo, se crea**, si el mapa lo define como espejo. La ruta de una celda `No se espeja` no lo es. Es una consecuencia mecánica de que el agente esté activo, no una ampliación de alcance.

## Flujo de trabajo

Resuelve el Paso 0 una vez. Después ejecuta los Pasos 1-4 por cada tipo espejado, y reporta una sola vez en el Paso 5.

### Paso 1 - Construir los inventarios

Para el tipo actual:

1. Unidades canónicas según su patrón de `descubrimiento`.
2. Entradas de espejo en el destino de **cada agente activo** que espeje ese tipo.
3. Entradas en la ruta de una celda `No se espeja`, si existe.
4. Entradas de espejo de **agentes no activos**, si sus directorios existen.

Clasifica cada entrada de espejo:

- `linked`: symlink válido que apunta a una unidad canónica existente, en un destino de espejo. Fuera de uno, ver `unmirrored`.
- `orphan`: symlink al espacio de nombres canónico cuya unidad ya no existe → se elimina.
- `broken`: symlink cuyo destino no existe **pero cuya unidad canónica sí está presente** —una ruta mal formada, un renombrado a medias— → se recrea.
- `conflict`: entrada que no es symlink con el mismo nombre que una unidad canónica.
- `stale-agent`: entrada de un agente que el registro no declara activo → se reporta, no se toca. **Queda congelada a propósito:** no recibe las unidades nuevas, así que con el tiempo divergirá del canónico. Eso es la política funcionando, no un olvido de sincronización, y el reporte lo dice para que nadie lo lea como error.
- `unmirrored`: entrada en la ruta de una celda `No se espeja` → se reporta con el motivo de la celda y el comando de eliminación propuesto, y no se toca: eliminarla lo decide el usuario sobre el reporte, y una autorización genérica previa —"borra lo que haga falta"— no la cubre, porque se dio sin saber que la entrada existía. **Gana sobre `linked`:** que el symlink resuelva no lo hace válido, porque el mapa dice que ese tipo, ahí, no va.
- `external`: entrada ajena a la política canónica → se deja intacta.

La distinción entre `orphan` y `broken` es la unidad canónica, no el enlace: los dos se ven igual al mirar el symlink y van a planes opuestos. Verifica la unidad antes de clasificar.

### Paso 2 - Calcular el plan

Por cada destino de espejo de un agente activo. Un tipo marcado `No se espeja` para un agente no tiene destino: no entra en `to_add`, aunque sus unidades falten ahí.

- `to_add`: unidades canónicas que faltan. Si el directorio de espejo no existe, crearlo es parte de esto.
- `to_fix`: entradas `broken`.
- `to_remove`: entradas `orphan`.
- `to_report`: `conflict`, `unmirrored`, `stale-agent`, `external`, y los directorios fuera de alcance.

### Paso 3 - Aplicar de forma segura

En este orden:

1. Crear los directorios de espejo ausentes de agentes activos.
2. Agregar los symlinks faltantes.
3. Recrear los `broken`: eliminar el enlace y volver a crearlo.
4. Eliminar los `orphan`, solo si apuntan al espacio de nombres canónico y su unidad ya no existe.

Nunca eliminar directorios o archivos que no sean symlinks, ni entradas fuera de la política canónica, ni nada de un agente no activo.

### Paso 4 - Verificar la integridad

- Cada unidad canónica existe en el espejo de cada agente activo que espeje su tipo como symlink válido, o está listada como conflicto.
- La ruta de cada celda `No se espeja` no tiene entradas, o están listadas como `unmirrored`.
- **Cada entrada de espejo pasa `test -L` y `test -e`**, no solo `test -e`.
- No quedan symlinks rotos en ningún lugar del repositorio, fuera de `node_modules/`: los de dependencias de terceros no son del repositorio.
- Las entradas externas siguen intactas.
- Cada unidad canónica satisface su columna "una unidad válida requiere". Si falla, sigue enlazada pero se reporta como advertencia.
- Los **puntos de entrada** pasan `test -L` además de `test -e`. Obligatorio en toda ejecución, incluso si nada cambió bajo `ai-specs`: el clobber viene del clon o de otra herramienta, no de tus cambios.
- Si el repositorio es un repo git, los enlaces quedan registrados como symlinks (modo `120000`) y no como archivos regulares. Si no lo es —un directorio extraído, una copia de trabajo suelta—, **declara la verificación como no aplicable** en el reporte en lugar de omitirla: la ausencia de índice es un dato, no un fallo.

### Paso 5 - Reportar

Un único reporte que cubra:

- Los **agentes activos** y de dónde salió esa lista (argumento, registro o pregunta).
- Por tipo espejado: cantidad de unidades canónicas.
- Por tipo y por agente activo: agregados, corregidos, eliminados, conflictos, externas omitidas. Un tipo que no se espeja para ese agente se reporta como **no espejado por convención**, con el motivo de la celda: procesado, no omitido.
- **Tipos conocidos no espejados**: `scripts`, con su cantidad de archivos, indicando explícitamente que no se espeja por diseño. **Se reporta exista o no el directorio** —con cero archivos si falta—, para que quede constancia de que se evaluó.
- **Scripts huérfanos**: los archivos de `ai-specs/scripts` que ninguna skill referencia. `ai-specs-standards.md` los declara código huérfano que se reporta; esta skill es la que lo comprueba, porque es la que ya recorrió `ai-specs/`.
- **Estado de los puntos de entrada**, uno por uno, con su estado exacto.
- **Espejos de agentes no activos**, si existen, con el comando de eliminación propuesto.
- **Observaciones fuera de alcance**: directorios bajo `ai-specs` sin fila en la tabla, y agentes activos sin convención verificada.
- Advertencias de validez y bloqueos restantes.

Indica explícitamente que se procesó cada tipo espejado. Un reporte que cubra solo uno está incompleto.

## Escenarios

Los tres primeros aplican igual a cada tipo; solo cambia la forma de la unidad.

### A - Artefacto nuevo en ai-specs

Agregar el symlink faltante en el espejo de cada agente activo que espeje ese tipo y verificar que resuelva. En la primera ejecución de un clon, la unidad que falta puede ser **esta misma skill**: es un caso normal, no una anomalía.

### B - Artefacto eliminado de ai-specs

Eliminar el symlink huérfano de cada espejo activo. Mantener intactas las entradas no canónicas y reportarlas.

### C - Artefacto renombrado

Un renombrado es un agregado más una eliminación. Nunca renombrar un symlink en su lugar: la ruta de destino se recrea.

### D - Agente activo sin convención verificada

El registro lo declara activo y el mapa no tiene su fila. **No derivar las rutas por analogía.** Reportarlo como bloqueo, pedir el punto de entrada y los directorios, y sincronizarlo recién cuando estén confirmados y agregados al mapa.

### E - Agente no activo con espejos presentes

Sus directorios existen de una configuración anterior. Reportarlos como `stale-agent` con el comando de eliminación propuesto, y **no tocarlos**: eliminar estructura que alguien creó es una decisión del usuario.

### F - `ai-specs/scripts` presente

No se espeja. Confirmarlo explícitamente en el reporte como tipo conocido no espejado, para que quede claro que se evaluó y se excluyó por diseño.

## Patrones de comandos (referencia)

**Para la verificación completa, ejecuta `ai-specs/scripts/check-references.sh`**: cubre los cuatro bloques de abajo más las rutas y secciones citadas en la documentación, y sale con código distinto de cero si encuentra algo. Los comandos que siguen quedan como referencia de qué hace cada chequeo y para inspeccionar un caso puntual.

Los ejemplos usan las rutas de Claude Code. Sustitúyelas por las de cada destino de espejo que el mapa defina para el agente activo que corresponda, nunca por la raíz de su directorio: ahí la herramienta guarda archivos propios, como `node_modules/`, que no son espejos.

```bash
# inventarios canónicos
ls ai-specs/skills                      # skills: nombres de directorio
ls ai-specs/agents                      # agents: nombres de archivo con .md
ls ai-specs/scripts 2>/dev/null         # scripts: conocido, no espejado

# inspeccionar entradas de espejo (el directorio puede no existir)
ls -la .claude/skills .claude/agents 2>/dev/null

# crear el directorio de espejo ausente de un agente activo
mkdir -p .claude/skills .claude/agents

# agregar enlace canónico (skills: sin extensión)
ln -s ../../ai-specs/skills/<nombre-skill> .claude/skills/<nombre-skill>

# agregar enlace canónico (agents: con extensión .md)
ln -s ../../ai-specs/agents/<nombre-agent>.md .claude/agents/<nombre-agent>.md

# eliminar enlace canónico huérfano
rm .claude/<tipo>/<unidad>

# 1. symlinks rotos en TODO el repositorio: no debe devolver nada
find . \( -path ./.git -o -name node_modules \) -prune -o -type l ! -exec test -e {} \; -print

# 2. cada entrada de espejo sigue siendo symlink y resuelve
find .claude/skills .claude/agents -mindepth 1 -maxdepth 1 2>/dev/null | while read -r e; do
  test -L "$e" || echo "$e ya no es symlink (clobber)"
  test -e "$e" || echo "$e apunta a un destino inexistente"
done

# 3. puntos de entrada: mismo criterio, test -L imprescindible
for f in CLAUDE.md AGENTS.md; do
  test -L "$f" || echo "$f ya no es symlink (clobber o clone en Windows)"
  test -e "$f" || echo "$f apunta a un destino inexistente"
done

# 4. si es un repo git: los enlaces deben ser modo 120000
git ls-files -s CLAUDE.md AGENTS.md .claude/skills .claude/agents 2>/dev/null \
  | grep -v '^120000' || true
# acotado a los directorios de espejo a propósito: bajo .claude/ puede haber
# archivos versionados que legítimamente no son symlinks, como settings.json
```

## Señales de alerta

Nunca:

- sincronizar sin haber resuelto antes qué agentes están activos
- asumir que la herramienta en uso es un agente activo
- crear el directorio de un agente que el registro no declara activo
- crear la ruta de una celda `No se espeja`, o dar por `linked` una entrada que esté en ella
- derivar las rutas de un agente por analogía con otro
- espejar `scripts`, o reportarlo como tipo desconocido
- tratar `ai-specs` como no canónico
- procesar un tipo espejado y omitir otro
- inferir el alcance "por analogía" en lugar de leer las tablas
- enlazar un directorio bajo `ai-specs` sin fila en la tabla de Tipos de Artefacto
- eliminar automáticamente directorios o archivos reales, o espejos de agentes no activos
- dar por bueno un enlace solo porque el archivo existe: `test -e` no distingue un symlink de un archivo clobbeado
- dejar symlinks canónicos rotos después de la sincronización
- reportar una sincronización completa sin haber verificado los puntos de entrada

Siempre:

- resolver los agentes activos en el Paso 0, con su orden de tres pasos
- verificar con `test -L` tanto los espejos como los puntos de entrada
- analizar antes de cambiar, y aplicar cambios mínimos y seguros
- preservar las entradas no canónicas y las de agentes no activos
- entregar un reporte que cubra los agentes activos, cada tipo espejado, los tipos no espejados, los puntos de entrada y los bloqueos
