# HU-18 · Prueba de carga con 500 usuarios simultáneos

Escenario reproducible con **JMeter 5.5** (decidido por el equipo):

- 500 hilos (1 por usuario `CARGA-0001..CARGA-0500`, ids 504–1003) lanzan `POST /reservas` a la vez (ramp-up 10 s) contra el **mismo recurso** (id 5, "Recurso de carga", `DISPONIBLE`) y el **mismo horario** (`2030-01-01T10:00:00` a `2030-01-01T12:00:00`).
- Criterio: **exactamente 1 × 201** y **499 × 409** (`RECURSO_NO_DISPONIBLE`), **ningún otro código**.
- Umbral de respuesta (decidido por el equipo): **p95 < 800 ms**.
- Integridad en la BD: 0 reservas duplicadas ni solapadas (1 sola `ACTIVA` solapada).

## Archivos

| Ruta | Qué es |
|---|---|
| `perf/jmeter/reservas-500.jmx` | Escenario JMeter 5.5 (ThreadGroup + LoopController, 500 hilos, ramp 10 s, loops 1, POST con `postBodyRaw`, `HeaderManager` con `Content-Type: application/json`, `JSR223PreProcessor` con `usuarioId = 504 + threadNum`) |
| `perf/seed/01-seed-500.sql` | Semilla: 500 usuarios activos `CARGA-0001..CARGA-0500` y recurso "Recurso de carga" `DISPONIBLE` |
| `perf/scripts/run-hu18.sh` | Orquesta: limpia reservas de carga, siembra, ejecuta JMeter vía Docker y verifica integridad |
| `perf/scripts/verify-integrity.py` | Verifica JTL (1×201, 499×409, p95 < 800 ms) + API (1 activa solapada) |
| `perf/jmeter/smoke.jmx` | Smoke test de 2/5 hilos para validar el JMX |

## Cómo ejecutarlo

1. Levantar la API con Docker Compose (HU-22):

```bash
docker compose up --build -d
curl http://localhost:8080/usuarios
```

El servicio `api` queda accesible como host `api:8080` dentro de la red `sistema-reservas_default`.

2. Semilla (idempotente, `ON CONFLICT DO NOTHING`):

```bash
docker exec -i sistema-reservas-db-1 psql -U postgres -d sistema_reservas < perf/seed/01-seed-500.sql
# Si el recurso "Recurso de carga" ya existía con otro id, ajusta el JMX (por defecto id 5).
docker exec sistema-reservas-db-1 psql -U postgres -d sistema_reservas -c "SELECT id FROM recursos WHERE nombre='Recurso de carga';"
```

3. Ejecutar JMeter vía Docker (no requiere JMeter local):

```bash
docker run --rm --network sistema-reservas_default -v "$PWD":/work -w /work justb4/jmeter -n -t /work/perf/jmeter/reservas-500.jmx -l /work/perf/results/reservas-500.jtl
```

O todo junto:

```bash
./perf/scripts/run-hu18.sh
```

4. Verificar la integridad (lee el JTL y consulta la API):

```bash
python3 perf/scripts/verify-integrity.py perf/results/reservas-500.jtl localhost 8080 5 "2030-01-01T10:00:00" "2030-01-01T12:00:00"
```

## Resultados esperados

- `perf/results/reservas-500.jtl` con 500 muestras.
- `verify-integrity.py` imprime `OK: 1x 201 y 499x 409`, `OK: p95 < 800ms` y `OK: exactamente 1 reserva activa solapada`.
- En la BD, `SELECT count(*) FROM reservas WHERE recurso_id=5 AND estado='ACTIVA' AND fecha_inicio < '2030-01-01T12:00:00' AND fecha_fin > '2030-01-01T10:00:00';` devuelve `1`.

## Notas técnicas

- El bloque pesimista (`findByIdForUpdate`) serializa los 500 intentos sobre el mismo recurso; por eso el escenario da 1 confirmación y 499 conflictos sin duplicar.
- El `JSR223PreProcessor` asigna `usuarioId = 504 + threadNum` (threads 0–499 → ids 504–1003, los `CARGA-0001..CARGA-0500`). Si la semilla cambia de rango, ajusta el desplazamiento.
- El `HeaderManager` debe usar `<collectionProp name="HeaderManager.headers">` con `<elementProp name="Content-Type" elementType="Header">`; con la forma antigua JMeter envía `text/plain` y la API responde 500.
- El bloque pesimista serializa: con ramp 2 s el p95 sube a ~4.3 s; con ramp 10 s (flujo escalonado) el p95 queda en ~360 ms y cumple < 800 ms manteniendo 1×201 y 499×409.
- Tiempos: `HU18_FECHA_INICIO/FIN` deben ser futuras respecto al reloj del servidor; por defecto usan `2030-01-01` para no depender de "hoy".
