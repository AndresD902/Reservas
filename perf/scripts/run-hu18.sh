#!/usr/bin/env bash
set -euo pipefail
# HU-18: ejecuta el escenario JMeter de 500 usuarios simultáneos contra Docker Compose.
# Precondición: docker compose up (api en el servicio "api" dentro de la red sistema-reservas_default).

NETWORK="sistema-reservas_default"
JMX="perf/jmeter/reservas-500.jmx"
RESULTS="perf/results/reservas-500.jtl"
mkdir -p perf/results

if ! docker network inspect "$NETWORK" >/dev/null 2>&1; then
  echo "ERROR: no existe la red $NETWORK. Levanta la API con: docker compose up -d" >&2
  exit 1
fi

# Aplica la semilla (500 usuarios CARGA-0001..CARGA-0500 y recurso "Recurso de carga" id 5)
# Si ya está aplicada, el ON CONFLICT la hace idempotente. Limpia reservas de carga previas para repetir.
docker exec sistema-reservas-db-1 psql -U postgres -d sistema_reservas -c "DELETE FROM reservas WHERE recurso_id=5 AND fecha_inicio='2030-01-01T10:00:00';"
docker exec -i sistema-reservas-db-1 psql -U postgres -d sistema_reservas < perf/seed/01-seed-500.sql

echo "== Ejecutando JMeter (500 hilos, ramp 10s) vía Docker =="
docker run --rm --network "$NETWORK" -v "$PWD":/work -w /work justb4/jmeter \
  -n -t "/work/$JMX" \
  -l "/work/$RESULTS" 2>&1 | tail -20

echo "== Verificando integridad =="
python3 perf/scripts/verify-integrity.py "$RESULTS" localhost 8080 5 "2030-01-01T10:00:00" "2030-01-01T12:00:00" || true

echo "== Comprobación en BD: solapadas activas =="
docker exec sistema-reservas-db-1 psql -U postgres -d sistema_reservas -t -A -c "SELECT count(*) FROM reservas WHERE recurso_id=5 AND estado='ACTIVA' AND fecha_inicio < '2030-01-01T12:00:00' AND fecha_fin > '2030-01-01T10:00:00';"
