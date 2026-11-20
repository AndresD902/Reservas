---
description: "Guía operativa para planificar, ejecutar y presentar la evidencia de calidad del sistema de reservas: plan de pruebas, JUnit, PIT, SonarQube, Gherkin, OWASP ZAP, JMeter y Azure Pipelines."
---
# Guía de evidencia de calidad

## Propósito y alcance

Esta guía reúne el plan de pruebas y el procedimiento para producir la evidencia que exige la evaluación del proyecto. Cubre los ítems A a I: presentación, plan de pruebas, JUnit, PIT, SonarQube, Gherkin, OWASP ZAP, JMeter y Azure Pipelines.

La guía describe **cómo ejecutar y demostrar** cada verificación. No reemplaza las pruebas ni los reportes: los comandos producen los artefactos y esta guía fija dónde quedan, qué umbral deben cumplir y cómo se interpretan.

Queda fuera de alcance el despliegue a producción, la prueba de penetración manual y el análisis de infraestructura.

## Estado actual y trabajo pendiente

| Ítem | Estado de partida | Qué falta |
|---|---|---|
| A. Presentación | El README describe problema, arquitectura y rutas | Guion de demo, datos y evidencia consolidada |
| B. Plan de pruebas | Criterios dispersos en el backlog y los estándares | Documento único con alcance, tipos, riesgos y criterios |
| C. JUnit | 181 pruebas, 0 fallos en la última corrida local | Reporte adjunto y análisis de casos positivos/negativos |
| D. PIT | Configurado con umbral 80/90 sobre `service.impl` | Reporte versionado y análisis de supervivientes |
| E. SonarQube | Configuración Gradle y YAML de Azure añadidos; el variable group `reservas-quality` ya existe | Crear/autorizar el pipeline, ejecutar el análisis y registrar dashboard, quality gate y triage |
| F. Gherkin | Dos escenarios de login, 0 fallos | Ejecución en Azure y publicación de videos/reportes |
| G. OWASP ZAP | Reporte HU-26 con triage | Ejecución en Azure y actualización de evidencia |
| H. JMeter | Escenario de 500 usuarios y scripts | Dashboard, métricas reales y análisis |
| I. Azure Pipelines | GitHub Actions y `azure-pipelines.yml` | Crear/autorizar el pipeline en Azure y publicar evidencia de su ejecución |

## Plan de pruebas

### Objetivo

Verificar que el sistema cumple los requisitos funcionales y de calidad con evidencia reproducible, y que las regresiones en las reglas de negocio, la autenticación, la interfaz y el rendimiento hacen fallar el pipeline.

### Alcance

Incluye:

- API REST de autenticación, usuarios, recursos, reservas y disponibilidad.
- Reglas de negocio de reserva, cancelación, límite de reservas activas, solapamiento y finalización.
- Persistencia PostgreSQL, migraciones Flyway y bloqueos de concurrencia.
- Interfaz web de reserva, mis reservas, login y administración.
- Contrato OpenAPI y formato homogéneo de errores.
- Seguridad dinámica pasiva y rendimiento bajo carga.

No incluye:

- Despliegue a producción.
- Pruebas de penetración manuales.
- Análisis de infraestructura o de dependencias.
- Flujos autenticados completos en ZAP; el escaneo actual es pasivo.
- Cobertura de la SPA en SonarQube; la cobertura publicada es la del backend.

### Funcionalidades y componentes evaluados

| Componente | Qué se verifica | Dónde vive |
|---|---|---|
| Autenticación | Login, logout, sesión y CSRF | `AuthController`, `AuthServiceImpl`, pruebas de login |
| Usuarios | Alta, consulta, estado y duplicados | `UsuarioController`, `UsuarioServiceImpl` |
| Recursos | Alta, edición, estado, disponibilidad y mantenimiento | `RecursoController`, `RecursoServiceImpl` |
| Reservas | Creación, cancelación, límites, solapamiento y consultas | `ReservaController`, `ReservaServiceImpl` |
| Persistencia | Esquema, restricciones, migraciones y bloqueos | Flyway, repositorios, `postgresTest` |
| Interfaz | Reserva, mis reservas, login y panel administrativo | `frontend/src` |
| Contrato | Rutas, DTOs, códigos y errores | `docs/api-spec.yml`, `OpenApiContractTest` |
| Operación | Compilación, pruebas, análisis y artefactos | `azure-pipelines.yml`, GitHub Actions |

### Tipos de prueba

| Tipo | Herramienta | Comando | Reporte |
|---|---|---|---|
| Unitarias | JUnit 5, Mockito, AssertJ | `./gradlew test` | `build/reports/tests/test` |
| Integración | Spring Boot, MockMvc, H2 | `./gradlew test` | `build/reports/tests/test` |
| Concurrencia | JUnit + PostgreSQL real | `TEST_DB_URL=... ./gradlew postgresTest` | `build/reports/tests/postgresTest` |
| Aceptación | Cucumber-JVM + Gherkin | `./gradlew acceptanceTest` | `build/reports/tests/acceptanceTest` |
| E2E HTTP | Spring Boot + HTTP real | `./gradlew e2eTest` | `build/reports/tests/e2eTest` |
| E2E navegador | Serenity + Playwright | `./gradlew serenityE2ETest` | `target/site/serenity` |
| E2E Gherkin | Cucumber + Playwright | `./gradlew e2eGherkinTest` | `build/reports/cucumber`, `build/e2e-videos` |
| Mutación | PIT | `./gradlew pitest` | `build/reports/pitest` |
| Cobertura | JaCoCo | `./gradlew jacocoTestReport` | `build/reports/jacoco/test` |
| Estático | SonarQube | `./gradlew sonar` | Dashboard de SonarQube |
| DAST | OWASP ZAP | `bash security/zap/run-baseline.sh` | `security/zap/reports` |
| Rendimiento | JMeter | `bash perf/scripts/run-hu18.sh` | `perf/results` |
| Frontend | Vitest + Testing Library | `cd frontend && npm test` | Salida de Vitest |
| Integración continua | Azure Pipelines | Push o PR | Artefactos del run |

### Entornos

| Entorno | Uso | Requisito |
|---|---|---|
| H2 en memoria | Suite rápida de backend | Ninguno; lo levanta Spring Boot |
| PostgreSQL real | Concurrencia y E2E HTTP | `TEST_DB_URL` o Docker Compose |
| Docker Compose | E2E de navegador, ZAP y JMeter | Docker con Compose v2 |
| Azure `ubuntu-latest` | Pipeline oficial | Proyecto de Azure DevOps y service connection |
| Local con Vite | Demo y desarrollo de la SPA | Node 24 y API en `:8080` |

### Riesgos identificados

| Riesgo | Impacto | Mitigación |
|---|---|---|
| Solapamiento o duplicación de reservas bajo concurrencia | Alto | Bloqueo pesimista y prueba con PostgreSQL real |
| Sesión, CSRF o login mal configurados | Alto | Pruebas de autenticación, E2E de login y revisión de cookies |
| Diferencias entre H2 y PostgreSQL | Medio | `postgresTest` para bloqueos y concurrencia |
| Flakiness de navegador o de red | Medio | Esperas explícitas, modo headless y videos de respaldo |
| Deriva entre el entorno local y Azure | Medio | Docker Compose y versiones fijas de JDK, Node y Gradle |
| Deuda legacy que hace fallar el quality gate | Medio | Gate sobre New Code y triage documentado |
| ZAP no cubre flujos autenticados | Medio | Declararlo como limitación y ampliar el alcance en una fase posterior |
| Rendimiento dependiente del agente | Medio | Documentar vCPU/RAM y repetir en un entorno equivalente |
| Secretos filtrados en logs o YAML | Alto | Variable group secreto y revisión de logs |
| Reportes generados pero no publicados | Bajo | Artefactos obligatorios en el pipeline |

### Criterios de éxito

| Verificación | Criterio |
|---|---|
| JUnit e integración | Todas las pruebas en verde |
| Cobertura JaCoCo | Al menos 90 % de líneas en `com.institucion.reservas.service.impl.*` |
| PIT | Mutation score de al menos 80 % y cobertura de al menos 90 % en el gate |
| SonarQube | Quality gate aprobado; hallazgos relevantes triados |
| ZAP | Sin hallazgos Medium, High ni Critical; los Low e Informational se justifican |
| Gherkin | Login exitoso y login fallido con reintento en verde |
| JMeter | p95 < 800 ms, 1×201, 499×409 y una sola reserva activa solapada |
| Azure Pipelines | Etapas verdes en `main` y run de fallo controlado documentado |
| Presentación | Recorrido crítico demostrado y evidencia enlazada |

## Evidencia JUnit (C)

### Qué se evalúa

Las pruebas unitarias y de integración cubren servicios, controladores, manejo global de errores, contrato OpenAPI, paginación, autenticación y finalización automática de reservas.

Casos representativos:

- Positivos: creación de usuario, recurso y reserva; consulta; cancelación; login válido.
- Negativos: usuario inactivo, recurso no disponible, fechas inválidas, límite de reservas activas, solapamiento, recurso en mantenimiento, credenciales inválidas y recurso inexistente.
- Bordes: intervalos semiabiertos, límites exactos, nulos y estados `CANCELADA`/`FINALIZADA`.

### Ejecución

```bash
./gradlew test
```

Reportes:

- `build/reports/tests/test/index.html`
- `build/test-results/test/*.xml`

### Análisis esperado

- Número de pruebas ejecutadas, fallos y omitidas.
- Casos positivos y negativos por componente.
- Brechas de cobertura y pruebas que faltan.
- Mejoras: mantener nombres por comportamiento, aislar dependencias y agregar casos límite cuando aparezca un defecto.

## Evidencia PIT (D)

### Configuración actual

`build.gradle` configura PIT sobre `com.institucion.reservas.service.impl.*`, con pruebas de `com.institucion.reservas.service.**`, umbral de mutación 80 y cobertura 90. La tarea `check` depende de `pitest`.

### Ejecución

```bash
./gradlew pitest
```

Reporte:

- `build/reports/pitest/index.html`
- `build/reports/pitest/mutations.xml`

Para la evidencia ampliada se agrega una tarea `pitestFull` sobre servicios, controladores y excepciones, sin umbral, con reporte en `build/reports/pitest-full`. No forma parte de `check`.

### Análisis de supervivientes

Cada mutante sobreviviente se registra con:

| Campo | Contenido |
|---|---|
| Clase | Clase donde se aplicó el mutador |
| Línea | Línea afectada |
| Mutador | Operación mutada |
| Estado | `SURVIVED`, `NO_COVERAGE` o `TIMED_OUT` |
| Causa | Por qué la suite no lo detecta |
| Acción | Prueba agregada, exclusión justificada o riesgo aceptado |

Si el gate queda en 100 %, se documenta que no hay supervivientes en ese alcance y se adjunta el reporte como evidencia.

## Evidencia SonarQube (E)

### Configuración

`build.gradle` aplica `org.sonarqube` 7.6.0.18749 y configura `projectKey=AndresD902_Reservas`, organización `andresd902`, URL `https://sonarcloud.io` y nombre `Sistema de Reservas`. El análisis importa el XML de JaCoCo de `build/reports/jacoco/test/jacocoTestReport.xml`. Se excluyen DTOs, entidades, mappers, configuración y la clase de arranque del cálculo de cobertura; DTOs, entidades y mappers también se excluyen del cálculo de duplicación.

El token se lee exclusivamente desde `SONAR_TOKEN`; no debe guardarse en el repositorio ni imprimirse en logs. En Azure DevOps, el variable group `reservas-quality` es la fuente de esa variable. `sonar.qualitygate.wait=true` hace que la tarea falle cuando SonarCloud rechaza el quality gate; el timeout configurado es de 300 segundos.

El primer análisis de HU-33 falló por `new_security_rating=3`, con requisito máximo `1`. SonarCloud señaló dos referencias `gradle/actions/setup-gradle@v4` sin SHA completo y `npm ci` sin `--ignore-scripts` en `.github/workflows/ci.yml`. El workflow ahora fija setup-gradle a un SHA verificado y omite scripts de instalación durante `npm ci`; falta confirmar el resultado del siguiente análisis.

### Ejecución

```bash
./gradlew test jacocoTestReport sonar
```

### Interpretación

| Métrica | Qué significa | Acción |
|---|---|---|
| Bugs | Defectos probables | Corregir los de impacto alto |
| Vulnerabilidades | Riesgos de seguridad | Corregir o justificar |
| Code Smells | Problemas de mantenibilidad | Corregir los de impacto alto/medio |
| Deuda técnica | Tiempo estimado de corrección | Priorizar y documentar |
| Duplicación | Código repetido | Revisar si oculta un problema de diseño |
| Cobertura | Porcentaje cubierto por pruebas | Comparar con el umbral JaCoCo |

Las exclusiones de DTOs, entidades, mappers y configuración se justifican para que la cobertura refleje lógica de negocio.

## Evidencia Gherkin (F)

### Escenarios existentes

- `src/test/resources/features/e2e/admin-login-success.feature`
- `src/test/resources/features/e2e/admin-login-retry.feature`

El primer escenario cubre login, acceso al panel y logout. El segundo cubre contraseña incorrecta, ausencia de sesión y reintento exitoso.

### Ejecución

```bash
docker compose --profile frontend up -d --build
./gradlew e2eGherkinTest
```

Para ver el navegador en vivo:

```bash
./gradlew e2eGherkinTest -Pplaywright.headless=false -Pplaywright.slowmo=500
```

Reportes:

- `build/reports/cucumber/e2e.html`
- `build/reports/cucumber/e2e.json`
- `build/reports/cucumber/e2e.xml`
- `build/e2e-videos/*.webm`

### Evidencia a presentar

- Escenarios en Gherkin.
- Resultado de la ejecución automatizada.
- Video por escenario.
- Errores encontrados y validaciones realizadas.

## Evidencia OWASP ZAP (G)

### Alcance

El escaneo DAST pasivo cubre la API importando el contrato OpenAPI y la SPA servida por Nginx. Se ejecuta contra un entorno desechable de Docker Compose. No cubre flujos autenticados ni pruebas de penetración manuales.

### Ejecución

```bash
bash security/zap/run-baseline.sh
```

Reportes:

- `security/zap/reports/api-zap.html` y `.json`
- `security/zap/reports/ui-zap.html` y `.json`
- `security/zap/reports/summary.md`

El análisis versionado está en `security/zap/reports/hu-26-zap-baseline.md`.

### Interpretación

Cada alerta se clasifica como vulnerabilidad confirmada, falso positivo, riesgo aceptado o mejora pendiente. Los hallazgos Medium, High o Critical hacen fallar el umbral; los Low e Informational se documentan.

## Evidencia JMeter (H)

### Escenario

- 500 usuarios virtuales, uno por usuario `CARGA-0001..CARGA-0500`.
- Ramp-up de 10 segundos.
- Una iteración por usuario.
- Todos reservan el mismo recurso y el mismo horario.
- Resultado esperado: 1×201 y 499×409.
- Umbral: p95 < 800 ms.
- Integridad: una sola reserva activa solapada.

### Ejecución

```bash
docker compose up -d --build
bash perf/scripts/run-hu18.sh
```

El script debe generar:

- `perf/results/reservas-500.jtl`
- `perf/results/html/index.html`
- `perf/results/summary.json`

### Métricas a presentar

| Métrica | Cómo se obtiene |
|---|---|
| Usuarios virtuales | Configuración del Thread Group |
| Ramp-up | Configuración del Thread Group |
| Duración | Dashboard o suma de muestras |
| Tiempo promedio | Dashboard o `summary.json` |
| Throughput | Muestras por segundo |
| Error bruto | Porcentaje de respuestas no exitosas; incluye los 409 esperados |
| Error inesperado | Respuestas fuera del contrato, como 500 |
| p50, p90, p95, p99 | Percentiles del JTL o dashboard |

### Interpretación

JMeter marca los 499 HTTP 409 como fallos HTTP. El informe debe separar:

- **Conflictos esperados:** 499 respuestas 409 por la regla de recurso no disponible.
- **Errores inesperados:** respuestas 500 o fuera del contrato.

El cuello de botella principal es el bloqueo pesimista `findByIdForUpdate`, que serializa los intentos sobre el mismo recurso.

## Azure Pipelines (I)

### Preparación

1. El proyecto `andresposada274/Reservas` y la service connection GitHub de `AndresD902` ya existen.
2. El variable group `reservas-quality` ya existe y contiene `SONAR_TOKEN` como secreto.
3. Crear o autorizar el pipeline desde `azure-pipelines.yml` siguiendo la sección de configuración manual.
4. El agente `ubuntu-latest` usa JDK 21; no requiere Docker Compose para este pipeline.

### Etapas propuestas

| Etapa | Qué ejecuta | Artefactos |
|---|---|---|
| `BackendQuality` | JUnit, PIT, JaCoCo, aceptación, E2E HTTP y SonarCloud | Reportes de pruebas, cobertura, mutación y resultado del quality gate |

### Pipeline versionado

El pipeline versionado en `azure-pipelines.yml` se dispara con cambios y pull requests hacia `main`. Ejecuta la suite de backend, genera el reporte JaCoCo y ejecuta SonarCloud con el secreto `SONAR_TOKEN` del variable group `reservas-quality`. El checkout usa `fetchDepth: 0` para descargar el historial completo: Sonar necesita el blame para distinguir issues existentes de issues nuevos y evaluar correctamente el quality gate. El análisis espera el resultado del quality gate y, por ello, el job queda fallido si el gate no se aprueba.

Este pipeline cubre el backend y SonarCloud; no sustituye los jobs de GitHub Actions para frontend, navegador, seguridad o rendimiento.

### Configuración manual en Azure DevOps

1. Abrir **Pipelines → New pipeline** y seleccionar **GitHub**.
2. Elegir la conexión existente de GitHub para `AndresD902`; si se solicita, autorizarla para acceder al repositorio `AndresD902/Reservas`.
3. Seleccionar el repositorio y elegir **Existing Azure Pipelines YAML file**.
4. Seleccionar la rama que contiene el archivo (`feature/hu-33-sonar-quality-gate` antes del merge) y la ruta `/azure-pipelines.yml`; guardar y ejecutar.
5. Si Azure solicita autorización de recursos, autorizar la conexión GitHub y el variable group `reservas-quality` para este pipeline. También puede hacerse en **Pipelines → Library → reservas-quality → Pipeline permissions**, habilitando este pipeline.
6. Confirmar que el primer run termina con `BackendQuality` exitoso y que el dashboard SonarCloud muestra cobertura JaCoCo y quality gate. Revisar el log para confirmar que no se expone el valor del token.
7. En GitHub, abrir **Settings → Rules → Rulesets** y crear un ruleset para `main` que requiera el status check publicado por este pipeline. El check aparecerá para seleccionarlo después de la primera ejecución de validación de PR.

### Evidencia de ejecución

- URL del run de `main` con el job de SonarCloud y el quality gate aprobados.
- URL del proyecto SonarCloud con cobertura y métricas visibles.
- Confirmación de que Build validation quedó como requerida en la política de `main`.
- Clasificación y acción para bugs, vulnerabilidades, code smells, deuda y duplicación observados.

## Presentación y demo (A)

### Guion de diapositivas

1. Problema que resuelve el sistema.
2. Funcionalidades principales.
3. Arquitectura y tecnologías.
4. Recorrido crítico del usuario.
5. Plan de pruebas.
6. JUnit.
7. PIT.
8. SonarQube.
9. Gherkin E2E.
10. OWASP ZAP.
11. JMeter.
12. Azure Pipelines.
13. Conclusiones y mejoras.

### Recorrido crítico

1. Abrir `/login` e intentar con contraseña incorrecta.
2. Iniciar sesión con `admin@example.com` y `admin123`.
3. Mostrar `/admin/usuarios` y `/admin/recursos`.
4. Crear un usuario y un recurso de demo.
5. Abrir `/reservar` y reservar un intervalo.
6. Abrir `/mis-reservas` y cancelar la reserva.
7. Intentar el mismo intervalo dos veces y mostrar `409 RECURSO_NO_DISPONIBLE`.
8. Cerrar sesión.

### Precondiciones

```bash
docker compose --profile frontend up -d --build
./gradlew test
```

### Plan de respaldo

Si la demo en vivo falla, usar el video de los escenarios Gherkin y las capturas de los reportes. La presentación no debe depender de una ejecución en vivo.

## Matriz de trazabilidad

| Ítem | Historia | Card | Evidencia |
|---|---|---|---|
| A. Presentación | `HU-38` | [HU-38](https://trello.com/c/KFVgnBQg) | Guion, video o capturas, reportes enlazados |
| B. Plan de pruebas | `HU-37` | [HU-37](https://trello.com/c/ZaIwXnKX) | Esta guía |
| C. JUnit | `HU-14`, `HU-15`, `HU-16` | Cards existentes | `build/reports/tests/test`, análisis de casos |
| D. PIT | `HU-34` | [HU-34](https://trello.com/c/jCuoecqj) | `build/reports/pitest`, tabla de supervivientes |
| E. SonarQube | `HU-33` | [HU-33](https://trello.com/c/tDqu9wJQ) | Dashboard, quality gate, triage |
| F. Gherkin | `HU-31`, `HU-32`, `HU-36` | [HU-36](https://trello.com/c/QpKJys2h) | `build/reports/cucumber`, videos |
| G. OWASP ZAP | `HU-26`, `HU-36` | [HU-36](https://trello.com/c/QpKJys2h) | `security/zap/reports`, triage |
| H. JMeter | `HU-35` | [HU-35](https://trello.com/c/JtpIbK6B) | JTL, dashboard, `summary.json` |
| I. Azure Pipelines | `HU-36` | [HU-36](https://trello.com/c/QpKJys2h) | Run de Azure, artefactos, fallo controlado |

Las cards `HU-33` a `HU-38` están en la lista `Backlog` del tablero `Sistema de Reservas [CALIDAD]`.

## Checklist de cierre

- [ ] JUnit ejecutado y reporte adjunto.
- [ ] PIT ejecutado, mutation score y supervivientes documentados.
- [ ] SonarQube analizado y quality gate evaluado.
- [ ] Gherkin de login ejecutado en Azure con videos.
- [ ] ZAP ejecutado en Azure y triage actualizado.
- [ ] JMeter ejecutado con dashboard, throughput, errores y percentiles.
- [ ] Azure Pipelines verde en `main` y fallo controlado documentado.
- [ ] Plan de pruebas y guía revisados.
- [ ] Presentación y demo ensayadas.
- [ ] Evidencia enlazada en las cards y en el README.

## Referencias

- `README.md`
- `docs/backend-standards.md`
- `docs/frontend-standards.md`
- `docs/trello-backlog-contract.md`
- `security/zap/reports/hu-26-zap-baseline.md`
- `perf/README.md`
- `backlog/backlog.md`
