---
description: "Guía operativa para planificar, ejecutar y presentar la evidencia de calidad del sistema de reservas: plan de pruebas, JUnit, PIT, SonarQube, Gherkin, OWASP ZAP, JMeter y Azure Pipelines."
---
# Guía de evidencia de calidad

## Propósito y alcance

Esta guía reúne el plan de pruebas y el procedimiento para producir la evidencia que exige la evaluación del proyecto. Cubre los ítems A a I: presentación, plan de pruebas, JUnit, PIT, SonarQube, Gherkin, OWASP ZAP, JMeter y Azure Pipelines.

La guía describe **cómo ejecutar y demostrar** cada verificación. No reemplaza las pruebas ni los reportes: los comandos producen los artefactos y esta guía fija dónde quedan, qué umbral deben cumplir y cómo se interpretan.

Queda fuera de alcance el despliegue a producción, la prueba de penetración manual y el análisis de infraestructura.

## Cómo usar esta guía

1. Ubica el ítem A–I que se quiere demostrar en la matriz de trazabilidad.
2. Ejecuta el comando de la sección correspondiente y conserva el reporte en la ruta indicada.
3. Registra la fecha, el entorno y la URL o ruta del artefacto; una captura sin ubicación no es evidencia reproducible.
4. Actualiza el estado de la evidencia, la card de la historia y el checklist de cierre cuando corresponda.
5. Si cambia un comando, un umbral o una etapa de CI, actualiza esta guía en el mismo cambio.

La guía distingue **cómo se reproduce** una evidencia de **qué resultado se obtuvo**. Las cifras de las secciones son la última corrida registrada; no reemplazan el reporte que genera cada ejecución.

## Estado de la evidencia

Esta tabla es una foto al 2026-11-23. No sustituye los artefactos: indica qué está listo y qué falta para cerrar la entrega.

| Ítem | Estado | Evidencia vigente | Qué falta |
|---|---|---|---|
| A. Presentación | Guion y recorrido definidos en esta guía | `## Presentación y demo (A)` | Grabar el video de respaldo, ensayar tiempos y registrar la demo |
| B. Plan de pruebas | En revisión en HU-37 | Esta guía y PR #35 | Merge del PR y revisión del equipo |
| C. JUnit | Reporte local disponible sin fallos; la corrida de cierre se toma de CI | `build/reports/tests/test`, `build/test-results/test` | Adjuntar el reporte de la corrida de cierre |
| D. PIT | Gate 80/90 y corrida ampliada documentados | `build/reports/pitest`, `build/reports/pitest-full`, tabla de supervivientes | Repetir la corrida ampliada cuando cambie la lógica de negocio |
| E. SonarQube | `BackendQuality` aprobado en Azure | Run 33 y dashboard SonarCloud | Registrar URL directa del dashboard y triage de hallazgos |
| F. Gherkin | E2E Serenity/Gherkin aprobado en Azure | Artefacto `browser-e2e-evidence` del run 33 | Conservar los reportes y videos del artefacto |
| G. OWASP ZAP | Baseline DAST aprobado en Azure | Artefacto `zap-security-evidence` del run 33 | Registrar triage vigente de alertas |
| H. JMeter | 500 muestras verificadas en `main`; p95 213 ms y 53,28 muestras/s | [Run 33](https://dev.azure.com/andresposada274/Reservas/_build/results?buildId=33), `jmeter-performance-evidence` | Repetir si cambia el agente o el escenario |
| I. Azure Pipelines | Cinco etapas aprobadas en `main`; fallo controlado identificado y documentado | Runs 27, 28 y 33; ruleset requerido `AndresD902.Reservas` | Revisar artefactos/triage y mantener la evidencia al cambiar el CI |

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
| Frontend | ESLint, TypeScript, Vitest y Vite | `cd frontend && npm run lint && npm run typecheck && npm test && npm run build` | Salida de cada comando |
| Integración continua | GitHub Actions y Azure Pipelines | Push o PR en GitHub; Azure según `azure-pipelines.yml` | Artefactos y resultados de cada run |

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

Requiere JDK 21 y no necesita PostgreSQL: la suite usa H2 en memoria y aplica las migraciones de Flyway. La tarea `test` excluye las etiquetas `acceptance`, `e2e`, `postgres` y `serenity`.

```bash
./gradlew test --console=plain
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

La tarea `pitestFull` analiza servicios, controladores y excepciones, excluye DTOs, entidades, mappers, configuración y la clase de arranque, y no aplica umbrales de mutación ni cobertura. No forma parte de `check`; el pipeline de Azure la ejecuta junto con `pitest` y publica ambos reportes dentro de `build/reports`.

```bash
./gradlew pitestFull
```

El reporte HTML/XML queda en `build/reports/pitest-full/`.

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

### Corrida ampliada local

En Windows, `./gradlew.bat pitestFull --console=plain` generó 135 mutaciones: 134 eliminadas y una sobreviviente; score 99 %, cobertura de líneas 235/240 (98 %), cobertura de mutantes 100 % y 32 clases de prueba examinadas. El reporte está en `build/reports/pitest-full/`.

| Clase | Línea | Mutador | Causa | Acción |
|---|---:|---|---|---|
| `AuthController` | 60 | `VoidMethodCallMutator` elimina `SecurityContextHolder.setContext` | `SecurityContextRepository.saveContext` persiste el contexto y las pruebas de sesión verifican el acceso posterior. La llamada al holder no cambia el resultado observable del flujo cubierto y el mutante sobrevivió. | Aceptado como redundancia para el contrato HTTP actual; se conserva documentado como riesgo de disponibilidad del contexto durante el resto de la petición. |

Una primera corrida detectó además que la prueba de `/auth/csrf` verificaba el JSON, pero no la cookie CSRF. Se agregó la aserción de existencia de `XSRF-TOKEN`; la corrida repetida eliminó ese mutante. El mutante de `setContext` permanece clasificado como sobreviviente equivalente y no se cambió lógica de autenticación fuera del alcance de HU-34.

## Evidencia SonarQube (E)

### Configuración

`build.gradle` aplica `org.sonarqube` 7.6.0.18749 y configura `projectKey=AndresD902_Reservas`, organización `andresd902`, URL `https://sonarcloud.io` y nombre `Sistema de Reservas`. El análisis importa el XML de JaCoCo de `build/reports/jacoco/test/jacocoTestReport.xml`. Se excluyen DTOs, entidades, mappers, configuración y la clase de arranque del cálculo de cobertura; DTOs, entidades y mappers también se excluyen del cálculo de duplicación.

El token se lee exclusivamente desde `SONAR_TOKEN`; no debe guardarse en el repositorio ni imprimirse en logs. En Azure DevOps, el variable group `reservas-quality` es la fuente de esa variable. `sonar.qualitygate.wait=true` hace que la tarea falle cuando SonarCloud rechaza el quality gate; el timeout configurado es de 300 segundos.

El primer análisis de HU-33 falló por `new_security_rating=3`, con requisito máximo `1`. SonarCloud señaló dos referencias `gradle/actions/setup-gradle@v4` sin SHA completo y `npm ci` sin `--ignore-scripts` en `.github/workflows/ci.yml`. El workflow ya fija setup-gradle a un SHA verificado y omite scripts de instalación durante `npm ci`. El resultado que cuenta como evidencia es el del último run de `BackendQuality`: si `sonar` falla o el quality gate se rechaza, el job termina en rojo. La URL del run y el triage se registran en la card HU-33; no se usa una captura local como sustituto.

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

El runner fija `sampleresult.timestamp.start=true`. Para `POST /reservas`, el throughput se calcula como número de muestras dividido por el intervalo entre el primer inicio y el último fin (`timeStamp + elapsed`). Los percentiles del verificador usan nearest-rank; JMeter interpola, por lo que no se espera igualdad exacta. La fila `Total` del dashboard incluye CSRF y no representa únicamente las reservas.

### Interpretación

JMeter marca los 499 HTTP 409 como fallos HTTP. El informe debe separar:

- **Conflictos esperados:** 499 respuestas 409 por la regla de recurso no disponible.
- **Errores inesperados:** respuestas 500 o fuera del contrato.

El bloqueo pesimista `findByIdForUpdate` serializa los intentos sobre el mismo recurso y constituye un punto de contención del escenario. Estas mediciones no incluyen profiling que permita atribuirle por sí solo toda la latencia.

### Corrida local de HU-35

El 20 de noviembre de 2026, Docker Desktop reportó Linux x86_64 con 16 vCPU y 7,59 GiB de memoria. Seis corridas locales cumplieron 1×201, 499×409, 500 GET de CSRF con HTTP 200 y una reserva activa solapada. Con el heap del generador limitado a 2 GiB, la corrida más reciente produjo p95 de 4 ms, promedio de 3 ms y throughput de 52,13 reservas/s. Las corridas anteriores midieron p95 de 126, 6, 6, 5 y 4 ms. La variación confirma que las latencias dependen del estado del entorno.

El resumen más reciente registró 99,8 % de error HTTP bruto porque JMeter cuenta los 499 conflictos esperados como errores; el porcentaje de respuestas inesperadas fue 0 %. El dashboard desglosa los samplers y conserva esa distinción en `summary.json` para `POST /reservas`.

Los runs 14 (`20261120.7`) y 15 de Azure publicaron `jmeter-performance-evidence`. Ambos dieron 1×201, 499×409, 500 GET CSRF HTTP 200, 0 % de errores inesperados y una reserva activa solapada, pero no cumplieron p95 < 800 ms. El run 14 midió p95 1106 ms, promedio 210 ms y throughput 45,14/s con heap JMeter de 5,2 GiB; el run 15 midió p95 1580 ms, promedio 394,05 ms y throughput 43,52/s con heap limitado a 2 GiB. La corrida local con el mismo heap midió p95 4 ms. La diferencia observada confirma que los resultados dependen del agente; no determina por sí sola un cambio requerido en el backend.

### Repetición en agente Azure self-hosted de HU-35

El [run 18, `20261120.11`](https://dev.azure.com/andresposada274/Reservas/_build/results?buildId=18) ejecutó `PerformanceEvidence` correctamente en `LocalPool` / `NewAgent` el 20 de noviembre de 2026. `environment.json` registra 16 CPU y 8 151 265 280 bytes de RAM disponibles para Docker (7,59 GiB), y el commit de validación del PR `41015fb097900de7b02a75aaa6bded720f834608`, correspondiente a la rama con commit `7fe2d3b`. Se conservó el escenario de 500 hilos, ramp-up de 10 segundos y una iteración, sin cambiar el umbral ni el backend.

| Métrica de los 500 POST | Run 18 |
|---|---|
| Códigos | 1×201 y 499×409 |
| Promedio | 22,29 ms |
| Throughput del dashboard | 52,36 reservas/s |
| p50 / p90 / p95 / p99 | 4 / 10 / 190 / 334 ms |
| Error HTTP bruto / inesperado | 99,8 % / 0 % |
| Reservas activas solapadas | 1, verificada por API y PostgreSQL |

El artefacto `jmeter-performance-evidence` se descargó y verificó: JTL con 500 CSRF HTTP 200 y 500 POST, `summary.json`, dashboard HTML con sus 127 archivos y `environment.json`. El CSV auxiliar no se publica. El p95 de 190 ms del verificador y el p95 interpolado de 208,05 ms del dashboard cumplen < 800 ms para este escenario y entorno; no prueban capacidad dedicada ni rendimiento sostenido para otras cargas.

Al contrastar el dashboard se detectó que los resúmenes originales hasta el run 19 interpretaban el timestamp como fin de petición y calculaban mal la duración. El `summary.json` histórico del run 18 conserva 50,40/s; el throughput correcto es 52,36/s, coincidente entre el dashboard y el JTL recalculado con la corrección. La prueba de regresión reprodujo una duración errónea de 1500 ms frente a los 1000 ms esperados y pasó tras corregir la ventana. Este ajuste no cambia las latencias, los códigos ni la integridad. Las cifras de throughput de corridas anteriores se conservan como registro histórico del cálculo original.

El intento anterior, run 17, publicó JTL/dashboard pero falló antes del resumen y de verificar integridad: WSL heredó una ruta temporal de Docker que quedó inválida tras terminar el contenedor. El pipeline ahora entra al checkout estable bajo `/mnt/c/` antes de ejecutar el script. El run 18 pasó también la limpieza del stack aislado. El PR permanece pendiente de revisión y merge; este registro acredita la etapa de rendimiento, no afirma el estado de otras etapas todavía en ejecución.

## Azure Pipelines (I)

### Preparación

1. El proyecto `andresposada274/Reservas` y la service connection GitHub de `AndresD902` ya existen.
2. El variable group `reservas-quality` ya existe y contiene `SONAR_TOKEN` como secreto.
3. Crear o autorizar el pipeline desde `azure-pipelines.yml` siguiendo la sección de configuración manual.
4. El agente `ubuntu-latest` usa JDK 21 para backend. Rendimiento requiere el agente Windows self-hosted `NewAgent` en `LocalPool`, con Docker Desktop Linux, Compose v2, Bash/WSL y Python 3 en WSL; debe estar conectado cuando se ejecute el pipeline. El checkout se resuelve explícitamente bajo `/mnt/<unidad>/`: una ruta temporal de Docker Desktop puede desaparecer al terminar JMeter y dejar a Python sin directorio de trabajo.

### Etapas implementadas

| Etapa | Qué ejecuta | Artefactos |
|---|---|---|
| `PerformanceEvidence` | Docker Compose, 500 usuarios JMeter, verificación de integridad | Artefacto `jmeter-performance-evidence` con JTL, dashboard HTML y `summary.json`; `run-status.txt` si no se completa la ejecución |
| `BackendQuality` | JUnit, PIT, JaCoCo, aceptación, E2E HTTP y SonarCloud | Reportes de pruebas, cobertura, mutación y resultado del quality gate |
| `FrontendQuality` | Node 24, lint, typecheck, Vitest y build | Artefacto `frontend-quality-reports` y resultados JUnit |
| `BrowserE2E` | Stack Docker Compose, Serenity/Playwright y Gherkin | Artefacto `browser-e2e-evidence` con reportes y videos; resultados JUnit de Gherkin |
| `ZapSecurity` | Baseline DAST de API e interfaz con umbral medio | Artefacto `zap-security-evidence` con JSON, HTML y resumen del run |

La etapa `ControlledFailureEvidence` se agrega únicamente cuando una ejecución manual
selecciona el parámetro `runControlledFailure=true`. Activa una aserción JUnit dedicada,
publica el XML aunque el test falle y deja el pipeline rojo de forma intencional. Las
ejecuciones normales omiten esa etapa. Después de capturar la evidencia roja, ejecutar
el pipeline con el valor predeterminado para volver a obtener un resultado verde.


### Pipeline versionado

El pipeline versionado en `azure-pipelines.yml` se dispara con cambios y pull requests hacia `main`. Ejecuta en paralelo la suite de backend y la etapa `PerformanceEvidence`. Esta última levanta Docker Compose, ejecuta `perf/scripts/run-hu18.sh` y publica JTL, dashboard y resumen como `jmeter-performance-evidence`, incluso si falla una verificación para facilitar el diagnóstico. El agente `NewAgent` ejecuta la carga con proyecto Compose `hu35-<BuildId>` y puertos 18085 (API) y 15435 (PostgreSQL), conservando aislados los volúmenes del entorno local. La limpieza elimina solo el stack de esa corrida. `environment.json` registra CPU y RAM disponibles para Docker, agente, commit y run; esos recursos no equivalen a capacidad dedicada. `LocalPool` debe estar autorizado para este pipeline. El umbral se conserva en p95 < 800 ms.

La etapa de backend genera el reporte JaCoCo y ejecuta SonarCloud con el secreto `SONAR_TOKEN` del variable group `reservas-quality`. El checkout usa `fetchDepth: 0` para descargar el historial completo: Sonar necesita el blame para distinguir issues existentes de issues nuevos y evaluar correctamente el quality gate. El job falla si el gate no se aprueba.

Backend, frontend, navegador y ZAP se ejecutan en pushes y pull requests a `main`, en
agentes hospedados de Ubuntu. JMeter se ejecuta en pushes a `main` o al iniciar una
ejecución manual con `runPerformance=true`; por tanto, no bloquea PR por indisponibilidad
del agente local. Requiere `NewAgent` conectado en `LocalPool`. Todas las etapas
publican sus artefactos con condición `always()` para conservar evidencia diagnóstica
cuando una verificación falla. La etapa de frontend también publica resultados JUnit
de Vitest y la etapa de navegador publica los resultados XML de Gherkin.

### Configuración manual en Azure DevOps

1. Abrir **Pipelines → New pipeline** y seleccionar **GitHub**.
2. Elegir la conexión existente de GitHub para `AndresD902`; si se solicita, autorizarla para acceder al repositorio `AndresD902/Reservas`.
3. Seleccionar el repositorio y elegir **Existing Azure Pipelines YAML file**.
4. Seleccionar `main` (o la rama que contiene el archivo si se configura antes del merge) y la ruta `/azure-pipelines.yml`; guardar y ejecutar.
5. Si Azure solicita autorización de recursos, autorizar la conexión GitHub y el variable group `reservas-quality` para este pipeline. También puede hacerse en **Pipelines → Library → reservas-quality → Pipeline permissions**, habilitando este pipeline.
6. Confirmar que backend, frontend, navegador y ZAP terminan en el PR y que el dashboard SonarCloud muestra cobertura JaCoCo y quality gate. Revisar el log para confirmar que no se expone el token.
7. En la validación del PR no debe ejecutarse JMeter. En un push a `main` o en una ejecución manual con `runPerformance=true`, confirmar que JMeter publica el quinto artefacto.
8. Ejecutar manualmente con `runControlledFailure=true`; verificar que `ControlledFailureEvidence` quede roja y que el XML identifique `ControlledFailureEvidenceTest`. Después, ejecutar el pipeline con el valor predeterminado y confirmar que vuelve a verde.
9. En GitHub, abrir **Settings → Rules → Rulesets** y confirmar que el ruleset `Azure Pipelines required on main` exige el check `AndresD902.Reservas` para `main`.

### Evidencia de ejecución

- [PR #34](https://github.com/AndresD902/Reservas/pull/34) y [run 31](https://dev.azure.com/andresposada274/Reservas/_build/results?buildId=31): backend, frontend, navegador y ZAP aprobados; JMeter omitido conforme a la condición del PR.
- [Run manual 25](https://dev.azure.com/andresposada274/Reservas/_build/results?buildId=25): cinco etapas aprobadas y artefactos de backend, frontend, navegador, ZAP y JMeter descargados y revisados. JMeter registró 500 muestras, 1×201, 499×409 esperados, 500 CSRF HTTP 200, ningún error inesperado, p95 de 130 ms y 53,52 muestras/s.
- [Run 27](https://dev.azure.com/andresposada274/Reservas/_build/results?buildId=27): fallo intencional de `ControlledFailureEvidenceTest.reportsAnIdentifiableAssertionFailureWhenExplicitlyEnabled()`, etapa roja y resultado JUnit publicado en el test run 56. El check agregado requerido quedó rojo.
- [Run normal 28](https://dev.azure.com/andresposada274/Reservas/_build/results?buildId=28): backend, frontend, navegador y ZAP aprobados; el check agregado requerido volvió a verde.
- [Run automático 32 en `main`](https://dev.azure.com/andresposada274/Reservas/_build/results?buildId=32): backend, frontend, navegador y ZAP aprobados; JMeter falló porque Docker Desktop no estaba iniciado en `NewAgent`.
- [Run 33 en `main`](https://dev.azure.com/andresposada274/Reservas/_build/results?buildId=33): cinco etapas aprobadas después de iniciar Docker. JMeter: 500 muestras, 1×201, 499×409 esperados, 500 CSRF HTTP 200, 0 errores inesperados, p95 213 ms, throughput 53,28 muestras/s y una reserva activa solapada verificada por API y base de datos. El artefacto `jmeter-performance-evidence` fue descargado y revisado.
- El ruleset `Azure Pipelines required on main` exige el check `AndresD902.Reservas`; el run 33 confirma su verificación en `main` después del merge del PR #34.
- Pendiente de registrar aquí: URL del proyecto SonarCloud con sus métricas visibles y clasificación de bugs, vulnerabilidades, code smells, deuda y duplicación observados.


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

| Ítem | Historia(s) | Card(s) | Cómo se reproduce | Evidencia |
|---|---|---|---|---|
| A. Presentación | `HU-38` | [HU-38](https://trello.com/c/KFVgnBQg) | Seguir `## Presentación y demo (A)` | Guion, video o capturas, reportes enlazados |
| B. Plan de pruebas | `HU-37` | [HU-37](https://trello.com/c/ZaIwXnKX) | Leer esta guía y revisar el PR #35 | Este documento y el registro de revisión |
| C. JUnit | `HU-13`, `HU-14`, `HU-15`, `HU-16` | [HU-13](https://trello.com/c/gekDLvyk), [HU-14](https://trello.com/c/yzweceYl), [HU-15](https://trello.com/c/jxOestU8), [HU-16](https://trello.com/c/U7rggwKZ) | `./gradlew test --console=plain` | `build/reports/tests/test`, `build/test-results/test` |
| D. PIT | `HU-14`, `HU-34` | [HU-14](https://trello.com/c/yzweceYl), [HU-34](https://trello.com/c/jCuoecqj) | `./gradlew pitest pitestFull` | `build/reports/pitest`, `build/reports/pitest-full`, tabla de supervivientes |
| E. SonarQube | `HU-33` | [HU-33](https://trello.com/c/tDqu9wJQ) | Run `BackendQuality` en Azure | Dashboard de SonarCloud, quality gate y triage enlazados en la card |
| F. Gherkin | `HU-31`, `HU-32`, `HU-36` | [HU-31](https://trello.com/c/kkbh2Yl3), [HU-32](https://trello.com/c/jzWteyWo), [HU-36](https://trello.com/c/QpKJys2h) | `./gradlew e2eGherkinTest` con el stack levantado | `build/reports/cucumber`, `build/e2e-videos` |
| G. OWASP ZAP | `HU-26`, `HU-36` | [HU-26](https://trello.com/c/Gq6uXMzI), [HU-36](https://trello.com/c/QpKJys2h) | `bash security/zap/run-baseline.sh` o workflow `zap-baseline.yml` | `security/zap/reports`, artefacto `reporte-seguridad-zap` |
| H. JMeter | `HU-35` | [HU-35](https://trello.com/c/JtpIbK6B) | `bash perf/scripts/run-hu18.sh` con el stack levantado | JTL, dashboard, `summary.json` y artefacto `jmeter-performance-evidence` |
| I. Azure Pipelines | `HU-23`, `HU-36` | [HU-23](https://trello.com/c/seULlzuC), [HU-36](https://trello.com/c/QpKJys2h) | Push o PR a `main`; revisar las dos etapas del pipeline | Run de Azure, artefactos y fallo controlado |

El estado vigente de cada card se consulta en el tablero `Sistema de Reservas [CALIDAD]`. Las cards completadas no se usan como evidencia por sí mismas: la evidencia es el run o el artefacto enlazado.

## Checklist de cierre

- [ ] JUnit ejecutado y reporte adjunto.
- [x] PIT ejecutado, mutation score y supervivientes documentados.
- [ ] SonarQube analizado y quality gate evaluado.
- [ ] Gherkin de login ejecutado en Azure con videos.
- [ ] ZAP ejecutado en Azure y triage actualizado.
- [x] JMeter ejecutado con dashboard, throughput, errores y percentiles.
- [ ] Azure Pipelines verde en `main` y fallo controlado documentado.
- [ ] Plan de pruebas y guía revisados.
- [ ] Presentación y demo ensayadas.
- [ ] Evidencia enlazada en las cards y en el README.

Los ítems marcados tienen un reporte local o un run verificado; los pendientes requieren una ejecución o una revisión que todavía no está registrada.

## Registro de revisión

| Fecha | Alcance | Resultado |
|---|---|---|
| 2026-11-22 | Revisión técnica de comandos, umbrales, rutas, estado de CI y matriz A–I (HU-37) | Guía actualizada y contrastada con el repositorio; PR #35 abierto para revisión del equipo; `check-references.sh` sin hallazgos nuevos (12 preexistentes ajenos a HU-37). |

## Referencias

- `README.md`
- `docs/backend-standards.md`
- `docs/frontend-standards.md`
- `docs/trello-backlog-contract.md`
- `security/zap/reports/hu-26-zap-baseline.md`
- `perf/README.md`
- `backlog/backlog.md`

\r\n
