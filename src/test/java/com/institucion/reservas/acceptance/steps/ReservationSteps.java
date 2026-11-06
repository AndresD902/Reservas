package com.institucion.reservas.acceptance.steps;

import com.institucion.reservas.acceptance.support.AcceptanceApi;
import com.institucion.reservas.acceptance.support.AcceptanceWorld;
import com.institucion.reservas.entity.EstadoReserva;
import com.institucion.reservas.entity.ReservaEntity;
import com.institucion.reservas.repository.ReservaRepository;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pasos de los escenarios de creación y cancelación de reservas.
 */
public class ReservationSteps {

    @Autowired
    private AcceptanceWorld world;

    @Autowired
    private AcceptanceApi api;

    @Autowired
    private ReservaRepository reservaRepository;

    @Cuando("reservo el recurso dentro de {int} días de {string} a {string}")
    public void reservoElRecurso(int dias, String horaInicio, String horaFin) throws Exception {
        reservar(dias, horaInicio, horaFin);
    }

    @Cuando("reservo el recurso con fin anterior al inicio")
    public void reservoElRecursoConFinAnteriorAlInicio() throws Exception {
        LocalDateTime inicio = enDias(2, "10:00");
        LocalDateTime fin = enDias(2, "09:00");
        world.setLastResponse(api.post("/reservas", Map.of(
                "usuarioId", world.getUsuarioId(),
                "recursoId", world.getRecursoId(),
                "fechaInicio", inicio.toString(),
                "fechaFin", fin.toString())));
    }

    @Cuando("reservo el recurso en una fecha pasada")
    public void reservoElRecursoEnUnaFechaPasada() throws Exception {
        LocalDateTime inicio = LocalDateTime.now().minusDays(2)
                .withHour(9).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime fin = inicio.plusHours(1);
        world.setLastResponse(api.post("/reservas", Map.of(
                "usuarioId", world.getUsuarioId(),
                "recursoId", world.getRecursoId(),
                "fechaInicio", inicio.toString(),
                "fechaFin", fin.toString())));
    }

    @Dado("una reserva activa del recurso dentro de {int} días de {string} a {string}")
    public void unaReservaActivaDelRecurso(int dias, String horaInicio, String horaFin)
            throws Exception {
        reservar(dias, horaInicio, horaFin);
        assertThat(world.getLastResponse().getResponse().getStatus()).isEqualTo(201);
    }

    @Dado("el usuario ya tiene {int} reservas activas")
    public void elUsuarioYaTieneReservasActivas(int cantidad) throws Exception {
        for (int i = 0; i < cantidad; i++) {
            reservar(i + 2, "09:00", "10:00");
            assertThat(world.getLastResponse().getResponse().getStatus()).isEqualTo(201);
        }
    }

    @Dado("una reserva registrada en estado {string}")
    public void unaReservaRegistradaEnEstado(String estado) throws Exception {
        reservar(2, "09:00", "10:00");
        assertThat(world.getLastResponse().getResponse().getStatus()).isEqualTo(201);

        if (!"ACTIVA".equals(estado)) {
            ReservaEntity reserva = reservaRepository.findById(world.getReservaId()).orElseThrow();
            reserva.setEstado(EstadoReserva.valueOf(estado));
            reservaRepository.save(reserva);
        }
    }

    @Cuando("cancelo la reserva registrada")
    public void canceloLaReservaRegistrada() throws Exception {
        world.setLastResponse(api.delete("/reservas/{id}", world.getReservaId()));
    }

    @Entonces("la reserva devuelta está en estado {string}")
    public void laReservaDevueltaEstaEnEstado(String estado) {
        assertThat(api.body(world.getLastResponse()).get("estado").asString()).isEqualTo(estado);
    }

    @Entonces("el listado de reservas contiene la reserva registrada")
    public void elListadoDeReservasContieneLaReservaRegistrada() {
        JsonNode body = api.body(world.getLastResponse());
        JsonNode reservas = body.isArray() ? body : body.get("content");
        assertThat(reservas).isNotNull();
        assertThat(reservas.isArray()).isTrue();

        List<Long> ids = new ArrayList<>();
        reservas.forEach(node -> ids.add(node.get("id").asLong()));
        assertThat(ids).contains(world.getReservaId());
    }

    private void reservar(int dias, String horaInicio, String horaFin) throws Exception {
        LocalDateTime inicio = enDias(dias, horaInicio);
        LocalDateTime fin = enDias(dias, horaFin);
        MvcResult result = api.post("/reservas", Map.of(
                "usuarioId", world.getUsuarioId(),
                "recursoId", world.getRecursoId(),
                "fechaInicio", inicio.toString(),
                "fechaFin", fin.toString()));
        world.setLastResponse(result);
        if (result.getResponse().getStatus() == 201) {
            world.setReservaId(api.body(result).get("id").asLong());
            world.setReservaInicio(inicio);
            world.setReservaFin(fin);
        }
    }

    static LocalDateTime enDias(int dias, String hora) {
        return LocalDateTime.now().plusDays(dias).with(LocalTime.parse(hora))
                .withSecond(0).withNano(0);
    }
}
