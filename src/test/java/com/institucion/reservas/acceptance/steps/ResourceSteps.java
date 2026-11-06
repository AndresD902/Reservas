package com.institucion.reservas.acceptance.steps;

import com.institucion.reservas.acceptance.support.AcceptanceApi;
import com.institucion.reservas.acceptance.support.AcceptanceWorld;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pasos de los escenarios de registro, consulta y cambio de estado de recursos.
 */
public class ResourceSteps {

    private static final String NOMBRE_POR_DEFECTO = "Recurso de aceptación";

    @Autowired
    private AcceptanceWorld world;

    @Autowired
    private AcceptanceApi api;

    @Cuando("registro el recurso {string} de tipo {string} con capacidad {int} en estado {string}")
    public void registroElRecurso(String nombre, String tipo, int capacidad, String estado)
            throws Exception {
        MvcResult result = api.post("/recursos", Map.of(
                "nombre", nombre,
                "descripcion", "Recurso de aceptación",
                "tipo", tipo,
                "capacidad", capacidad,
                "estado", estado));
        world.setLastResponse(result);
        if (result.getResponse().getStatus() == 201) {
            JsonNode body = api.body(result);
            world.setRecursoId(body.get("id").asLong());
        }
    }

    @Dado("un recurso disponible registrado")
    public void unRecursoDisponibleRegistrado() throws Exception {
        crearRecurso(NOMBRE_POR_DEFECTO, "SALA", 10, "DISPONIBLE");
    }

    @Dado("un recurso registrado en estado {string}")
    public void unRecursoRegistradoEnEstado(String estado) throws Exception {
        crearRecurso(NOMBRE_POR_DEFECTO, "SALA", 10, estado);
    }

    @Cuando("cambio el estado del recurso registrado a {string}")
    public void cambioElEstadoDelRecursoRegistrado(String estado) throws Exception {
        world.setLastResponse(api.put("/recursos/{id}/estado", Map.of("estado", estado),
                world.getRecursoId()));
    }

    @Cuando("intento cambiar el estado del recurso registrado a {string}")
    public void intentoCambiarElEstadoDelRecursoRegistrado(String estado) throws Exception {
        world.setLastResponse(api.put("/recursos/{id}/estado", Map.of("estado", estado),
                world.getRecursoId()));
    }

    @Cuando("consulto el recurso registrado")
    public void consultoElRecursoRegistrado() throws Exception {
        world.setLastResponse(api.get("/recursos/{id}", world.getRecursoId()));
    }

    @Cuando("listo los recursos")
    public void listoLosRecursos() throws Exception {
        world.setLastResponse(api.get("/recursos"));
    }

    @Entonces("el recurso registrado tiene nombre {string}, tipo {string} y estado {string}")
    public void elRecursoRegistradoTieneNombreTipoYEstado(String nombre, String tipo, String estado) {
        JsonNode body = api.body(world.getLastResponse());
        assertThat(body.get("nombre").asString()).isEqualTo(nombre);
        assertThat(body.get("tipo").asString()).isEqualTo(tipo);
        assertThat(body.get("estado").asString()).isEqualTo(estado);
    }

    @Entonces("el recurso devuelto tiene nombre {string} y tipo {string}")
    public void elRecursoDevueltoTieneNombreYTipo(String nombre, String tipo) {
        JsonNode body = api.body(world.getLastResponse());
        assertThat(body.get("nombre").asString()).isEqualTo(nombre);
        assertThat(body.get("tipo").asString()).isEqualTo(tipo);
    }

    @Entonces("el recurso devuelto tiene estado {string}")
    public void elRecursoDevueltoTieneEstado(String estado) {
        assertThat(api.body(world.getLastResponse()).get("estado").asString()).isEqualTo(estado);
    }

    @Entonces("el listado de recursos contiene el recurso registrado")
    public void elListadoDeRecursosContieneElRecursoRegistrado() {
        JsonNode content = api.body(world.getLastResponse()).get("content");
        assertThat(content.isArray()).isTrue();
        List<Long> ids = new ArrayList<>();
        content.forEach(node -> ids.add(node.get("id").asLong()));
        assertThat(ids).contains(world.getRecursoId());
    }

    private void crearRecurso(String nombre, String tipo, int capacidad, String estado)
            throws Exception {
        MvcResult result = api.post("/recursos", Map.of(
                "nombre", nombre,
                "descripcion", "Recurso de aceptación",
                "tipo", tipo,
                "capacidad", capacidad,
                "estado", estado));
        world.setLastResponse(result);
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        world.setRecursoId(api.body(result).get("id").asLong());
    }
}
