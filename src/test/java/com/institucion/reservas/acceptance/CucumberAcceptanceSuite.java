package com.institucion.reservas.acceptance;

import io.cucumber.junit.platform.engine.Constants;
import org.junit.jupiter.api.Tag;
import org.junit.platform.suite.api.ConfigurationParameter;
import org.junit.platform.suite.api.IncludeEngines;
import org.junit.platform.suite.api.SelectClasspathResource;
import org.junit.platform.suite.api.Suite;

/**
 * Punto de entrada de la suite de aceptación de negocio. Selecciona las
 * features Gherkin de {@code src/test/resources/features}, excluye las de
 * autenticación (etiqueta {@code @auth}) y las ejecuta con el motor de Cucumber
 * sobre JUnit Platform. La tarea {@code acceptanceTest} la elige por el tag
 * {@code acceptance}.
 */
@Tag("acceptance")
@Suite
@IncludeEngines("cucumber")
@SelectClasspathResource("features")
@ConfigurationParameter(
        key = Constants.GLUE_PROPERTY_NAME,
        value = "com.institucion.reservas.acceptance.steps")
public class CucumberAcceptanceSuite {
}
