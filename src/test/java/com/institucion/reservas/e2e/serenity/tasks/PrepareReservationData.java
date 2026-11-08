package com.institucion.reservas.e2e.serenity.tasks;

import java.util.UUID;

import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

/**
 * Prepara los datos de la reserva por API (igual que las specs de
 * Playwright): crea un usuario activo y un recurso disponible con nombres
 * únicos, y los recuerda en el actor.
 */
public class PrepareReservationData implements Task {

    @Override
    @Step("{0} prepara un usuario y un recurso por API")
    public <T extends Actor> void performAs(T actor) {
        AdminApiClient api = new AdminApiClient();
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        String userName = "000 Usuario Serenity " + suffix;
        api.createUser(userName, "SER" + suffix, "serenity." + suffix + "@example.com");

        String resourceName = "000 Serenity " + suffix;
        api.createResource(resourceName);

        actor.remember("usuario", userName);
        actor.remember("recurso", resourceName);
    }

    public static PrepareReservationData andResource() {
        return new PrepareReservationData();
    }
}
