---
description: "Guía paso a paso para adaptar este harness base a un proyecto nuevo: prerequisitos del clon, configuración de integraciones, poda de estándares que no apliquen y registro de los artefactos de la capa agéntica."
---
# Guía de adopción del harness

Esta guía se usa **una sola vez por proyecto**, al clonar el harness. Por eso vive fuera de `base-standards.md`: lo que se carga en cada sesión de trabajo debe ser solo lo que aplica siempre.

El harness no asume que el proyecto sea de software. Sirve igual para un desarrollo, para la configuración de un entorno como Google Workspace o una nube, o para un proceso sin componente técnico. Lo que cambia entre proyectos es qué estándares aplican, no la estructura.

## Paso 0 - Prerequisito del clon

El repositorio usa symlinks versionados: los puntos de entrada de la raíz apuntan a `docs/base-standards.md`, y los directorios de cada agente activo apuntan a `ai-specs/`. Cuáles existen depende de los agentes que el proyecto declare en el paso 1.

**En Windows, habilita los symlinks antes de clonar:**

```bash
git config --global core.symlinks true
```

Sin eso, Git materializa cada symlink como un archivo de texto que contiene la ruta de destino. El repositorio se ve normal y las reglas desaparecen sin ningún mensaje de error. En macOS y Linux funciona por defecto.

**Ese comando aplica al Git de Windows, no al de WSL.** Ahí la decisión no es la terminal que uses sino dónde vive el clon. Si el clon está en el filesystem de Linux de WSL, el checkout lo hace Git de Linux y los symlinks son nativos: `core.symlinks` no interviene y forzarlo no aporta nada. Si el clon está en `/mnt/c`, el checkout lo hace Git de Windows y sí necesita el `core.symlinks` de arriba, además de Modo para desarrolladores o una terminal elevada, porque crear symlinks exige ese privilegio. La configuración es por usuario y por entorno: el `~/.gitconfig` de WSL y el de Windows son archivos distintos. Lo que no conviene es clonar en `/mnt/c` con Git de Windows y después trabajar desde WSL, ni mantener dos clones en paralelo.

**Windows no muestra un symlink de Linux como symlink.** Explorer y PowerShell ven `CLAUDE.md` y `AGENTS.md` como archivos regulares de 22 bytes cuyo contenido es `docs/base-standards.md`, y lo mismo pasa con los espejos de `.claude/` y `.opencode/`. No es un clobber: es cómo se ve desde Windows un enlace de ext4. Por eso la verificación se corre desde WSL, y si abrís el proyecto con VS Code conviene Remote-WSL, que sí sigue los enlaces. No borres los directorios de un agente porque "no tengan symlinks" vistos desde Windows, ni corras Git de Windows sobre un clon que vive en ext4.

Verifica el clon antes de trabajar, desde el mismo entorno que va a usar el repositorio:

```bash
for f in CLAUDE.md AGENTS.md; do
  test -L "$f" && test -e "$f" && echo "ok   $f -> $(readlink "$f")" \
    || echo "ROTO $f: revisa core.symlinks y vuelve a clonar"
done

# los espejos de cada agente sufren el mismo clobber que los de la raíz; el
# script toma sus rutas del mapa de convenciones de syncing-agent-symlinks
bash ai-specs/scripts/check-references.sh
```

## Paso 1 - Declara los agentes del proyecto

Abre la sección `## Agentes del proyecto` de `docs/base-standards.md` y declara con qué herramientas de IA se trabaja este proyecto. **Este paso va primero: determina qué directorios y qué puntos de entrada debe tener el repositorio**, y por lo tanto qué crea y qué elimina `syncing-agent-symlinks`.

El harness viene con Claude Code activo, que es la única convención verificada contra documentación oficial. Si tu proyecto usa otra herramienta, agrégala — y si su convención de directorios no está en el mapa de la skill, la skill te va a pedir las rutas antes de crear nada, en lugar de adivinarlas.

Marcar un agente como no usado no es neutral: sus espejos, si existen, se reportan para que decidas eliminarlos.

La fila `Cursor | No usado — convención sin verificar` viene de fábrica y **es estado válido, no residuo**: documenta que se evaluó ese agente y se descartó. Déjala si la decisión es esa, cámbiala a activo si lo vas a usar, o elimínala si el agente no es siquiera un candidato para tu equipo.

## Paso 2 - Completa las integraciones

Abre la sección `## Integraciones` de `docs/base-standards.md` y registra los identificadores de los recursos externos del proyecto: el tablero de backlog, el workspace, lo que aplique.

La tabla viene **vacía a propósito**: el harness no trae ningún identificador de ejemplo. Agrega una
fila por recurso, con su identificador completo.

La columna `Servicio` es una etiqueta legible para las personas, no una clave que ninguna skill busque; el `Identificador` es lo que una skill lee, así que va completo y sin ambigüedad:

```markdown
| Servicio | Identificador |
|---|---|
| Tablero de backlog en Trello | https://trello.com/b/EJEMPLO1/mi-tablero |
```

**Una fila a medias es peor que ninguna fila.** No dejes una con el identificador en blanco ni con un
marcador de relleno: una skill leería esa celda como configuración válida del proyecto y dejaría de
preguntar. Si un recurso todavía no está definido, la fila no va.

Si un recurso no está configurado, las skills lo preguntan. Eso es correcto y esperado: es preferible a inferirlo.

## Paso 3 - Poda los estándares que no apliquen

| Documento | Qué hacer |
|---|---|
| `engineering-standards.md` | Elimínalo si el proyecto no tiene código |
| `trello-backlog-contract.md` | Elimínalo o adáptalo si el backlog no vive en Trello |

Cada vez que elimines un documento, **quita también su fila del mapa de estándares** de `base-standards.md`. Un mapa que apunta a un archivo inexistente es peor que un mapa incompleto.

**La poda casi nunca termina en el mapa.** Un documento eliminado suele estar citado en otros lugares —la tabla de disciplina de verificación, un ejemplo, la cláusula de una skill—, y esas citas quedan colgando. No las busques a mano: el Paso 8 las encuentra. Lo que sí conviene revisar a criterio, porque ninguna herramienta puede decidirlo por ti:

- La columna `Estado` de las filas que **conservaste**: si `trello-backlog-contract.md` decía "Solo si el proyecto usa un backlog en Trello" y ya decidiste que sí, pasa a "Vigente".
- La tabla de política de idioma del Paso 5, si podaste el código. Cae la fila de **comentarios y docstrings**, que no existen sin código. **No cae** la de *código, identificadores y nombres*: un proyecto sin software igual nombra columnas, carpetas y grupos, y esa fila sigue gobernándolos.

## Paso 4 - Agrega los estándares del dominio

Crea los documentos que el proyecto sí necesita siguiendo la convención de nombres de `documentation-standards.md`: `backend-standards.md`, `frontend-standards.md`, `<dominio>-data-model.md`, y lo que corresponda.

Registra cada uno como una fila nueva en el mapa de estándares. Si un documento no encaja en ningún patrón del catálogo, propón el patrón antes de crear el archivo: no inventes una convención sobre la marcha.

## Paso 5 - Ajusta la política de idioma

La tabla de `base-standards.md` refleja la decisión de este equipo: prosa en español, código y nombres de archivo en inglés. Si el proyecto tiene otra política, este es el único lugar donde se cambia.

## Paso 6 - Revisa la capa agéntica

Los artefactos que trae el harness son transversales y deberían servir tal cual. Revisa si el proyecto necesita alguno propio y, si lo creas, sigue la skill que le corresponde: `writing-skills` para una skill, `writing-agents` para un agent. Las dos requieren `testing-with-subagents`, que es el ciclo de pruebas. Qué artefactos existen y qué forma tienen lo gobierna `ai-specs/ai-specs-standards.md`.

Después de cualquier cambio bajo `ai-specs/`, corre la skill `syncing-agent-symlinks`. La regla la fija `docs/base-standards.md`, sección "Convenciones de control de versiones".

Si el Paso 8 te hace corregir algo bajo `ai-specs/` —una cita que quedó colgando de la poda—, vuelve acá y córrela de nuevo antes de dar la adopción por terminada.

## Paso 7 - Reescribe el `README.md`

El `README.md` de la raíz describe **el harness**, no tu proyecto: es la portada que trae la plantilla. Si lo dejas, el primer archivo que abre cualquiera que llegue al repositorio le habla de otra cosa.

Reescríbelo para tu proyecto conservando su función: qué es, para quién, cómo empezar y qué hay adentro. Se rige por `docs/documentation-standards.md` como el resto de la documentación, aunque viva en la raíz.

**Lo único que conviene conservar del original** es el puntero a `docs/base-standards.md` como punto de entrada de las reglas, y la línea de verificación del Paso 8. Todo lo demás es del harness y sobra en tu proyecto.

## Paso 8 - Verifica

```bash
bash ai-specs/scripts/check-references.sh
```

Verifica las rutas y las secciones que los documentos citan, que cada skill y cada agent nombrado exista, que el mapa de estándares no apunte a un archivo que eliminaste en el Paso 3, y que los symlinks sigan siendo symlinks. Mira los archivos que están en disco, hayas hecho `git add` o no.

**Debe terminar con `OK` y sin hallazgos.** Si reporta algo, arréglalo antes de trabajar: son las referencias que quedaron colgando de la poda.

**Reparar una cita rota bajo `ai-specs/` está permitido acá.** `ai-specs/ai-specs-standards.md` dice que un artefacto no se cambia por iniciativa propia, y sigue valiendo — pero adoptar el harness es el pedido explícito, y una cita a un documento que acabas de eliminar no es un cambio de criterio: es terminar la poda. Lo que esa regla prohíbe es reescribir lo que la skill *dice*, no reparar a dónde *apunta*.

El script declara en su salida lo que no cubre. Para la coherencia que exige juicio —que un documento diga la verdad sobre lo que otro contiene— está la skill `updating-docs` en su modo auditoría.
