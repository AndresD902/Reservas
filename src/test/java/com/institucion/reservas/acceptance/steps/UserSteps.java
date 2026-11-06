package com.institucion.reservas.acceptance.steps;

import com.institucion.reservas.acceptance.support.AcceptanceApi;
import com.institucion.reservas.acceptance.support.AcceptanceWorld;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pasos de los escenarios de registro, consulta y cambio de estado de usuarios.
 */
public class UserSteps {

    private static final AtomicInteger SECUENCIA = new AtomicInteger(1);

    @Autowired
    private AcceptanceWorld world;

    @Autowired
    private AcceptanceApi api;

    @Cuando("registro el usuario {string} con documento {string} y correo {string} en estado {string}")
    public void registroElUsuario(String nombre, String documento, String correo, String estado)
            throws Exception {
        MvcResult result = api.post("/usuarios", Map.of(
                "nombre", nombre,
                "documento", documento,
                "correo", correo,
                "estado", estado));
        world.setLastResponse(result);
        guardarIdSiSeCreo(result);
    }

    @Dado("un usuario registrado con documento {string} y correo {string}")
    public void unUsuarioRegistradoConDocumentoYCorreo(String documento, String correo)
            throws Exception {
        crearUsuario("Usuario de aceptación", documento, correo, "ACTIVO");
    }

    @Dado("un usuario activo registrado")
    public void unUsuarioActivoRegistrado() throws Exception {
        String sufijo = String.format("%07d", SECUENCIA.getAndIncrement());
        crearUsuario("Usuario activo " + sufijo, "8" + sufijo,
                "usuario.activo." + sufijo + "@example.com", "ACTIVO");
    }

    @Dado("un usuario inactivo registrado")
    public void unUsuarioInactivoRegistrado() throws Exception {
        String sufijo = String.format("%07d", SECUENCIA.getAndIncrement());
        crearUsuario("Usuario inactivo " + sufijo, "8" + sufijo,
                "usuario.inactivo." + sufijo + "@example.com", "INACTIVO");
    }

    @Cuando("consulto el usuario registrado")
    public void consultoElUsuarioRegistrado() throws Exception {
        world.setLastResponse(api.get("/usuarios/{id}", world.getUsuarioId()));
    }

    @Cuando("listo los usuarios")
    public void listoLosUsuarios() throws Exception {
        world.setLastResponse(api.get("/usuarios"));
    }

    @Cuando("cambio el estado del usuario registrado a {string}")
    public void cambioElEstadoDelUsuarioRegistrado(String estado) throws Exception {
        world.setLastResponse(api.put("/usuarios/{id}/estado", Map.of("estado", estado),
                world.getUsuarioId()));
    }

    @Entonces("el usuario registrado tiene documento {string}, correo {string} y estado {string}")
    public void elUsuarioRegistradoTieneDocumentoCorreoYEstado(String documento,
                                                              String correo,
                                                              String estado) {
        JsonNode body = api.body(world.getLastResponse());
        assertThat(body.get("documento").asString()).isEqualTo(documento);
        assertThat(body.get("correo").asString()).isEqualTo(correo);
        assertThat(body.get("estado").asString()).isEqualTo(estado);
    }

    @Entonces("el usuario devuelto tiene documento {string} y correo {string}")
    public void elUsuarioDevueltoTieneDocumentoYCorreo(String documento, String correo) {
        JsonNode body = api.body(world.getLastResponse());
        assertThat(body.get("documento").asString()).isEqualTo(documento);
        assertThat(body.get("correo").asString()).isEqualTo(correo);
    }

    @Entonces("el usuario devuelto tiene estado {string}")
    public void elUsuarioDevueltoTieneEstado(String estado) {
        assertThat(api.body(world.getLastResponse()).get("estado").asString()).isEqualTo(estado);
    }

    @Entonces("el listado de usuarios contiene el usuario registrado")
    public void elListadoDeUsuariosContieneElUsuarioRegistrado() {
        JsonNode content = api.body(world.getLastResponse()).get("content");
        assertThat(content.isArray()).isTrue();
        assertThat(ids(content)).contains(world.getUsuarioId());
    }

    private void crearUsuario(String nombre, String documento, String correo, String estado)
            throws Exception {
        MvcResult result = api.post("/usuarios", Map.of(
                "nombre", nombre,
                "documento", documento,
                "correo", correo,
                "estado", estado));
        world.setLastResponse(result);
        assertThat(result.getResponse().getStatus()).isEqualTo(201);
        world.setUsuarioId(api.body(result).get("id").asLong());
    }

    private void guardarIdSiSeCreo(MvcResult result) {
        if (result.getResponse().getStatus() == 201) {
            world.setUsuarioId(api.body(result).get("id").asLong());
        }
    }

    private java.util.List<Long> ids(JsonNode array) {
        java.util.List<Long> ids = new java.util.ArrayList<>();
        array.forEach(node -> ids.add(node.get("id").asLong()));
        return ids;
    }
}
