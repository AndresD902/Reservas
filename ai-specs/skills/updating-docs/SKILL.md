---
name: updating-docs
description: Úsala antes de un commit o push, cuando se te pida documentar cambios, o cuando código modificado pueda haber dejado obsoleta la documentación en docs/. También cuando se pida auditar o verificar la coherencia de cualquier archivo versionado del repositorio, docs/ y ai-specs/ incluidos, cuando se sospeche que hay referencias o atribuciones desactualizadas de cambios anteriores, o al cerrar una tanda de trabajo.
version: 1.0.0
---

# Skill updating-docs

Mantiene la documentación de `docs/` sincronizada con los cambios reales del código.

Tiene **dos modos**. El de **sincronización** es el de siempre y el que corre por defecto: parte del
diff y actualiza lo que ese cambio dejó obsoleto. El de **auditoría** no parte del diff: verifica que
las afirmaciones que los documentos hacen sigan siendo ciertas.

**Por qué hacen falta los dos.** El modo sincronización es incremental: solo ve el delta entre el
último commit y el working tree. Una inconsistencia que se cuela una vez queda fuera de todos los
deltas siguientes y **nunca vuelve a ser visible**. Lo incremental no detecta deuda acumulada; para
eso está la auditoría.

El catálogo de documentos y la convención de nombres viven en `docs/documentation-standards.md`, que es su fuente única. La política de idioma es transversal y vive en `docs/base-standards.md`. Esta skill aporta el procedimiento.

## Modo sincronización

Es el modo por defecto. Si nadie pidió una auditoría, corre esto.

### Paso 1 - Detectar el alcance real

Ejecuta `git status`, `git diff` y `git diff --staged` para ver qué cambió de verdad. No asumas el alcance a partir de la conversación ni de lo que crees recordar.

### Paso 2 - Cargar las reglas vigentes

Lee `docs/documentation-standards.md` para obtener el catálogo de patrones, sus disparadores de actualización y la convención de nombres. Estas reglas pueden haber cambiado desde la última ejecución. La política de idioma ya está fijada en `docs/base-standards.md`.

### Paso 3 - Mapear cada cambio a su documento

Para cada cambio detectado en el Paso 1, identifica el archivo del catálogo que le corresponde.

- Si el archivo destino no existe todavía, créalo siguiendo la convención de nombres del estándar.
- Si el cambio no encaja en ningún patrón del catálogo, repórtalo como observación y propón el patrón nuevo. No inventes una convención.
- Si un cambio no requiere documentación, no fuerces una actualización.

### Paso 4 - Aplicar las actualizaciones

Escribe en español, según la política de idioma. Preserva la estructura y el formato del documento existente en lugar de reorganizarlo.

### Paso 5 - Verificar

Confirma dos cosas:

- Cada cambio relevante del diff quedó reflejado en la documentación.
- No se documentó nada que no aparezca en el diff.

### Paso 6 - Reportar

Devuelve una tabla con los archivos actualizados y qué cambió en cada uno, más las observaciones del Paso 3 si las hubo.

| Archivo | Cambio aplicado |
|---|---|
| ... | ... |

## Modo auditoría

No parte del diff. Recorre el repositorio y comprueba que lo que los documentos **afirman** siga
siendo cierto.

**Corre este modo cuando** se pida explícitamente una auditoría, al cerrar una tanda de iteraciones,
o cuando haya motivo para sospechar deuda acumulada. No en cada commit: ahí sería ruido.

**Qué se audita: la afirmación, no el directorio.** Este modo cubre **todo lo versionado**, y eso
incluye lo que un archivo de `ai-specs/` afirma sobre otro archivo de `ai-specs/`. Entra por hacer
una afirmación verificable, no por el directorio en que vive.

**Los dos modos no comparten alcance, y la asimetría es deliberada.** Sincronizar es específico de
`docs/`: parte del diff y mapea contra el catálogo de `docs/documentation-standards.md`. Auditar no
tiene nada de `docs/`: las cuatro clases de abajo valen igual para cualquier archivo. Acotar la
auditoría a `docs/` dejaba a la capa agéntica sin nadie que la revisara —`ai-specs-standards.md`
gobierna qué artefactos existen, no verifica lo que afirman— y ahí es donde la deuda se acumuló.

### Paso A1 - Recolectar las afirmaciones verificables

Cuatro clases, de la más mecánica a la que exige juicio:

| Clase | Qué se busca |
|---|---|
| Rutas citadas | Referencias a un archivo del repositorio |
| Secciones citadas | Referencias a un encabezado dentro de otro documento |
| Atribuciones | "vive en X", "X es la fuente única", "lo gobierna X" |
| Reglas repetidas | El mismo enunciado en dos archivos, que pueden haber divergido |

### Paso A2 - Verificar cada una contra el destino

- **Rutas**: el archivo existe. **Un ejemplo ilustrativo no es una referencia**: `Ejemplo:
  \`docs/study-partners-product-brief.md\`` describe un patrón de nombre, no apunta a un archivo que
  deba existir. Confundirlos llena el reporte de falsos positivos y lo vuelve ignorable.
- **Secciones**: el encabezado existe, con ese texto, en el archivo citado.
- **Atribuciones**: **abre el destino y confirma que contiene lo que se le atribuye.** No alcanza con
  que el archivo exista. Este es el que más se salta y el que más deuda produce.
- **Reglas repetidas**: compara las dos versiones. Si divergieron, una está desactualizada; si
  coinciden, sigue siendo duplicación y se reporta como riesgo.

### Paso A3 - Reportar sin corregir

La auditoría **no aplica cambios**: reporta. Cada hallazgo con su archivo, su línea, la afirmación
textual y por qué es falsa. Corregir es una decisión aparte, con su propio alcance.

| Archivo:línea | Afirma | Realidad |
|---|---|---|
| ... | ... | ... |

Si no hay hallazgos, se reporta igual: "auditadas N afirmaciones, ninguna incorrecta" es un
resultado, y es lo que distingue *se verificó* de *no se verificó*.

## Errores comunes

- **Documentar lo que no ocurrió:** describir cambios que no están en el diff, inferidos de la conversación.
- **Escribir en el idioma equivocado:** la política de idioma la fija `docs/base-standards.md`, y su tabla es la fuente única. Consúltala en vez de reproducirla de memoria: una copia enumerada diverge en cuanto la tabla cambia.
- **Crear archivos innecesarios:** abrir un documento nuevo cuando correspondía extender uno existente.
- **Inventar convenciones:** nombrar un archivo fuera de los patrones del catálogo sin proponerlo antes.
- **Omitir el reporte final:** terminar sin decir qué se actualizó.
- **Tratar un ejemplo ilustrativo como referencia rota:** llena la auditoría de falsos positivos.
- **Dar por buena una atribución porque el archivo existe:** hay que abrir el destino y confirmar que dice lo que se le atribuye.
- **Correr la auditoría cuando se pedía sincronización, o al revés:** son dos modos con disparadores distintos.
