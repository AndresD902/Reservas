package com.institucion.reservas.acceptance.support;

import io.cucumber.spring.ScenarioScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/**
 * Registra los colaboradores de la suite de aceptación únicamente en el
 * contexto de Cucumber. Al ser una configuración de prueba no se escanea desde
 * el resto de contextos de prueba.
 */
@TestConfiguration(proxyBeanMethods = false)
public class CucumberSupportConfiguration {

    @Bean
    @ScenarioScope
    public AcceptanceWorld acceptanceWorld() {
        return new AcceptanceWorld();
    }

    @Bean
    public AcceptanceApi acceptanceApi(MockMvc mockMvc, ObjectMapper objectMapper) {
        return new AcceptanceApi(mockMvc, objectMapper);
    }
}
