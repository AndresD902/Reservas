package com.institucion.reservas.acceptance.auth.support;

import jakarta.servlet.http.Cookie;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

/**
 * Cliente HTTP de los escenarios de autenticación. Conserva la sesión y las
 * cookies CSRF entre peticiones para recorrer el flujo real de login, consulta
 * y cierre de sesión.
 */
public class AuthApi {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final AuthWorld world;
    private final Map<String, Cookie> cookieJar = new LinkedHashMap<>();

    public AuthApi(MockMvc mockMvc, ObjectMapper objectMapper, AuthWorld world) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.world = world;
    }

    public MvcResult fetchCsrf() throws Exception {
        cookieJar.clear();

        MvcResult result = mockMvc.perform(get("/auth/csrf")).andReturn();
        world.setLastResponse(result);
        actualizarSesion(result);
        actualizarCookies(result.getResponse().getCookies());

        JsonNode body = body(result);
        world.setCsrfHeader(body.get("headerName").asText());
        world.setCsrfToken(body.get("token").asText());
        return result;
    }

    public MvcResult login(String correo, String password) throws Exception {
        MockHttpServletRequestBuilder request = post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of(
                        "correo", correo,
                        "password", password)));

        return perform(request);
    }

    public MvcResult me() throws Exception {
        return perform(get("/auth/me"));
    }

    public MvcResult logout() throws Exception {
        return perform(post("/auth/logout"));
    }

    public JsonNode body(MvcResult result) {
        String content = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        return objectMapper.readTree(content);
    }

    private MvcResult perform(MockHttpServletRequestBuilder request) throws Exception {
        if (world.getSession() != null) {
            request.session(world.getSession());
        }
        if (!cookieJar.isEmpty()) {
            request.cookie(cookieJar.values().toArray(new Cookie[0]));
        }
        if (world.getCsrfHeader() != null) {
            request.header(world.getCsrfHeader(), world.getCsrfToken());
        }

        MvcResult result = mockMvc.perform(request).andReturn();
        world.setLastResponse(result);
        actualizarSesion(result);
        actualizarCookies(result.getResponse().getCookies());
        return result;
    }

    private void actualizarCookies(Cookie[] cookies) {
        for (Cookie cookie : cookies) {
            cookieJar.put(cookie.getName(), cookie);
        }
    }

    private void actualizarSesion(MvcResult result) {
        try {
            MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
            world.setSession(session != null && !session.isInvalid() ? session : null);
        } catch (IllegalStateException ex) {
            world.setSession(null);
        }
    }
}
