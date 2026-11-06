# Resultados del análisis DAST con OWASP ZAP

## Identificación de la ejecución

| Campo | Valor |
|---|---|
| Historia | HU-26 · Pruebas de seguridad automatizadas con OWASP ZAP |
| Fecha | 2026-11-06 |
| Rama | `feature/hu-26-owasp-zap` |
| Base | `8262d24` de `main` |
| Herramienta | OWASP ZAP 2.17.0, imagen `ghcr.io/zaproxy/zaproxy@sha256:9624669db72f54d86c4b46a7064518bd9bf79d8c61a4c31befa9923489f328af` |
| Entorno | Docker Desktop 29.7.2, Docker Compose, PostgreSQL 16, API Spring Boot y Nginx 1.27.5 |
| Objetivos | `http://api:8080` y `http://frontend` dentro de la red `sistema-reservas_default` |
| Umbral | `medium`: Media, Alta o Crítica hacen fallar la verificación |

## Objetivo y alcance

El análisis busca una primera línea base de seguridad dinámica, reproducible y ligada al commit, sin convertirla en una barrera desproporcionada para el equipo.

- **API:** escaneo seguro de los endpoints del contrato OpenAPI con `zap-api-scan.py -S`. Importa `/v3/api-docs` y ejecuta exploración pasiva; no lanza ataques activos de inyección.
- **Interfaz web:** `zap-baseline.py` con el client spider sobre la SPA servida por Nginx.
- **Entorno:** Docker Compose con datos semilla ficticios. La base es desechable y el script la elimina con `docker compose down -v` al terminar.
- **Fuera de alcance:** pruebas de penetración manuales, escaneo de infraestructura, análisis de dependencias, TLS/HSTS y flujos autenticados. HU-25 ya existe, pero este escaneo no inicia sesión.

## Ejecución

```bash
# Linux o GitHub Actions
bash security/zap/run-baseline.sh

# WSL con Docker Desktop
DOCKER_BIN=docker.exe bash security/zap/run-baseline.sh
```

El script levanta `db`, `api` y `frontend`, espera a que respondan, ejecuta los dos escaneos, aplica el umbral y publica los reportes. El workflow `.github/workflows/zap-baseline.yml` reproduce el procedimiento en cada pull request y push a `main`, y publica el artefacto `reporte-seguridad-zap`.

## Resultados finales

No hubo hallazgos de severidad Media, Alta ni Crítica. El umbral `medium` no se superó.

| Reporte | Sitio | ID | Hallazgo | Riesgo | Confianza | Instancias |
|---|---|---|---|---|---|---|
| `api-zap.json` | `http://api:8080` | 10010 | Cookie No HttpOnly Flag | Low | 2 | 5 |
| `api-zap.json` | `http://api:8080` | 90004 | Cross-Origin-Resource-Policy Header Missing or Invalid | Low | 2 | 5 |
| `api-zap.json` | `http://api:8080` | 100000 | A Client Error response code was returned by the server | Informational | 3 | 19 |
| `api-zap.json` | `http://api:8080` | 10049 | Non-Storable Content | Informational | 2 | 5 |
| `api-zap.json` | `http://api:8080` | 10112 | Session Management Response Identified | Informational | 2 | 15 |
| `ui-zap.json` | `http://frontend` | 10010 | Cookie No HttpOnly Flag | Low | 2 | 3 |
| `ui-zap.json` | `http://frontend` | 10027 | Information Disclosure - Suspicious Comments | Informational | 2 | 1 |
| `ui-zap.json` | `http://frontend` | 10109 | Modern Web Application | Informational | 2 | 4 |
| `ui-zap.json` | `http://frontend` | 10049 | Non-Storable Content | Informational | 2 | 2 |
| `ui-zap.json` | `http://frontend` | 10049 | Storable and Cacheable Content | Informational | 2 | 4 |

## Triage de hallazgos

| ID | Hallazgo | Evidencia | Impacto | Decisión y verificación |
|---|---|---|---|---|
| 10010 | `XSRF-TOKEN` sin `HttpOnly` en la API y en la SPA | `Set-Cookie: XSRF-TOKEN` en `/auth/login`, `/auth/csrf`, `/reservas`, `/usuarios` y `/recursos` | La cookie de CSRF es legible por JavaScript. La cookie de sesión `JSESSIONID` sí es `HttpOnly`; el token de doble envío necesita ser legible por la SPA | Aceptado por diseño. La regla queda documentada como informativa en `security/zap/rules.tsv` |
| 90004 | Falta `Cross-Origin-Resource-Policy` en las respuestas de la API | `GET /v3/api-docs`, `/usuarios`, `/recursos`, `/reservas` y `/auth/csrf` | Reduce las defensas contra ataques de canal lateral tipo Spectre; el impacto es bajo en una API JSON de laboratorio | Aceptado para esta fase. Mejora propuesta: declarar `Cross-Origin-Resource-Policy: same-origin` en la configuración de Spring Security |
| 100000 | Respuestas 4xx esperadas durante el escaneo | `401`, `403`, `404` y `400` en rutas protegidas o con identificadores de ejemplo | No es una vulnerabilidad: son respuestas negativas que el escáner provoca al recorrer el contrato | Sin acción. El contrato de error responde con el cuerpo homogéneo |
| 10049 | `no-store` en respuestas de la API | `Cache-Control: no-store` en respuestas sensibles | Evita que intermediarios cacheen datos de usuarios o reservas | Esperado y correcto. Sin acción |
| 10112 | Gestión de sesión identificada | Presencia de `XSRF-TOKEN` | ZAP reconoce el mecanismo de sesión de la SPA | Sin acción. Es una observación de reconocimiento |
| 10027 | Comentario sospechoso en el bundle | Cadena `...to:\`/admin\`` dentro de `assets/index-*.js` | No hay un comentario ni una credencial expuesta; coincide con una ruta de la SPA | Falso positivo. Sin acción |
| 10109 | Aplicación web moderna | Scripts de módulo y rutas de la SPA | Es la naturaleza esperada de una SPA con React y Vite | Sin acción |

## Mitigaciones aplicadas

| Hallazgo inicial | Corrección | Verificación |
|---|---|---|
| `500` en `GET /reservas/rango` y `GET /recursos/{id}/disponibilidad` con fechas con formato inválido | Se agregó el manejo de `MethodArgumentTypeMismatchException` en `GlobalExceptionHandler`: ahora responde `400` con `SOLICITUD_INVALIDA` | `./gradlew test --tests "*GlobalExceptionHandlerTest"`; el escaneo final ya no reporta el `500` ni `Application Error Disclosure` |
| `CSP` ausente y anti-clickjacking ausente (Media) | Se agregaron `Content-Security-Policy` y `X-Frame-Options` en `frontend/nginx.conf` | El escaneo final marca `PASS` en las reglas 10020, 10038 y 10055 |
| `X-Content-Type-Options`, `Permissions-Policy`, `COOP`, `COEP`, `CORP` y versión de Nginx expuesta (Low) | Se agregaron las cabeceras correspondientes y `server_tokens off` en `frontend/nginx.conf` | El escaneo final marca `PASS` en las reglas 10021, 10036, 10063 y 90004 de la SPA |
| `Application Error Disclosure` en las respuestas `500` | Desaparece al corregir el manejo del parámetro inválido | Reporte final sin esa alerta |

## Evidencia

| Evidencia | Ubicación |
|---|---|
| Reporte HTML de la API | `security/zap/reports/api-zap.html` (generado y publicado como artefacto) |
| Reporte JSON de la API | `security/zap/reports/api-zap.json` (generado y publicado como artefacto) |
| Reporte HTML de la interfaz | `security/zap/reports/ui-zap.html` (generado y publicado como artefacto) |
| Reporte JSON de la interfaz | `security/zap/reports/ui-zap.json` (generado y publicado como artefacto) |
| Resumen generado por el umbral | `security/zap/reports/summary.md` |
| Artefacto de CI | `reporte-seguridad-zap` del workflow `DAST OWASP ZAP` |
| Procedimiento reproducible | `security/zap/run-baseline.sh` y `security/zap/README.md` |

Los reportes HTML y JSON se regeneran en cada ejecución y no se versionan para evitar ruido; el artefacto del workflow conserva la evidencia de cada run.

## Limitaciones

- ZAP Baseline no demuestra ausencia de vulnerabilidades: solo cubre reglas pasivas y las rutas que logra descubrir.
- El escaneo de la API recorre las operaciones del contrato OpenAPI, incluidas las de escritura. Por eso se ejecuta contra una base desechable y se elimina al terminar.
- No se cubren flujos autenticados. El login de HU-25 y las pantallas de administración requieren una sesión, un token CSRF y un contexto de autenticación que este primer alcance no configura.
- La SPA se explora con el client spider. Las rutas que dependen de una sesión o de datos creados en la sesión pueden quedar fuera.
- El entorno es HTTP local: HSTS no aplica y las comprobaciones de TLS no forman parte de este análisis.
- Los resultados dependen de la versión de ZAP y del contrato OpenAPI del commit analizado.

## Reflexión

1. **¿Por qué `host.docker.internal` en lugar de `localhost`?** Porque `localhost` dentro de un contenedor apunta al propio contenedor, no al equipo anfitrión. En esta adaptación la API y la interfaz también corren en contenedores de la misma red de Compose, así que los objetivos son `http://api:8080` y `http://frontend`, que es aún más determinista que depender del host.
2. **¿Cuál es la diferencia entre ejecutar ZAP a mano y desde el pipeline?** A mano sirve para explorar y depurar, pero no deja una evidencia reproducible ligada a un commit. El pipeline ejecuta siempre el mismo procedimiento, aplica el mismo umbral y publica los reportes como artefacto de la ejecución.
3. **¿Por qué Baseline puede generar advertencias sin que sea correcto bloquear el despliegue?** Porque las reglas pasivas tienen falsos positivos y el riesgo depende del contexto. Bloquear por cualquier advertencia produce fatiga y presiona a ignorar el reporte. Por eso el proyecto fija un umbral (`medium`) y obliga a triar los hallazgos bajos e informativos.
4. **¿Qué evidencia demuestra que el análisis pertenece a una ejecución específica?** El commit y la rama analizados, el run del workflow, el artefacto `reporte-seguridad-zap`, las marcas de tiempo de los reportes y el resumen generado por `verify-findings.py`.
5. **¿Qué riesgos aparecen al ejecutar una aplicación contra una base real durante un DAST?** Puede crear, modificar o borrar datos, mezclar datos ficticios con datos reales y exponer información sensible en los reportes. Aquí se mitiga con una base desechable, datos semilla ficticios, credenciales locales y `docker compose down -v` al terminar.

## Conclusión

La primera línea base DAST queda automatizada, es reproducible y está ligada al commit. El escaneo no encontró hallazgos de severidad Media o superior. Se corrigieron el `500` por parámetros de fecha inválidos y las cabeceras de seguridad de la SPA; los dos hallazgos bajos restantes están justificados y documentados. La siguiente mejora recomendada es agregar `Cross-Origin-Resource-Policy` a la API y ampliar el alcance a un flujo autenticado cuando el equipo decida asumir el costo de mantener ese contexto.
