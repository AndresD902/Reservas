package com.institucion.reservas.acceptance.steps;

import com.institucion.reservas.support.DatabaseCleaner;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Deja la base de pruebas limpia antes de cada escenario para que los datos de
 * un escenario no afecten al siguiente.
 */
public class DatabaseHooks {

    @Autowired
    private DatabaseCleaner databaseCleaner;

    @Before
    public void cleanDatabaseBeforeScenario() {
        databaseCleaner.clean();
    }
}
