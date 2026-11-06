package com.institucion.reservas.acceptance.steps;

import com.institucion.reservas.acceptance.support.CucumberSupportConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Configuración de Spring compartida por los escenarios de aceptación.
 * Levanta el contexto completo con el perfil {@code test} y MockMvc, igual que
 * las pruebas de integración de los controladores.
 */
@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(CucumberSupportConfiguration.class)
public class CucumberSpringConfiguration {
}
