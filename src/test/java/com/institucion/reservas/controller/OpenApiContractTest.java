package com.institucion.reservas.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Criterios de aceptación de HU-11: el contrato OpenAPI expone todos los
 * endpoints y documenta la estructura común de error.
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiContractTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void exponeDocumentacionDeUsuarios() throws Exception {
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths./usuarios.post").exists())
                .andExpect(jsonPath("$.paths./usuarios.get").exists())
                .andExpect(jsonPath("$['paths']['/usuarios/{id}']['get']").exists())
                .andExpect(jsonPath("$['paths']['/usuarios/{id}/estado']['put']").exists())
                .andExpect(jsonPath("$['paths']['/usuarios/{id}/estado']['patch']").exists());
    }

    @Test
    void exponeDocumentacionDeRecursos() throws Exception {
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths./recursos.post").exists())
                .andExpect(jsonPath("$.paths./recursos.get").exists())
                .andExpect(jsonPath("$['paths']['/recursos/{id}']['get']").exists())
                .andExpect(jsonPath("$['paths']['/recursos/{id}']['put']").exists())
                .andExpect(jsonPath("$['paths']['/recursos/{id}/estado']['put']").exists())
                .andExpect(jsonPath("$['paths']['/recursos/{id}/estado']['patch']").exists())
                .andExpect(jsonPath("$['paths']['/recursos/{id}/disponibilidad']['get']").exists());
    }

    @Test
    void exponeDocumentacionDeReservas() throws Exception {
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths./reservas.post").exists())
                .andExpect(jsonPath("$.paths./reservas.get").exists())
                .andExpect(jsonPath("$.paths./reservas/rango.get").exists())
                .andExpect(jsonPath("$['paths']['/reservas/{id}']['get']").exists())
                .andExpect(jsonPath("$['paths']['/reservas/{id}']['delete']").exists())
                .andExpect(jsonPath("$['paths']['/reservas/usuario/{usuarioId}']['get']").exists())
                .andExpect(jsonPath("$['paths']['/reservas/recurso/{recursoId}']['get']").exists());
    }

    @Test
    void exponeDocumentacionDeAutenticacion() throws Exception {
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths./auth/login.post").exists())
                .andExpect(jsonPath("$.paths./auth/logout.post").exists())
                .andExpect(jsonPath("$.paths./auth/me.get").exists())
                .andExpect(jsonPath("$.paths./auth/csrf.get").exists());
    }

    @Test
    void documentaEstructuraComunDeError() throws Exception {
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.components.schemas.ErrorResponseDto.properties.status").exists())
                .andExpect(jsonPath("$.components.schemas.ErrorResponseDto.properties.error").exists())
                .andExpect(jsonPath("$.components.schemas.ErrorResponseDto.properties.message").exists());
    }

    @Test
    void documentaPaginacionYFiltrosDeListados() throws Exception {
        mockMvc.perform(get("/v3/api-docs").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths./usuarios.get.parameters[?(@.name=='estado')]").exists())
                .andExpect(jsonPath("$.paths./usuarios.get.parameters[?(@.name=='pageable')]").exists())
                .andExpect(jsonPath("$.components.schemas.Pageable.properties.page").exists())
                .andExpect(jsonPath("$.components.schemas.Pageable.properties.size").exists())
                .andExpect(jsonPath("$.components.schemas.Pageable.properties.sort").exists())
                .andExpect(jsonPath("$.paths./recursos.get.parameters[?(@.name=='tipo')]").exists())
                .andExpect(jsonPath("$.paths./reservas.get.parameters[?(@.name=='usuarioId')]").exists())
                .andExpect(jsonPath("$.paths./reservas.get.parameters[?(@.name=='recursoId')]").exists());
    }

    @Test
    void publicaInterfazSwaggerUi() throws Exception {
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}
