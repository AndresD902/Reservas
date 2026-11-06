package com.institucion.reservas.e2e;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.institucion.reservas.repository.RecursoRepository;
import com.institucion.reservas.repository.ReservaRepository;
import com.institucion.reservas.repository.UsuarioRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Flujo E2E de la reserva por HTTP real contra un servidor embebido en un puerto aleatorio.
 * La tarea {@code e2eTest} selecciona esta clase por el tag {@code e2e}.
 */
@Tag("e2e")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
class ReservationE2ETest {

    @Value("${local.server.port}")
    private int port;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private RecursoRepository recursoRepository;

    @Autowired
    private ReservaRepository reservaRepository;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    @BeforeEach
    void cleanDatabase() {
        clean();
    }

    @AfterEach
    void cleanDatabaseAfterTest() {
        clean();
    }

    @Test
    void activeUserAndAvailableResource_whenReservingOverHttp_thenReservationIsConfirmed() throws Exception {
        long userId = post("/usuarios", Map.of(
                "nombre", "Usuario E2E",
                "documento", "92000001",
                "correo", "e2e.user@example.com",
                "estado", "ACTIVO")).get("id").asLong();

        long resourceId = post("/recursos", Map.of(
                "nombre", "Sala E2E",
                "descripcion", "Recurso para la prueba E2E",
                "tipo", "SALA",
                "capacidad", 10,
                "estado", "DISPONIBLE")).get("id").asLong();

        LocalDateTime start = LocalDateTime.now().plusDays(1)
                .withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);

        JsonNode reservation = post("/reservas", Map.of(
                "usuarioId", userId,
                "recursoId", resourceId,
                "fechaInicio", start.toString(),
                "fechaFin", end.toString()));

        assertThat(reservation.get("usuarioId").asLong()).isEqualTo(userId);
        assertThat(reservation.get("recursoId").asLong()).isEqualTo(resourceId);
        assertThat(reservation.get("estado").asString()).isEqualTo("ACTIVA");

        JsonNode fetched = get("/reservas/" + reservation.get("id").asLong());
        assertThat(fetched.get("estado").asString()).isEqualTo("ACTIVA");
        assertThat(reservaRepository.count()).isEqualTo(1);
    }

    private JsonNode post(String path, Object body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri(path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(body)))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(201);
        return objectMapper.readTree(response.body());
    }

    private JsonNode get(String path) throws Exception {
        HttpRequest request = HttpRequest.newBuilder(uri(path)).GET().build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        assertThat(response.statusCode()).isEqualTo(200);
        return objectMapper.readTree(response.body());
    }

    private URI uri(String path) {
        return URI.create("http://localhost:" + port + path);
    }

    private void clean() {
        reservaRepository.deleteAll();
        recursoRepository.deleteAll();
        usuarioRepository.deleteAll();
    }
}
