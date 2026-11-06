#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
ZAP_DIR="$ROOT_DIR/security/zap"
REPORTS_DIR="$ZAP_DIR/reports"

DOCKER_BIN="${DOCKER_BIN:-docker}"
ZAP_IMAGE="${ZAP_IMAGE:-ghcr.io/zaproxy/zaproxy:stable}"
COMPOSE_PROJECT="${COMPOSE_PROJECT:-sistema-reservas}"
NETWORK="${COMPOSE_PROJECT}_default"
FAIL_ON="${ZAP_FAIL_ON:-medium}"

COMPOSE=("$DOCKER_BIN" compose)

mkdir -p "$REPORTS_DIR"
chmod 0777 "$REPORTS_DIR"
rm -f "$REPORTS_DIR"/api-zap.* "$REPORTS_DIR"/ui-zap.* "$REPORTS_DIR"/summary.md

to_docker_path() {
  if [[ "$DOCKER_BIN" == *.exe ]]; then
    if command -v wslpath >/dev/null 2>&1; then
      wslpath -w "$1"
    elif command -v cygpath >/dev/null 2>&1; then
      cygpath -w "$1"
    else
      printf '%s' "$1"
    fi
  else
    printf '%s' "$1"
  fi
}

REPORTS_MOUNT="$(to_docker_path "$REPORTS_DIR")"
ZAP_DIR_MOUNT="$(to_docker_path "$ZAP_DIR")"

cleanup() {
  if [[ "${KEEP:-0}" == "1" ]]; then
    echo "KEEP=1: se deja el entorno en ejecución."
    return
  fi
  "${COMPOSE[@]}" --profile frontend down -v --remove-orphans >/dev/null 2>&1 || true
}
trap cleanup EXIT

echo "==> Preparando un entorno desechable"
"${COMPOSE[@]}" --profile frontend down -v --remove-orphans >/dev/null 2>&1 || true

echo "==> Levantando API y frontend con Docker Compose"
"${COMPOSE[@]}" --profile frontend up -d --build

wait_for() {
  local url=$1
  local name=$2
  for _ in $(seq 1 60); do
    if curl -fsS "$url" >/dev/null 2>&1; then
      echo "==> $name disponible en $url"
      return 0
    fi
    sleep 2
  done
  echo "ERROR: $name no respondió en $url" >&2
  "${COMPOSE[@]}" --profile frontend logs --tail=200 >&2 || true
  return 1
}

wait_for "http://localhost:8080/usuarios" "API"
wait_for "http://localhost:5173/healthz" "interfaz"

SCAN_FAILURES=0

run_scan() {
  local name=$1
  shift
  local exit_code=0
  echo "==> Escaneo $name"
  set +e
  "$DOCKER_BIN" run --rm \
    --network "$NETWORK" \
    -v "${REPORTS_MOUNT}:/zap/wrk:rw" \
    -v "${ZAP_DIR_MOUNT}:/zap/rules:ro" \
    "$ZAP_IMAGE" "$@"
  exit_code=$?
  set -e
  if [[ $exit_code -ne 0 ]]; then
    echo "ADVERTENCIA: el escaneo $name terminó con código $exit_code" >&2
    SCAN_FAILURES=$((SCAN_FAILURES + 1))
  fi
}

run_scan api zap-api-scan.py \
  -t http://api:8080/v3/api-docs \
  -f openapi -S -I \
  -c /zap/rules/rules.tsv \
  -r api-zap.html -J api-zap.json -T 5

run_scan ui zap-baseline.py \
  -t http://frontend -j -I \
  -c /zap/rules/rules.tsv \
  -m 2 -r ui-zap.html -J ui-zap.json -T 5

VERIFY_CODE=0
echo "==> Verificando umbral de fallo: $FAIL_ON"
set +e
python3 "$ZAP_DIR/verify-findings.py" \
  --reports "$REPORTS_DIR" \
  --fail-on "$FAIL_ON" \
  --summary "$REPORTS_DIR/summary.md"
VERIFY_CODE=$?
set -e

if [[ $SCAN_FAILURES -gt 0 ]]; then
  echo "ERROR: $SCAN_FAILURES escaneo(s) terminaron con error operativo." >&2
  exit 1
fi

exit "$VERIFY_CODE"
