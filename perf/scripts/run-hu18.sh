#!/usr/bin/env bash
set -euo pipefail
# HU-18: ejecuta el escenario JMeter de 500 usuarios simultáneos contra Docker Compose.
# Precondición: docker compose up (api en el servicio "api" dentro de la red sistema-reservas_default).

PROJECT_NAME="${COMPOSE_PROJECT_NAME:-sistema-reservas}"
NETWORK="${PROJECT_NAME}_default"
DB_CONTAINER="${PROJECT_NAME}-db-1"
API_PORT="${API_PORT:-8080}"
JMX="perf/jmeter/reservas-500.jmx"
RESULTS="perf/results/reservas-500.jtl"
HTML_REPORT="perf/results/html"
SUMMARY="perf/results/summary.json"
mkdir -p perf/results

if ! docker network inspect "$NETWORK" >/dev/null 2>&1; then
  echo "ERROR: no existe la red $NETWORK. Levanta la API con: docker compose up -d" >&2
  exit 1
fi

echo "== Esperando la API =="
for attempt in $(seq 1 60); do
  if curl --fail --silent "http://localhost:$API_PORT/auth/csrf" >/dev/null; then
    break
  fi
  if [[ "$attempt" == "60" ]]; then
    echo "ERROR: la API no respondió en http://localhost:$API_PORT después de 60 segundos" >&2
    exit 1
  fi
  sleep 1
done

# Siembra los usuarios de carga y crea el recurso solo si aún no existe.
docker exec -i "$DB_CONTAINER" psql -U postgres -d sistema_reservas < perf/seed/01-seed-500.sql
RESOURCE_ID=$(docker exec "$DB_CONTAINER" psql -U postgres -d sistema_reservas -t -A -c "SELECT id FROM recursos WHERE nombre='Recurso de carga' ORDER BY id DESC LIMIT 1;")
if [[ ! "$RESOURCE_ID" =~ ^[0-9]+$ ]]; then
  echo "ERROR: no se pudo determinar el id del recurso de carga" >&2
  exit 1
fi
docker exec "$DB_CONTAINER" psql -U postgres -d sistema_reservas -c "DELETE FROM reservas WHERE recurso_id=$RESOURCE_ID AND fecha_inicio='2030-01-01T10:00:00';"
docker exec "$DB_CONTAINER" psql -U postgres -d sistema_reservas -t -A -F ',' -c "SELECT id || ',' || $RESOURCE_ID FROM usuarios WHERE documento LIKE 'CARGA-%' ORDER BY documento;" > perf/results/load-users.csv
USER_COUNT=$(wc -l < perf/results/load-users.csv | tr -d ' ')
if [[ "$USER_COUNT" != "500" ]]; then
  echo "ERROR: se esperaban 500 usuarios de carga, se encontraron $USER_COUNT" >&2
  exit 1
fi

echo "== Ejecutando JMeter (500 hilos, ramp 10s) vía Docker =="
rm -f "$RESULTS" "$SUMMARY"
rm -rf "$HTML_REPORT"
docker run --rm --network "$NETWORK" -e JVM_XMN=256 -e JVM_XMS=512 -e JVM_XMX=2048 -v "$PWD":/work -w /work justb4/jmeter \
  -n -t "/work/$JMX" \
  -l "/work/$RESULTS" \
  -e -o "/work/$HTML_REPORT" 2>&1 | tail -20

echo "== Verificando integridad =="
python3 perf/scripts/verify-integrity.py "$RESULTS" localhost "$API_PORT" "$RESOURCE_ID" "2030-01-01T10:00:00" "2030-01-01T12:00:00" "$SUMMARY"

echo "== Comprobación en BD: solapadas activas =="
ACTIVE_RESERVATIONS=$(docker exec "$DB_CONTAINER" psql -U postgres -d sistema_reservas -t -A -c "SELECT count(*) FROM reservas WHERE recurso_id=$RESOURCE_ID AND estado='ACTIVA' AND fecha_inicio < '2030-01-01T12:00:00' AND fecha_fin > '2030-01-01T10:00:00';")
echo "Reservas ACTIVAS solapadas: $ACTIVE_RESERVATIONS"
if [[ "$ACTIVE_RESERVATIONS" != "1" ]]; then
  echo "FAIL: se esperaba exactamente una reserva activa solapada" >&2
  exit 1
fi
