package com.institucion.reservas.controller;

import com.institucion.reservas.repository.AdministradorRepository;
import com.institucion.reservas.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "reservas.security.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerTest {

    private static final String LOGIN = """
            {"correo":"admin@example.com","password":"secreta"}
            """;

    private static final String USUARIO = """
            {"nombre":"Nuevo","documento":"90000001","correo":"nuevo@example.com","estado":"ACTIVO"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AdministradorRepository administradorRepository;

    @Autowired
    private AuthService authService;

    @BeforeEach
    void crearAdministrador() {
        administradorRepository.deleteAll();
        authService.crearAdministrador("Admin", "admin@example.com", "secreta");
    }

    @Test
    void loginConCredencialesValidasDevuelveElAdministrador() throws Exception {
        mockMvc.perform(post("/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.nombre").value("Admin"))
                .andExpect(jsonPath("$.correo").value("admin@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
    }

    @Test
    void loginConCredencialesInvalidasDevuelve401ConElCuerpoComun() throws Exception {
        mockMvc.perform(post("/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"correo\":\"admin@example.com\",\"password\":\"incorrecta\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("CREDENCIALES_INVALIDAS"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void csrfExponeElTokenYLaCabecera() throws Exception {
        mockMvc.perform(get("/auth/csrf"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.headerName").value("X-XSRF-TOKEN"))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    void lasLecturasSiguenSiendoPublicas() throws Exception {
        mockMvc.perform(get("/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    void laEscrituraSinSesionDevuelve401ConElCuerpoComun() throws Exception {
        mockMvc.perform(post("/usuarios").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USUARIO))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.error").value("NO_AUTENTICADO"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void laEscrituraSinTokenCsrfDevuelve403() throws Exception {
        MockHttpSession session = iniciarSesion();

        mockMvc.perform(post("/usuarios").session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USUARIO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.error").value("ACCESO_DENEGADO"));
    }

    @Test
    void laEscrituraConSesionYTokenCsrfSeEjecuta() throws Exception {
        MockHttpSession session = iniciarSesion();

        mockMvc.perform(post("/usuarios").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USUARIO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.documento").value("90000001"));
    }

    @Test
    void meDevuelveElAdministradorDeLaSesion() throws Exception {
        MockHttpSession session = iniciarSesion();

        mockMvc.perform(get("/auth/me").session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.correo").value("admin@example.com"));
    }

    @Test
    void meSinSesionDevuelve401() throws Exception {
        mockMvc.perform(get("/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("CREDENCIALES_INVALIDAS"));
    }

    @Test
    void elLogoutInvalidaLaSesion() throws Exception {
        MockHttpSession session = iniciarSesion();

        mockMvc.perform(post("/auth/logout").session(session).with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/usuarios").session(session).with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(USUARIO))
                .andExpect(status().isUnauthorized());
    }

    private MockHttpSession iniciarSesion() throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login").with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(LOGIN))
                .andExpect(status().isOk())
                .andReturn();

        return (MockHttpSession) result.getRequest().getSession(false);
    }
}
