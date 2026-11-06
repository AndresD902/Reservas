package com.institucion.reservas.controller;

import com.institucion.reservas.entity.EstadoUsuario;
import com.institucion.reservas.entity.UsuarioEntity;
import com.institucion.reservas.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Criterios de aceptación de HU-03 contra la API y una base PostgreSQL real.
 * Cada prueba revierte sus datos al terminar.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class UsuarioControllerTest {

    private static final String ESTADO_INACTIVO = """
            {"estado":"INACTIVO"}
            """;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void returns201AndPersistsUserWhenDataIsValid() throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioJson("10000001", "nuevo.usuario@example.com", EstadoUsuario.ACTIVO)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.documento").value("10000001"))
                .andExpect(jsonPath("$.correo").value("nuevo.usuario@example.com"))
                .andExpect(jsonPath("$.estado").value("ACTIVO"));

        UsuarioEntity creado = usuarioRepository.findByDocumento("10000001").orElseThrow();
        assertThat(creado.getCorreo()).isEqualTo("nuevo.usuario@example.com");
        assertThat(creado.getEstado()).isEqualTo(EstadoUsuario.ACTIVO);
    }

    @Test
    void returns400WithDocumentoDuplicadoWhenDocumentAlreadyExists() throws Exception {
        crearUsuario("20000002", "primero.documento@example.com");

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioJson("20000002", "segundo.documento@example.com", EstadoUsuario.ACTIVO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("DOCUMENTO_DUPLICADO"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returns400WithCorreoDuplicadoWhenEmailAlreadyExists() throws Exception {
        crearUsuario("30000003", "correo.existente@example.com");

        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioJson("30000004", "correo.existente@example.com", EstadoUsuario.ACTIVO)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("CORREO_DUPLICADO"))
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void returnsInactivoWhenStatusIsChangedAndUserIsQueried() throws Exception {
        long id = crearUsuario("40000004", "cambio.estado@example.com");

        mockMvc.perform(put("/usuarios/{id}/estado", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ESTADO_INACTIVO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));

        mockMvc.perform(get("/usuarios/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));
    }

    @Test
    void returnsInactivoWhenStatusIsChangedWithPatch() throws Exception {
        long id = crearUsuario("40000005", "cambio.estado.patch@example.com");

        mockMvc.perform(patch("/usuarios/{id}/estado", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(ESTADO_INACTIVO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));

        mockMvc.perform(get("/usuarios/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("INACTIVO"));
    }

    @Test
    void returns404WithStandardErrorBodyWhenUserDoesNotExist() throws Exception {
        mockMvc.perform(get("/usuarios/{id}", 999_999_999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").isNotEmpty())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    private long crearUsuario(String documento, String correo) throws Exception {
        return crearUsuario(documento, correo, EstadoUsuario.ACTIVO);
    }

    private long crearUsuario(String documento, String correo, EstadoUsuario estado) throws Exception {
        mockMvc.perform(post("/usuarios")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(usuarioJson(documento, correo, estado)))
                .andExpect(status().isCreated());

        return usuarioRepository.findByDocumento(documento).orElseThrow().getId();
    }

    private String usuarioJson(String documento, String correo, EstadoUsuario estado) {
        return """
                {"nombre":"Usuario de prueba","documento":"%s","correo":"%s","estado":"%s"}
                """.formatted(documento, correo, estado);
    }
}
