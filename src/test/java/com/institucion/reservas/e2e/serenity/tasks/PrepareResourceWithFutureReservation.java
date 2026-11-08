package com.institucion.reservas.e2e.serenity.tasks;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

/**
 * Prepara un recurso con una reserva futura por API, para probar que el
 * cambio a mantenimiento se bloquea.
 */
public class PrepareResourceWithFutureReservation implements Task {

    private static final DateTimeFormatter API_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    @Step("{0} prepara un recurso con reserva futura por API")
    public <T extends Actor> void performAs(T actor) {
        AdminApiClient api = new AdminApiClient();
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        long userId = api.createUser("000 Usuario Serenity " + suffix,
                "SERMAN" + suffix, "serenity.mantenimiento." + suffix + "@example.com");

        String resourceName = "000 Serenity " + suffix;
        long resourceId = api.createResource(resourceName);

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(11).withMinute(0)
                .withSecond(0).withNano(0);
        api.createReservation(userId, resourceId,
                start.format(API_FORMAT), start.plusHours(1).format(API_FORMAT));

        actor.remember("recurso", resourceName);
    }

    public static PrepareResourceWithFutureReservation futureReservation() {
        return new PrepareResourceWithFutureReservation();
    }
}
