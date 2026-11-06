package com.institucion.reservas.acceptance.steps;

import com.institucion.reservas.acceptance.support.AcceptanceApi;
import com.institucion.reservas.acceptance.support.AcceptanceWorld;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Entonces;
import org.springframework.beans.factory.annotation.Autowired;
import tools.jackson.databind.JsonNode;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pasos de las consultas de reservas por usuario, recurso y rango, y de la
 * verificación de disponibilidad de un recurso.
 */
public class QuerySteps {

    @Autowired
    private AcceptanceWorld world;

    @Autowired
    private AcceptanceApi api;

    @Cuando("consulto las reservas del usuario registrado")
    public void consultoLasReservasDelUsuarioRegistrado() throws Exception {
        world.setLastResponse(api.get("/reservas/usuario/{usuarioId}", world.getUsuarioId()));
    }

    @Cuando("consulto las reservas del recurso registrado")
    public void consultoLasReservasDelRecursoRegistrado() throws Exception {
        world.setLastResponse(api.get("/reservas/recurso/{recursoId}", world.getRecursoId()));
    }

    @Cuando("consulto las reservas dentro de {int} días de {string} a {string}")
    public void consultoLasReservasDentroDeRango(int dias, String horaInicio, String horaFin)
            throws Exception {
        LocalDateTime inicio = ReservationSteps.enDias(dias, horaInicio);
        LocalDateTime fin = ReservationSteps.enDias(dias, horaFin);
        world.setLastResponse(api.get("/reservas/rango?fechaInicio=" + inicio + "&fechaFin=" + fin));
    }

    @Cuando("consulto las reservas en un rango inválido")
    public void consultoLasReservasEnUnRangoInvalido() throws Exception {
        LocalDateTime inicio = ReservationSteps.enDias(2, "10:00");
        LocalDateTime fin = ReservationSteps.enDias(2, "09:00");
        world.setLastResponse(api.get("/reservas/rango?fechaInicio=" + inicio + "&fechaFin=" + fin));
    }

    @Cuando("consulto la disponibilidad del recurso dentro de {int} días de {string} a {string}")
    public void consultoLaDisponibilidadDelRecurso(int dias, String horaInicio, String horaFin)
            throws Exception {
        LocalDateTime inicio = ReservationSteps.enDias(dias, horaInicio);
        LocalDateTime fin = ReservationSteps.enDias(dias, horaFin);
        world.setLastResponse(api.get("/recursos/" + world.getRecursoId()
                + "/disponibilidad?fechaInicio=" + inicio + "&fechaFin=" + fin));
    }

    @Entonces("el recurso está disponible")
    public void elRecursoEstaDisponible() {
        assertThat(api.body(world.getLastResponse()).get("disponible").asBoolean()).isTrue();
    }

    @Entonces("el recurso no está disponible")
    public void elRecursoNoEstaDisponible() {
        assertThat(api.body(world.getLastResponse()).get("disponible").asBoolean()).isFalse();
    }

    @Entonces("la disponibilidad no reporta reservas solapadas")
    public void laDisponibilidadNoReportaReservasSolapadas() {
        JsonNode solapadas = api.body(world.getLastResponse()).get("reservasSolapadas");
        assertThat(solapadas.isArray()).isTrue();
        assertThat(solapadas.size()).isZero();
    }

    @Entonces("la disponibilidad reporta reservas solapadas")
    public void laDisponibilidadReportaReservasSolapadas() {
        JsonNode solapadas = api.body(world.getLastResponse()).get("reservasSolapadas");
        assertThat(solapadas.isArray()).isTrue();
        assertThat(solapadas.size()).isPositive();
    }
}
