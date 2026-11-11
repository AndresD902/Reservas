package com.institucion.reservas.acceptance.auth.support;

import io.cucumber.spring.ScenarioScope;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

/**
 * Registra los colaboradores de la suite de autenticación dentro del contexto
 * de Cucumber. No se escanea desde el resto de contextos de prueba.
 */
@TestConfiguration(proxyBeanMethods = false)
public class AuthSupportConfiguration {

    @Bean
    @ScenarioScope
    public AuthWorld authWorld() {
        return new AuthWorld();
    }

    @Bean
    @ScenarioScope
    public AuthApi authApi(MockMvc mockMvc, ObjectMapper objectMapper, AuthWorld authWorld) {
        return new AuthApi(mockMvc, objectMapper, authWorld);
    }
}
