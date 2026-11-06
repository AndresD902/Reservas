# Análisis DAST con OWASP ZAP

Este directorio contiene el escaneo dinámico pasivo de la API y la interfaz web del sistema de reservas con OWASP ZAP. Implementa la historia HU-26.

## Alcance

- **API:** escaneo de los endpoints descritos en `/v3/api-docs` con `zap-api-scan.py` en modo seguro (`-S`). Importa el contrato OpenAPI y ejecuta exploración pasiva; no lanza ataques de inyección activos.
- **Interfaz web:** escaneo de la SPA servida por Nginx con `zap-baseline.py` y el client spider (`-j`).
- **Entorno:** Docker Compose con PostgreSQL 16, datos semilla ficticios y una base desechable. Al terminar, el script elimina el volumen con `docker compose down -v`.
- **Fuera de alcance:** pruebas de penetración manuales, escaneo de infraestructura, análisis de dependencias y flujos autenticados. HU-25 existe, pero este escaneo no inicia sesión.

## Requisitos

- Docker con Docker Compose v2.
- `curl` y Python 3.
- En WSL con Docker Desktop, el binario de Docker puede ser `docker.exe`:

```bash
DOCKER_BIN=docker.exe bash security/zap/run-baseline.sh
```

## Ejecución local

```bash
bash security/zap/run-baseline.sh
```

El script hace todo el ciclo:

1. Levanta `db`, `api` y `frontend` con `docker compose --profile frontend up -d --build`.
2. Espera a que la API y la interfaz respondan.
3. Ejecuta el escaneo de la API contra `http://api:8080/v3/api-docs`.
4. Ejecuta el escaneo de la interfaz contra `http://frontend`.
5. Verifica los hallazgos contra el umbral configurado.
6. Publica los reportes y elimina el entorno y el volumen de la base.

Variables útiles:

| Variable | Valor por defecto | Uso |
|---|---|---|
| `ZAP_FAIL_ON` | `medium` | Riesgo mínimo que hace fallar la verificación: `info`, `low`, `medium`, `high` o `none`. |
| `KEEP` | `0` | Con `KEEP=1` deja el entorno en ejecución para inspeccionarlo. |
| `DOCKER_BIN` | `docker` | Binario de Docker; en WSL puede ser `docker.exe`. |
| `ZAP_IMAGE` | `ghcr.io/zaproxy/zaproxy:stable` | Imagen de ZAP. |
| `COMPOSE_PROJECT` | `sistema-reservas` | Nombre del proyecto de Compose; determina la red `sistema-reservas_default`. |

## Reportes

Los archivos generados quedan en `security/zap/reports/`:

| Archivo | Contenido |
|---|---|
| `api-zap.html`, `api-zap.json` | Reporte del escaneo de la API. |
| `ui-zap.html`, `ui-zap.json` | Reporte del escaneo de la interfaz. |
| `summary.md` | Resumen generado por `verify-findings.py`. |

El script hace escribible el directorio de reportes (`chmod 0777`) porque el contenedor de ZAP se ejecuta con otro UID y necesita crear allí los archivos HTML y JSON.

El análisis versionado de la ejecución de HU-26 está en `security/zap/reports/hu-26-zap-baseline.md`.

## Reglas y umbral

`security/zap/rules.tsv` ajusta el nivel de reglas conocidas del laboratorio. El token `XSRF-TOKEN` se marca como informativo porque la SPA debe leerlo para el patrón CSRF de doble envío; HSTS se ignora porque el entorno local usa HTTP.

`verify-findings.py` lee ambos reportes JSON y falla si alguna alerta alcanza el umbral de `ZAP_FAIL_ON`. Por defecto el umbral es `medium`: un hallazgo de severidad media, alta o crítica rompe la ejecución; los de severidad baja e informativa se registran y se trian en el reporte.

## Integración continua

El workflow `.github/workflows/zap-baseline.yml` ejecuta el mismo script en cada pull request y push a `main`, y publica los reportes como el artefacto `reporte-seguridad-zap`.

## Limitaciones

- ZAP Baseline no demuestra ausencia de vulnerabilidades: solo cubre las reglas pasivas y las rutas que logra descubrir.
- El escaneo de la API recorre las operaciones del contrato OpenAPI, incluidas las de escritura. Por eso se ejecuta contra una base desechable.
- La SPA se explora con el client spider, pero las rutas que dependen de una sesión autenticada no se cubren.
- Los resultados dependen de la versión de ZAP y del contrato OpenAPI del commit analizado.
