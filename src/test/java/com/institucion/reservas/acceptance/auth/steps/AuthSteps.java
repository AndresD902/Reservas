package com.institucion.reservas.acceptance.auth.steps;

import com.institucion.reservas.acceptance.auth.support.AuthApi;
import com.institucion.reservas.acceptance.auth.support.AuthWorld;
import com.institucion.reservas.service.AuthService;
import io.cucumber.java.es.Cuando;
import io.cucumber.java.es.Dado;
import io.cucumber.java.es.Entonces;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Pasos de los flujos de inicio de sesión. Cada paso habla con la API real a
 * través de {@link AuthApi}, que conserva la sesión y el token CSRF.
 */
public class AuthSteps {

    @Autowired
    private AuthApi authApi;

    @Autowired
    private AuthWorld world;

    @Autowired
    private AuthService authService;

    @Dado("un administrador registrado con correo {string} y contraseña {string}")
    public void administradorRegistrado(String correo, String password) {
        authService.crearAdministrador("Admin", correo, password);
        world.setAdminCorreo(correo);
    }

    @Dado("un token CSRF disponible")
    public void tokenCsrfDisponible() throws Exception {
        authApi.fetchCsrf();
    }

    @Cuando("inicio sesión con el correo {string} y la contraseña {string}")
    public void iniciarSesion(String correo, String password) throws Exception {
        authApi.login(correo, password);
    }

    @Cuando("reintento el inicio de sesión con la contraseña {string}")
    public void reintentarInicioSesion(String password) throws Exception {
        authApi.login(world.getAdminCorreo(), password);
    }

    @Cuando("cierro la sesión")
    public void cerrarSesion() throws Exception {
        authApi.logout();
    }

    @Entonces("la respuesta tiene estado HTTP {int}")
    public void respuestaConEstado(int status) {
        assertThat(world.getLastResponse().getResponse().getStatus()).isEqualTo(status);
    }

    @Entonces("la respuesta tiene estado HTTP {int} con error {string}")
    public void respuestaConEstadoYError(int status, String error) {
        MvcResult result = world.getLastResponse();
        JsonNode body = authApi.body(result);

        assertThat(result.getResponse().getStatus()).isEqualTo(status);
        assertThat(body.get("status").asInt()).isEqualTo(status);
        assertThat(body.get("error").asText()).isEqualTo(error);
        assertThat(body.get("message").asText()).isNotBlank();
    }

    @Entonces("la sesión devuelve al administrador {string}")
    public void sesionDevuelveAlAdministrador(String correo) throws Exception {
        MvcResult result = authApi.me();
        JsonNode body = authApi.body(result);

        assertThat(result.getResponse().getStatus()).isEqualTo(200);
        assertThat(body.get("correo").asText()).isEqualTo(correo);
        assertThat(body.has("password")).isFalse();
        assertThat(body.has("passwordHash")).isFalse();
    }

    @Entonces("la consulta de sesión responde {int}")
    public void consultaSesionResponde(int status) throws Exception {
        assertThat(authApi.me().getResponse().getStatus()).isEqualTo(status);
    }

    @Entonces("la consulta de sesión responde {int} con error {string}")
    public void consultaSesionRespondeConError(int status, String error) throws Exception {
        MvcResult result = authApi.me();

        assertThat(result.getResponse().getStatus()).isEqualTo(status);
        assertThat(authApi.body(result).get("error").asText()).isEqualTo(error);
    }
}
