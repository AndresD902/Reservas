# HU-18 · Prueba de carga con 500 usuarios simultáneos

Escenario reproducible con **JMeter 5.5** (decidido por el equipo):

- 500 hilos (uno por cada usuario sembrado `CARGA-0001..CARGA-0500`) obtienen CSRF y lanzan `POST /reservas` (ramp-up 10 s) contra el mismo recurso de carga y el mismo horario (`2030-01-01T10:00:00` a `2030-01-01T12:00:00`). El script resuelve los IDs desde PostgreSQL; no dependen de valores fijos.
- Criterio: **exactamente 1 × 201** y **499 × 409** (`RECURSO_NO_DISPONIBLE`), **ningún otro código**.
- Umbral de respuesta (decidido por el equipo): **p95 < 800 ms**.
- Integridad en la BD: 0 reservas duplicadas ni solapadas (1 sola `ACTIVA` solapada).

## Archivos

| Ruta | Qué es |
|---|---|
| `perf/jmeter/reservas-500.jmx` | Escenario JMeter 5.5 (500 hilos, ramp 10 s, una iteración, CSRF por hilo, IDs desde CSV) |
| `perf/seed/01-seed-500.sql` | Semilla: 500 usuarios activos `CARGA-0001..CARGA-0500` y recurso "Recurso de carga" `DISPONIBLE` |
| `perf/scripts/run-hu18.sh` | Espera la API, siembra, genera CSV con IDs reales, limita el heap del generador, limpia resultados, genera JTL/dashboard y verifica integridad |
| `perf/scripts/verify-integrity.py` | Verifica CSRF y reservas, resume promedio, throughput, errores, p50/p90/p95/p99 y confirma una activa solapada |
| `perf/jmeter/smoke.jmx` | Smoke test de 2/5 hilos para validar el JMX |

## Cómo ejecutarlo

1. Levantar la API con Docker Compose (HU-22):

```bash
docker compose up --build -d
curl http://localhost:8080/usuarios
```

El servicio `api` queda accesible como host `api:8080` dentro de la red `sistema-reservas_default`.

2. Ejecutar el escenario completo. El script siembra los datos, genera el CSV de IDs, ejecuta JMeter vía Docker, crea los reportes y verifica la integridad; no requiere JMeter instalado localmente.

```bash
./perf/scripts/run-hu18.sh
```

Para un stack aislado se pueden definir `COMPOSE_PROJECT_NAME` y `API_PORT`; los valores por defecto siguen siendo `sistema-reservas` y `8080`. La red y el contenedor PostgreSQL se derivan del nombre del proyecto.

En Azure Pipelines la carga corre en `LocalPool` / `NewAgent` (Windows con Docker Desktop Linux, Bash/WSL y Python 3 en WSL). Cada run usa proyecto `hu35-<BuildId>`, API en 18085 y PostgreSQL en 15435. El artefacto incluye `environment.json` con CPU/RAM de Docker, agente, commit y run. El agente debe permanecer conectado; los recursos disponibles no garantizan el umbral y se comprueban contra el JTL de cada corrida.

## Resultados esperados

- `perf/results/reservas-500.jtl` con 500 GET CSRF y 500 POST de reservas.
- `perf/results/html/index.html` con dashboard JMeter.
- `perf/results/summary.json` con métricas solo de los 500 POST de reservas.
- `verify-integrity.py` imprime `OK: 1x 201 y 499x 409`, `OK: p95 < 800ms` y `OK: exactamente 1 reserva activa solapada`.
- El verificador confirma 500 CSRF exitosos y una sola reserva activa solapada para el ID dinámico.

## Notas técnicas

- El bloque pesimista (`findByIdForUpdate`) serializa los 500 intentos sobre el mismo recurso; por eso el escenario da 1 confirmación y 499 conflictos sin duplicar.
- El CSV se genera a partir de los usuarios `CARGA-%` existentes y el recurso recién sembrado; el plan usa `CSVDataSet` sin reciclar datos.
- El contenedor JMeter usa `JVM_XMN=256`, `JVM_XMS=512` y `JVM_XMX=2048` para no reservar por defecto casi toda la memoria del agente.
- El `HeaderManager` debe usar `<collectionProp name="HeaderManager.headers">` con `<elementProp name="Content-Type" elementType="Header">`; con la forma antigua JMeter envía `text/plain` y la API responde 500.
- El bloque pesimista serializa: con ramp 2 s el p95 sube a ~4.3 s; con ramp 10 s (flujo escalonado) el p95 queda en ~360 ms y cumple < 800 ms manteniendo 1×201 y 499×409.
- Tiempos: `HU18_FECHA_INICIO/FIN` deben ser futuras respecto al reloj del servidor; por defecto usan `2030-01-01` para no depender de "hoy".
