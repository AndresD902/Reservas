package com.institucion.reservas.acceptance.steps;

import com.institucion.reservas.acceptance.support.AcceptanceApi;
import com.institucion.reservas.acceptance.support.AcceptanceWorld;
import io.cucumber.java.es.Entonces;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Comprobaciones comunes sobre la última respuesta HTTP obtenida por los pasos.
 */
public class HttpSteps {

    @Autowired
    private AcceptanceWorld world;

    @Autowired
    private AcceptanceApi api;

    @Entonces("la respuesta tiene estado HTTP {int}")
    public void laRespuestaTieneEstadoHttp(int status) {
        assertThat(world.getLastResponse()).isNotNull();
        assertThat(world.getLastResponse().getResponse().getStatus()).isEqualTo(status);
    }

    @Entonces("la respuesta tiene estado HTTP {int} con error {string}")
    public void laRespuestaTieneEstadoHttpConError(int status, String error) {
        assertThat(world.getLastResponse()).isNotNull();
        assertThat(world.getLastResponse().getResponse().getStatus()).isEqualTo(status);
        assertThat(api.body(world.getLastResponse()).get("error").asString()).isEqualTo(error);
    }
}
