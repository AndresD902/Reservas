package com.institucion.reservas.acceptance;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Escenario de aceptación de la reserva completa contra la API y el perfil de pruebas.
 * La tarea {@code acceptanceTest} selecciona esta clase por el tag {@code acceptance}.
 */
@Tag("acceptance")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ReservationAcceptanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void activeUserAndAvailableResource_whenReserving_thenReservationIsConfirmed() throws Exception {
        long userId = createUser("91000001", "acceptance.user@example.com");
        long resourceId = createResource("Sala de aceptación");

        LocalDateTime start = LocalDateTime.now().plusDays(1)
                .withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = start.plusHours(1);

        String request = objectMapper.writeValueAsString(Map.of(
                "usuarioId", userId,
                "recursoId", resourceId,
                "fechaInicio", start.toString(),
                "fechaFin", end.toString()));

        MvcResult result = mockMvc.perform(post("/reservas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.usuarioId").value(userId))
                .andExpect(jsonPath("$.recursoId").value(resourceId))
                .andExpect(jsonPath("$.estado").value("ACTIVA"))
                .andReturn();

        long reservationId = objectMapper.readTree(result.getResponse().getContentAsString())
                .get("id").asLong();

        mockMvc.perform(get("/reservas/{id}", reservationId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(reservationId))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    private long createUser(String documento, String correo) throws Exception {
        String request = objectMapper.writeValueAsString(Map.of(
                "nombre", "Usuario de aceptación",
                "documento", documento,
                "correo", correo,
                "estado", "ACTIVO"));

        MvcResult result = mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn();

        return idFrom(result);
    }

    private long createResource(String nombre) throws Exception {
        String request = objectMapper.writeValueAsString(Map.of(
                "nombre", nombre,
                "descripcion", "Recurso de aceptación",
                "tipo", "SALA",
                "capacidad", 10,
                "estado", "DISPONIBLE"));

        MvcResult result = mockMvc.perform(post("/recursos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn();

        return idFrom(result);
    }

    private long idFrom(MvcResult result) throws Exception {
        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        return body.get("id").asLong();
    }
}
