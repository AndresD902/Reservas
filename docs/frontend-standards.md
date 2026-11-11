---
description: "Stack, estructura, contrato con la API, convenciones de componentes, accesibilidad y pruebas de la interfaz web del sistema de reservas."
---
# Estándares del frontend

## Propósito y alcance

Este documento gobierna la aplicación web que vive en `frontend/`: el stack, la organización del código, la comunicación con la API, las convenciones de interfaz y las pruebas. Es un documento delegado de `docs/base-standards.md`, que rige las reglas transversales, incluida la política de idioma.

La interfaz de esta fase es una SPA con login de administrador: el usuario activo del flujo de reserva se elige en un selector, mientras que el panel `/admin` exige una sesión autenticada. La API sigue siendo la autoridad sobre las reglas de negocio.

## Stack

| Pieza | Uso |
|---|---|
| React 19 + TypeScript | Componentes y tipado estricto |
| Vite 8 | Servidor de desarrollo, build y proxy hacia la API |
| React Router 7 | Navegación entre la reserva, el detalle del recurso, mis reservas y el panel administrativo |
| TanStack Query 5 | Estado del servidor, caché, reintentos y mutaciones |
| Tailwind CSS 4 | Estilos utilitarios y diseño responsive |
| Vitest 5 + Testing Library | Pruebas unitarias y de componentes |
| Serenity + Playwright (Java) | Pruebas E2E de navegador contra la API real (viven en el backend, HU-27 a HU-30); los flujos Gherkin de login de HU-31 y HU-32 usan Cucumber + Playwright en `acceptance/e2e/` |
| Oxlint | Lint del código y de los scripts |

`frontend/package.json` es la fuente de verdad de versiones y scripts. Las dependencias se instalan con `npm ci` en CI y con `npm install` en desarrollo.

## Estructura

| Ruta | Contenido |
|---|---|
| `frontend/src/api/` | Cliente HTTP, tipos del contrato y funciones por recurso (`auth`, `users`, `resources`, `reservations`) |
| `frontend/src/hooks/` | Hooks de TanStack Query y hooks de contexto, incluido `use-auth` |
| `frontend/src/context/` | Contexto y provider de selección de usuario |
| `frontend/src/components/` | Componentes reutilizables, de la pantalla de reserva, de mis reservas y del panel administrativo |
| `frontend/src/pages/` | Páginas asociadas a rutas |
| `frontend/src/utils/` | Funciones puras de fechas y mensajes de error |
| `frontend/src/test/` | Setup de Vitest, fixtures, helpers de render y de `fetch` |

Los nombres de archivos y directorios van en inglés y kebab-case. Los identificadores del código van en inglés; los textos visibles para la persona usuaria, en español.

## Comunicación con la API

- `src/api/client.ts` centraliza `fetch`, el parseo del error común `{status,error,message}`, la clase `ApiError`, el envío de credenciales (`credentials: 'include'`) y el token CSRF en la cabecera `X-XSRF-TOKEN`. Si la cookie `XSRF-TOKEN` no existe, el cliente la obtiene de `GET /auth/csrf` antes de la primera mutación.
- La URL base es `import.meta.env.VITE_API_BASE_URL ?? '/api'`. En desarrollo, Vite proxya `/api` hacia `http://localhost:8080` y elimina el prefijo antes de reenviar; en el stack de Docker Compose (donde corren las pruebas Serenity), Nginx proxya `/api/` hacia `http://api:8080/` de la misma forma. `VITE_API_PROXY_TARGET` permite cambiar el destino del proxy sin tocar el código.
- Los componentes no llaman a `fetch` directamente: usan hooks de TanStack Query, que concentran `queryKey`, `staleTime`, reintentos y mutaciones.
- Los errores de negocio se traducen con `describeApiError` usando el código del backend (`RECURSO_NO_DISPONIBLE`, `FECHAS_INVALIDAS`, `USUARIO_INACTIVO`, `DOCUMENTO_DUPLICADO`, `CORREO_DUPLICADO`, `RECURSO_CON_RESERVAS_FUTURAS`, `CREDENCIALES_INVALIDAS`, `NO_AUTENTICADO`, `ACCESO_DENEGADO`, etc.). No se muestran mensajes técnicos crudos.
- Los códigos 400, 404 y 409 deben producir un mensaje claro y accionable; el 409 por solapamiento o por mantenimiento con reservas activas nunca se trata como un error genérico.
- El panel administrativo usa los endpoints de usuarios y recursos: listados paginados, detalle, alta, edición y cambio de estado. La API sigue validando duplicados y transiciones; el cliente solo presenta el resultado.
- La pantalla de mis reservas consume `GET /reservas/usuario/{usuarioId}` y `DELETE /reservas/{id}`; tras cancelar invalida las consultas de reservas y disponibilidad para reflejar el estado nuevo.

## Empaquetado y entorno local

- `frontend/Dockerfile` es multi-stage: compila con Node 24 y `npm ci`, y sirve el `dist` con Nginx.
- `frontend/nginx.conf` sirve la SPA con *fallback* a `index.html` y reenvía `/api/` a `http://api:8080/`, eliminando el prefijo igual que el proxy de Vite. No requiere configuración en tiempo de ejecución.
- `frontend/nginx.conf` aplica las cabeceras de seguridad `Content-Security-Policy`, `X-Content-Type-Options`, `X-Frame-Options`, `Permissions-Policy`, `Cross-Origin-Opener-Policy`, `Cross-Origin-Embedder-Policy` y `Cross-Origin-Resource-Policy`, y oculta la versión de Nginx con `server_tokens off`. El análisis DAST de HU-26 las verifica.
- En `docker-compose.yml` el servicio `frontend` está detrás del perfil `frontend`: `docker compose --profile frontend up --build` levanta la interfaz en `http://localhost:5173` (`FRONTEND_PORT`). Sin el perfil, Compose levanta solo la base y la API.
- `frontend/.dockerignore` deja fuera `node_modules`, `dist`, reportes y archivos `.env`.

## Interfaz y accesibilidad

- La pantalla principal vive en `/reservar`; el detalle de un recurso, en `/recursos/:recursoId`; mis reservas, en `/mis-reservas`.
- Mis reservas lista las reservas del usuario elegido en el selector, muestra su estado y ofrece cancelar solo las `ACTIVA`; la cancelación pide confirmación en un diálogo y anuncia el resultado.
- El panel administrativo vive en `/admin`, redirige a `/admin/usuarios` y expone `/admin/usuarios` y `/admin/recursos`. Desde HU-25 exige una sesión de administrador: `RequireAdmin` consulta `GET /auth/me` y redirige a `/login` cuando no hay sesión. El login está en `/login`; el cierre de sesión vive en el encabezado. Los usuarios del flujo de reserva siguen eligiéndose con el selector y no se autentican.
- Los listados administrativos tienen búsqueda local y filtro por estado; el detalle y los formularios conviven en la misma página. Los usuarios se pueden crear y activar o inactivar. Los recursos se pueden crear, editar y cambiar a `DISPONIBLE`, `MANTENIMIENTO` o `FUERA_DE_SERVICIO`.
- La API rechaza el cambio a `MANTENIMIENTO` cuando el recurso tiene reservas `ACTIVA` con fin posterior; la interfaz muestra el error y no oculta ni cancela esas reservas.
- El selector de usuario solo lista usuarios activos y conserva la selección en `localStorage` (`reservas.selectedUserId`). Si el usuario guardado deja de estar activo, se limpia.
- Toda pantalla que espera datos muestra un estado de carga; un listado vacío muestra un estado vacío con una explicación; un fallo muestra un mensaje y una acción de reintento.
- Los formularios usan `label` asociado al campo, `aria-invalid` cuando hay error, `aria-describedby` para el mensaje y `role="alert"` para los errores. Los diálogos de confirmación usan `role="dialog"` y `aria-modal="true"`, con título asociado, foco inicial en la acción segura y cierre con Escape. Los resultados exitosos se anuncian con `role="status"`.
- Las fechas se capturan con `datetime-local` y se convierten al formato de la API (`YYYY-MM-DDTHH:mm:ss`) en `src/utils/date-time.ts`. La validación del cliente exige un intervalo futuro y con fin posterior al inicio; la API vuelve a validar.
- El diseño debe ser legible en móvil y escritorio, con foco visible y contraste suficiente. No se agregan librerías de componentes sin una necesidad concreta.

## Pruebas

| Comando | Qué ejecuta |
|---|---|
| `npm run lint` | Oxlint sobre el código y los scripts |
| `npm run typecheck` | `tsc -b` con la configuración estricta |
| `npm test` | Vitest en modo no interactivo |
| `npm run build` | Typecheck y build de producción con Vite |

Las pruebas de componentes viven junto al código (`*.test.tsx` o `*.test.ts`) y usan Testing Library con `jsdom`. Cubren la reserva, mis reservas, la administración de usuarios, la administración de recursos y el login, incluidos los errores de duplicados, de mantenimiento y de credenciales inválidas. Las pruebas E2E de navegador viven en el backend con Serenity + Playwright (Java), en `src/test/java/com/institucion/reservas/e2e/serenity/`, y se ejecutan con `./gradlew serenityE2ETest` contra el stack de Docker Compose; cubren el flujo feliz de reserva, el conflicto 409, la validación de fechas, la consulta de disponibilidad, la consulta y cancelación de reservas, el alta y cambio de estado de usuarios, el alta y edición de recursos y el bloqueo de mantenimiento con reservas futuras (HU-27 a HU-30). Los flujos Gherkin de login de HU-31 y HU-32 viven en `src/test/resources/features/e2e/` con steps Playwright en `src/test/java/com/institucion/reservas/acceptance/e2e/`; se ejecutan con `./gradlew e2eGherkinTest`, graban video en `build/e2e-videos/` y dejan el reporte en `build/reports/cucumber/`.

## Integración continua

El workflow `.github/workflows/ci.yml` tiene tres jobs:

| Job | Qué verifica |
|---|---|
| `pipeline` | Backend: compilación, pruebas, PIT, JaCoCo, aceptación y E2E por HTTP |
| `frontend` | Lint, typecheck, pruebas de componentes y build de la SPA |
| `serenity-e2e` | Serenity + Playwright y los flujos Gherkin de `e2eGherkinTest` en modo headless contra el stack de Docker Compose (API, PostgreSQL e interfaz) |

El reporte de Serenity se publica como artefacto aunque la ejecución falle; el reporte de Cucumber y los videos de los flujos Gherkin se publican en el artefacto `gherkin-e2e-report`.

El workflow `.github/workflows/zap-baseline.yml` ejecuta el análisis DAST con OWASP ZAP sobre la API y la SPA, y publica los reportes HTML y JSON como el artefacto `reporte-seguridad-zap`. El procedimiento reproducible y las reglas viven en [`security/zap/README.md`](../security/zap/README.md).

## Limitaciones conocidas

- Los usuarios del flujo de reserva no se autentican: se eligen manualmente con el selector y ven las reservas del usuario seleccionado. El panel `/admin` sí exige la sesión de administrador implementada en HU-25.
- La cancelación de mis reservas no exige una identidad propia: opera sobre la reserva del usuario elegido en el selector, consistente con la limitación anterior.
