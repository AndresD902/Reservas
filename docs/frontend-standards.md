---
description: "Stack, estructura, contrato con la API, convenciones de componentes, accesibilidad y pruebas de la interfaz web del sistema de reservas."
---
# Estándares del frontend

## Propósito y alcance

Este documento gobierna la aplicación web que vive en `frontend/`: el stack, la organización del código, la comunicación con la API, las convenciones de interfaz y las pruebas. Es un documento delegado de `docs/base-standards.md`, que rige las reglas transversales, incluida la política de idioma.

La interfaz de esta fase es una SPA sin autenticación: el usuario activo se elige en un selector y la API sigue siendo la autoridad sobre las reglas de negocio.

## Stack

| Pieza | Uso |
|---|---|
| React 19 + TypeScript | Componentes y tipado estricto |
| Vite 8 | Servidor de desarrollo, build y proxy hacia la API |
| React Router 7 | Navegación entre el listado y el detalle del recurso |
| TanStack Query 5 | Estado del servidor, caché, reintentos y mutaciones |
| Tailwind CSS 4 | Estilos utilitarios y diseño responsive |
| Vitest 5 + Testing Library | Pruebas unitarias y de componentes |
| Playwright | Pruebas E2E de navegador contra la API real |
| Oxlint | Lint del código y de los scripts |

`frontend/package.json` es la fuente de verdad de versiones y scripts. Las dependencias se instalan con `npm ci` en CI y con `npm install` en desarrollo.

## Estructura

| Ruta | Contenido |
|---|---|
| `frontend/src/api/` | Cliente HTTP, tipos del contrato y funciones por recurso (`users`, `resources`, `reservations`) |
| `frontend/src/hooks/` | Hooks de TanStack Query y hooks de contexto |
| `frontend/src/context/` | Contexto y provider de selección de usuario |
| `frontend/src/components/` | Componentes reutilizables y componentes de la pantalla de reserva |
| `frontend/src/pages/` | Páginas asociadas a rutas |
| `frontend/src/utils/` | Funciones puras de fechas y mensajes de error |
| `frontend/src/test/` | Setup de Vitest, fixtures, helpers de render y de `fetch` |
| `frontend/e2e/` | Especificaciones de Playwright |
| `frontend/scripts/` | Utilidades de apoyo, como el arranque del backend para E2E |

Los nombres de archivos y directorios van en inglés y kebab-case. Los identificadores del código van en inglés; los textos visibles para la persona usuaria, en español.

## Comunicación con la API

- `src/api/client.ts` centraliza `fetch`, el parseo del error común `{status,error,message}` y la clase `ApiError`.
- La URL base es `import.meta.env.VITE_API_BASE_URL ?? '/api'`. En desarrollo y E2E, Vite proxya `/api` hacia `http://localhost:8080` y elimina el prefijo antes de reenviar. `VITE_API_PROXY_TARGET` permite cambiar el destino del proxy sin tocar el código.
- Los componentes no llaman a `fetch` directamente: usan hooks de TanStack Query, que concentran `queryKey`, `staleTime`, reintentos y mutaciones.
- Los errores de negocio se traducen con `describeApiError` usando el código del backend (`RECURSO_NO_DISPONIBLE`, `FECHAS_INVALIDAS`, `USUARIO_INACTIVO`, etc.). No se muestran mensajes técnicos crudos.
- Los códigos 400, 404 y 409 deben producir un mensaje claro y accionable; el 409 por solapamiento nunca se trata como un error genérico.

## Empaquetado y entorno local

- `frontend/Dockerfile` es multi-stage: compila con Node 24 y `npm ci`, y sirve el `dist` con Nginx.
- `frontend/nginx.conf` sirve la SPA con *fallback* a `index.html` y reenvía `/api/` a `http://api:8080/`, eliminando el prefijo igual que el proxy de Vite. No requiere configuración en tiempo de ejecución.
- En `docker-compose.yml` el servicio `frontend` está detrás del perfil `frontend`: `docker compose --profile frontend up --build` levanta la interfaz en `http://localhost:5173` (`FRONTEND_PORT`). Sin el perfil, Compose levanta solo la base y la API.
- `frontend/.dockerignore` deja fuera `node_modules`, `dist`, reportes y archivos `.env`.

## Interfaz y accesibilidad

- La pantalla principal vive en `/reservar`; el detalle de un recurso, en `/recursos/:recursoId`.
- El selector de usuario solo lista usuarios activos y conserva la selección en `localStorage` (`reservas.selectedUserId`). Si el usuario guardado deja de estar activo, se limpia.
- Toda pantalla que espera datos muestra un estado de carga; un listado vacío muestra un estado vacío con una explicación; un fallo muestra un mensaje y una acción de reintento.
- Los formularios usan `label` asociado al campo, `aria-invalid` cuando hay error, `aria-describedby` para el mensaje y `role="alert"` para los errores. Las confirmaciones usan `role="status"`.
- Las fechas se capturan con `datetime-local` y se convierten al formato de la API (`YYYY-MM-DDTHH:mm:ss`) en `src/utils/date-time.ts`. La validación del cliente exige un intervalo futuro y con fin posterior al inicio; la API vuelve a validar.
- El diseño debe ser legible en móvil y escritorio, con foco visible y contraste suficiente. No se agregan librerías de componentes sin una necesidad concreta.

## Pruebas

| Comando | Qué ejecuta |
|---|---|
| `npm run lint` | Oxlint sobre el código y los scripts |
| `npm run typecheck` | `tsc -b` con la configuración estricta |
| `npm test` | Vitest en modo no interactivo |
| `npm run build` | Typecheck y build de producción con Vite |
| `npm run test:e2e` | Playwright; arranca la API y Vite si no están corriendo |

Las pruebas de componentes viven junto al código (`*.test.tsx` o `*.test.ts`) y usan Testing Library con `jsdom`. Las pruebas E2E viven en `frontend/e2e/`, crean sus propios usuarios y recursos únicos por API y cubren el flujo feliz, el conflicto 409, la validación de fechas y la consulta de disponibilidad. `tsconfig.node.json` incluye `frontend/e2e/` para que el typecheck y el editor resuelvan `process` y los módulos `node:*`.

`npm run test:e2e` requiere PostgreSQL y un JDK 21 para levantar la API con `gradlew bootRun`. En local, el script `frontend/scripts/start-backend.mjs` arranca el backend con el perfil `local` y reutiliza una instancia existente si ya está en `http://localhost:8080`. En CI, el job `browser-e2e` levanta PostgreSQL como servicio y ejecuta el mismo comando.

## Integración continua

El workflow `.github/workflows/ci.yml` tiene tres jobs:

| Job | Qué verifica |
|---|---|
| `pipeline` | Backend: compilación, pruebas, PIT, JaCoCo, aceptación y E2E por HTTP |
| `frontend` | Lint, typecheck, pruebas de componentes y build de la SPA |
| `browser-e2e` | Playwright contra PostgreSQL real, la API y Vite |

Los reportes de Playwright se publican como artefacto aunque la ejecución falle.

## Limitaciones conocidas

- No hay autenticación ni autorización en esta fase; el usuario se elige manualmente.
- La interfaz cubre solo la reserva. Consultar/cancelar reservas y administrar usuarios y recursos corresponden a HU-20 y HU-21.
