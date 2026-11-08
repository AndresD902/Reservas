package com.institucion.reservas.e2e.serenity.tasks;

import java.util.UUID;

import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

/**
 * Crea un usuario por API recordando su documento, para probar el error
 * de documento duplicado en el formulario de administración.
 */
public class PrepareExistingUser implements Task {

    @Override
    @Step("{0} prepara un usuario existente por API")
    public <T extends Actor> void performAs(T actor) {
        AdminApiClient api = new AdminApiClient();
        String suffix = UUID.randomUUID().toString().substring(0, 8);
        String document = "SERDUP" + suffix;
        api.createUser("000 Usuario Serenity " + suffix, document,
                "serenity.duplicado." + suffix + "@example.com");
        actor.remember("documento", document);
        actor.remember("sufijo", suffix);
    }

    public static PrepareExistingUser forDuplicateDocument() {
        return new PrepareExistingUser();
    }
}
