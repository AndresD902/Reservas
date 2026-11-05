#!/usr/bin/env bash
# Verifica la parte mecánica de la consistencia del repositorio: rutas citadas,
# secciones citadas, artefactos nombrados, symlinks y el mapa de estándares.
#
# Lo invoca la skill `syncing-agent-symlinks` y el Paso 8 de
# `docs/harness-adoption-guide.md`. Sale con código distinto de cero si hay
# hallazgos, para que sirva en un hook o en CI.
#
# Lo que NO cubre está declarado en su salida, no solo acá: las atribuciones
# ("X dice Y") y las secciones citadas sin archivo explícito exigen juicio, y
# quedan para el modo auditoría de la skill `updating-docs`.

set -uo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")/../.." || exit 2

hallazgos=0
# Rutas que se citan a propósito sin que deban existir. Cada una con su razón:
# ampliar esta lista sin razón escrita es cómo un verificador se vuelve inútil.
EXCLUIDAS=(
  "docs/study-partners-product-brief.md"  # ejemplo ilustrativo de un patrón de nombre
  "backlog/backlog.md"                    # destino prescriptivo: existe solo si hay backlog
  "ai-specs/templates"                    # se cita como ejemplo de directorio fuera de alcance
  "docs/api-spec.yml"                     # destino prescriptivo de HU-11: existe cuando se escriba el contrato OpenAPI
)

reportar() { hallazgos=$((hallazgos + 1)); printf '  %s\n' "$1"; }

# Los destinos de espejo salen del mapa de convenciones de la skill
# `syncing-agent-symlinks`, que es su fuente única: fijarlos acá haría que un
# agente nuevo quedara sin verificar hasta que alguien se acordara de este
# script. Se toman las columnas de `skills` y `agents`; una celda `No se espeja`
# nombra una ruta que no debe tener entradas, y va a su propia lista.
# Se recorren los destinos, no la raíz de cada agente: ahí la herramienta guarda
# archivos propios, como el node_modules de OpenCode, que no son espejos.
MAPA=ai-specs/skills/syncing-agent-symlinks/SKILL.md
ESPEJOS=()
NO_ESPEJADAS=()
while IFS= read -r _celda; do
  _ruta=$(printf '%s' "$_celda" | grep -oE '`\.[a-z]+/[a-z]+/' | head -1 | tr -d '`')
  [ -z "$_ruta" ] && continue
  case "$_celda" in
    *"No se espeja"*) NO_ESPEJADAS+=("${_ruta%/}") ;;
    *) ESPEJOS+=("${_ruta%/}") ;;
  esac
done < <(awk '/^## Mapa de convenciones por agente/{m=1; next} m && /^## /{exit} m && /^\| /' "$MAPA" 2>/dev/null \
           | awk -F'|' '{print $4; print $5}')
# Bash 3.2 con `set -u` falla al expandir un arreglo vacío: de ahí el `+`.
RAICES=$(printf '%s\n' ${ESPEJOS[@]+"${ESPEJOS[@]}"} ${NO_ESPEJADAS[@]+"${NO_ESPEJADAS[@]}"} \
           | cut -d/ -f1 | grep . | sort -u | sed 's/\./\\./' | paste -sd'|' -)

excluida() {
  local r=$1 e
  # Por forma: los placeholders y los globs no son referencias a un archivo real.
  case "$r" in *'<'*|*'>'*|*'*'*) return 0 ;; esac
  for e in "${EXCLUIDAS[@]}"; do [ "$r" = "$e" ] && return 0; done
  # Una ruta que no se espeja se cita justamente para decir que no debe existir.
  for e in ${NO_ESPEJADAS[@]+"${NO_ESPEJADAS[@]}"}; do [ "${r%/}" = "$e" ] && return 0; done
  return 1
}

# Markdown versionado, sin los espejos de los agentes (son symlinks al canónico).
# Sin `mapfile`: macOS trae bash 3.2 y no lo tiene. El harness corre en macOS,
# Linux y Git Bash, así que el piso es 3.2.
# --cached --others: incluye lo versionado Y lo que todavía no se agregó al
# índice. Un documento recién creado es exactamente el que hay que verificar.
# Se saltean: los directorios de los agentes del mapa, los archivos que el índice todavía
# lista pero ya no están en disco (una poda sin `git rm`), y los symlinks de la
# raíz — `CLAUDE.md` y `AGENTS.md` *son* `docs/base-standards.md`, así que
# verificarlos aparte triplicaría cada hallazgo de ese archivo.
DOCS=()
while IFS= read -r _f; do
  [ -f "$_f" ] || continue
  [ -L "$_f" ] && continue
  DOCS+=("$_f")
done < <(git ls-files --cached --others --exclude-standard '*.md' '*.sh' \
           | { if [ -n "$RAICES" ]; then grep -vE "^($RAICES)/"; else cat; fi; } | sort -u)
if [ ${#DOCS[@]} -eq 0 ]; then echo "No hay markdown versionado que verificar." >&2; exit 2; fi

# Cuerpo de cada archivo sin sus bloques de código: los ejemplos viven ahí y no
# son referencias. Es lo que evita la mayoría de los falsos positivos.
sin_fences() { awk '/^```/{d=!d; next} !d' "$1"; }

echo "== Rutas citadas =="
for f in "${DOCS[@]}"; do
  while read -r ruta; do
    [ -z "$ruta" ] && continue
    excluida "$ruta" && continue
    [ -e "$ruta" ] || reportar "$f: no existe $ruta"
    # Backticks y links markdown, las dos formas de citar un archivo. El Paso 7
    # hace escribir un README nuevo, que es donde los links aparecen.
  done < <( { sin_fences "$f" | grep -oE "\`(docs|ai-specs|backlog${RAICES:+|$RAICES})/[A-Za-z0-9/<>._*-]+\`" | tr -d '`';
              sin_fences "$f" | grep -oE "\]\((docs|ai-specs|backlog${RAICES:+|$RAICES})/[A-Za-z0-9/._-]+\)" | sed -E 's/^\]\((.*)\)$/\1/'; } | sort -u)
done
[ "$hallazgos" -eq 0 ] && echo "  sin hallazgos"

previo=$hallazgos
echo "== Documentos citados sin ruta ==" 
# El caso que rompe la poda: un `<algo>-standards.md` citado a secas desde
# otro documento. Se da por bueno si existe un archivo con ese nombre en
# cualquier parte del repositorio; si no existe, solo se reporta cuando el
# nombre sigue un patrón del catálogo de `docs/documentation-standards.md`.
# Sin ese filtro, los ejemplos ilustrativos de la guía oficial de Anthropic
# (EXAMPLES.md, finance.md, REFERENCE.md…) llenarían el reporte de ruido.
EJEMPLOS_DEL_CATALOGO=(
  "backend-standards.md"   # documentation-standards los nombra como ejemplos
  "frontend-standards.md"  # de documentos que se crean si el proyecto los pide
)
# La guía de adopción nombra documentos *como candidatos a eliminar*: citarlos
# es su función, y después de una poda sus menciones quedan apuntando a algo que
# ya no está sin que eso sea un defecto. Sin esta excepción, pasar el Paso 8
# exigiría borrar las instrucciones de la guía.
ARCHIVOS_QUE_NOMBRAN_CANDIDATOS="docs/harness-adoption-guide.md"
for f in "${DOCS[@]}"; do
  [ "$f" = "$ARCHIVOS_QUE_NOMBRAN_CANDIDATOS" ] && continue
  while read -r n; do
    [ -z "$n" ] && continue
    find . -path ./.git -prune -o -name "$n" -print 2>/dev/null | grep -q . && continue
    case "$n" in
      *-standards.md|*-contract.md|*-guide.md|*-data-model.md|*-product-brief.md) : ;;
      *) continue ;;
    esac
    saltear=0
    for e in "${EJEMPLOS_DEL_CATALOGO[@]}"; do [ "$n" = "$e" ] && saltear=1; done
    [ "$saltear" -eq 1 ] && continue
    reportar "$f: cita \`$n\` y no existe ningún archivo con ese nombre"
  done < <(sin_fences "$f" | grep -oE '`[A-Za-z0-9._-]+\.md`' | tr -d '`' | sort -u)
done
[ "$hallazgos" -eq "$previo" ] && echo "  sin hallazgos"

previo=$hallazgos
echo "== Secciones citadas con archivo explícito =="
for f in "${DOCS[@]}"; do
  while IFS='|' read -r destino titulo; do
    [ -z "$titulo" ] && continue
    # Una referencia sin barra es al archivo hermano: `SKILL.md` desde un archivo
    # de apoyo apunta al de su propio directorio, no al de la raíz.
    case "$destino" in */*) : ;; *) destino="$(dirname "$f")/$destino" ;; esac
    if [ ! -e "$destino" ]; then reportar "$f: no existe $destino"; continue; fi
    # Comparación literal: los títulos traen paréntesis y acentos que romperían un regex.
    grep -q -F -x -f <(printf '# %s\n## %s\n### %s\n#### %s\n' "$titulo" "$titulo" "$titulo" "$titulo") "$destino" \
      || reportar "$f: \"$titulo\" no es un encabezado de $destino"
  done < <(sin_fences "$f" | grep -oE '`[A-Za-z0-9/._-]+\.md`, sección "[^"]+"' \
             | sed -E 's/^`([^`]+)`, sección "(.*)"$/\1|\2/' | sort -u)
done
[ "$hallazgos" -eq "$previo" ] && echo "  sin hallazgos"

previo=$hallazgos
echo "== Skills y agents nombrados =="
for f in "${DOCS[@]}"; do
  while read -r n; do
    [ -z "$n" ] && continue
    [ -f "ai-specs/skills/$n/SKILL.md" ] || [ -f "ai-specs/agents/$n.md" ] \
      || reportar "$f: no hay skill ni agent llamado $n"
  done < <(sin_fences "$f" | grep -oE '(skill|agent) `[a-z][a-z-]+`' | grep -oE '`[a-z-]+`' | tr -d '`' | sort -u)
done
[ "$hallazgos" -eq "$previo" ] && echo "  sin hallazgos"

previo=$hallazgos
echo "== Mapa de estándares =="
# A diferencia del chequeo que reemplaza, alcanza a ai-specs/ y no solo a docs/.
while read -r d; do
  [ -f "$d" ] || reportar "referencia muerta en el mapa: $d"
done < <(awk '/^## Mapa de estándares/{m=1; next} m && /^## /{exit} m' docs/base-standards.md \
           | grep -oE '`(docs|ai-specs)/[a-z-]+\.md`' | tr -d '`' | sort -u)
[ "$hallazgos" -eq "$previo" ] && echo "  sin hallazgos"

previo=$hallazgos
echo "== Symlinks =="
[ ${#ESPEJOS[@]} -eq 0 ] && reportar "no se pudo leer el mapa de convenciones de $MAPA: los espejos quedan sin verificar"
while read -r e; do
  test -L "$e" || { reportar "$e ya no es symlink (clobber o clon sin core.symlinks)"; continue; }
  test -e "$e" || reportar "$e apunta a un destino inexistente"
done < <(for d in ${ESPEJOS[@]+"${ESPEJOS[@]}"}; do find "$d" -mindepth 1 -maxdepth 1 2>/dev/null; done
         ls *.md 2>/dev/null | while read -r r; do test -L "$r" && echo "$r"; done)

# Que resuelva no lo hace válido: el mapa dice que ese tipo, para ese agente, no va.
while read -r e; do
  reportar "$e está en una ruta que no se espeja (ver el mapa de convenciones de $MAPA)"
done < <(for d in ${NO_ESPEJADAS[@]+"${NO_ESPEJADAS[@]}"}; do find "$d" -mindepth 1 -maxdepth 1 2>/dev/null; done)

# Sin node_modules: los symlinks rotos de dependencias de terceros no son del repositorio.
while read -r roto; do reportar "symlink roto: $roto"; done \
  < <(find . \( -path ./.git -o -name node_modules \) -prune -o -type l ! -exec test -e {} \; -print)

if git rev-parse --git-dir >/dev/null 2>&1; then
  while read -r m; do reportar "no está registrado como symlink en git: $m"; done \
    < <(git ls-files -s CLAUDE.md AGENTS.md ${ESPEJOS[@]+"${ESPEJOS[@]}"} 2>/dev/null \
          | grep -v '^120000' | awk '{print $4}')
else
  echo "  (no es un repo git: la verificación de modo 120000 no aplica)"
fi
[ "$hallazgos" -eq "$previo" ] && echo "  sin hallazgos"

cat <<'FIN'

== Fuera del alcance de este script ==
  Las atribuciones ("X dice Y") y las secciones citadas sin nombrar su archivo
  exigen abrir el destino y juzgar. Las cubre la skill `updating-docs` en su
  modo auditoría; este script no las mira.
FIN

echo
if [ "$hallazgos" -eq 0 ]; then
  echo "OK: ${#DOCS[@]} archivos verificados, sin hallazgos."
else
  echo "$hallazgos hallazgo(s) en ${#DOCS[@]} archivos."
fi
exit $(( hallazgos > 0 ))
