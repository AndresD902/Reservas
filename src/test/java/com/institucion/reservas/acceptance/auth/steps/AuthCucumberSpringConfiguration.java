package com.institucion.reservas.acceptance.auth.steps;

import com.institucion.reservas.acceptance.auth.support.AuthSupportConfiguration;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

/**
 * Configuración de Spring para los escenarios de autenticación. Habilita la
 * seguridad, que el perfil {@code test} desactiva para el resto de la suite de
 * aceptación, y expone MockMvc para recorrer el flujo HTTP completo.
 */
@CucumberContextConfiguration
@SpringBootTest(properties = "reservas.security.enabled=true")
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Import(AuthSupportConfiguration.class)
public class AuthCucumberSpringConfiguration {
}
