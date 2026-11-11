package com.institucion.reservas.acceptance;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.jupiter.api.Tag;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Suite de aceptación de autenticación. Selecciona los flujos Gherkin de
 * {@code features/auth} y usa un contexto Spring con la seguridad habilitada, a
 * diferencia de la suite de negocio que corre con la seguridad desactivada.
 * La tarea {@code authAcceptanceTest} la ejecuta en su propio JVM para que los
 * contextos de Spring y la configuración de Cucumber no se mezclen.
 */
@Tag("auth-acceptance")
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features/auth/login-exitoso.feature")
@SelectClasspathResource("features/auth/login-fallido-reintento.feature")
@ConfigurationParameter(
        key = Constants.GLUE_PROPERTY_NAME,
        value = "com.institucion.reservas.acceptance.auth.steps")
public class AuthCucumberAcceptanceSuite {
}
