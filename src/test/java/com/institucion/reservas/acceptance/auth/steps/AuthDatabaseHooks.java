package com.institucion.reservas.acceptance.auth.steps;

import com.institucion.reservas.support.DatabaseCleaner;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Deja la base de pruebas limpia antes de cada escenario de autenticación para
 * que los datos de un escenario no afecten al siguiente.
 */
public class AuthDatabaseHooks {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Before
    public void cleanDatabaseBeforeScenario() {
        databaseCleaner.clean();
    }
}
