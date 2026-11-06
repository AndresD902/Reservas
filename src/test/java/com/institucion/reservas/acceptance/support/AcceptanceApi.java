package com.institucion.reservas.acceptance.support;

import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;

/**
 * Cliente HTTP de la suite de aceptación. Todas las llamadas pasan por la API
 * real con MockMvc, sin tocar repositorios salvo para preparar estados que la
 * API no puede producir.
 */
public class AcceptanceApi {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;

    public AcceptanceApi(MockMvc mockMvc, ObjectMapper objectMapper) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
    }

    public MvcResult post(String path, Object body) throws Exception {
        return perform(MockMvcRequestBuilders.post(path)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    public MvcResult get(String path, Object... uriVariables) throws Exception {
        return perform(MockMvcRequestBuilders.get(path, uriVariables));
    }

    public MvcResult put(String path, Object body, Object... uriVariables) throws Exception {
        return perform(MockMvcRequestBuilders.put(path, uriVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    public MvcResult patch(String path, Object body, Object... uriVariables) throws Exception {
        return perform(MockMvcRequestBuilders.patch(path, uriVariables)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)));
    }

    public MvcResult delete(String path, Object... uriVariables) throws Exception {
        return perform(MockMvcRequestBuilders.delete(path, uriVariables));
    }

    public JsonNode body(MvcResult result) {
        String content = new String(result.getResponse().getContentAsByteArray(), StandardCharsets.UTF_8);
        return objectMapper.readTree(content);
    }

    private MvcResult perform(MockHttpServletRequestBuilder request) throws Exception {
        return mockMvc.perform(request).andReturn();
    }
}
