package com.institucion.reservas.e2e.serenity.tasks;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Map;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

/**
 * Cliente mínimo de la API con sesión de administrador (login + CSRF).
 * Reutiliza el mismo flujo que `frontend/e2e/support/auth.ts`: el token
 * CSRF válido es el del JSON de `/auth/csrf` y rota tras el login, así que
 * se refresca en cada escritura junto con la cookie de sesión.
 */
final class AdminApiClient {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String apiUrl;
    private final String sessionCookie;
    private final String csrfHeaderName;

    AdminApiClient() {
        this.apiUrl = System.getProperty("serenity.api.url", "http://localhost:8080");
        try {
            CsrfToken csrf = fetchCsrf(null);
            HttpResponse<String> login = send(
                    HttpRequest.newBuilder(URI.create(apiUrl + "/auth/login"))
                            .header("Content-Type", "application/json")
                            .header("Cookie", csrf.cookie())
                            .header("X-XSRF-TOKEN", csrf.token())
                            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(Map.of(
                                    "correo", System.getenv().getOrDefault("E2E_ADMIN_EMAIL", "admin@example.com"),
                                    "password", System.getenv().getOrDefault("E2E_ADMIN_PASSWORD", "admin123")))))
                            .build());
            assertStatus(login, 200, "/auth/login");
            this.sessionCookie = login.headers().allValues("Set-Cookie").stream()
                    .map(c -> c.split(";")[0])
                    .filter(c -> c.startsWith("JSESSIONID="))
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("El login no devolvió cookie de sesión"));
            this.csrfHeaderName = fetchCsrf(sessionCookie).headerName();
        } catch (Exception e) {
            throw new AssertionError("No se pudo iniciar sesión como administrador", e);
        }
    }

    JsonNode post(String path, Map<String, Object> body) {
        try {
            CsrfToken csrf = fetchCsrf(sessionCookie);
            HttpResponse<String> response = send(
                    HttpRequest.newBuilder(URI.create(apiUrl + path))
                            .header("Content-Type", "application/json")
                            .header("Cookie", sessionCookie + "; " + csrf.cookie())
                            .header(csrfHeaderName, csrf.token())
                            .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                            .build());
            assertStatus(response, 201, path);
            return objectMapper.readTree(response.body());
        } catch (Exception e) {
            throw new AssertionError("No se pudo crear el recurso en " + path, e);
        }
    }

    JsonNode get(String path) {
        try {
            HttpResponse<String> response = send(
                    HttpRequest.newBuilder(URI.create(apiUrl + path))
                            .header("Cookie", sessionCookie)
                            .GET()
                            .build());
            assertStatus(response, 200, path);
            return objectMapper.readTree(response.body());
        } catch (Exception e) {
            throw new AssertionError("No se pudo consultar " + path, e);
        }
    }

    long createUser(String name, String document, String email) {
        return post("/usuarios", Map.of(
                "nombre", name,
                "documento", document,
                "correo", email,
                "estado", "ACTIVO")).get("id").asLong();
    }

    long createResource(String name) {
        return post("/recursos", Map.of(
                "nombre", name,
                "descripcion", "Recurso creado por la prueba Serenity",
                "tipo", "SALA",
                "capacidad", 6,
                "estado", "DISPONIBLE")).get("id").asLong();
    }

    long createReservation(long userId, long resourceId, String start, String end) {
        return post("/reservas", Map.of(
                "usuarioId", userId,
                "recursoId", resourceId,
                "fechaInicio", start,
                "fechaFin", end)).get("id").asLong();
    }

    private record CsrfToken(String headerName, String token, String cookie) {
    }

    private CsrfToken fetchCsrf(String cookie) throws Exception {
        HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(apiUrl + "/auth/csrf")).GET();
        if (cookie != null) {
            builder.header("Cookie", cookie);
        }
        HttpResponse<String> response = send(builder.build());
        assertStatus(response, 200, "/auth/csrf");
        JsonNode json = objectMapper.readTree(response.body());
        String token = json.get("token").asString();
        return new CsrfToken(json.get("headerName").asString(), token, "XSRF-TOKEN=" + token);
    }

    private HttpResponse<String> send(HttpRequest request) throws Exception {
        return httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    }

    private void assertStatus(HttpResponse<String> response, int expected, String path) {
        if (response.statusCode() != expected) {
            throw new AssertionError(path + " respondió " + response.statusCode()
                    + ", se esperaba " + expected + ": " + response.body());
        }
    }
}
