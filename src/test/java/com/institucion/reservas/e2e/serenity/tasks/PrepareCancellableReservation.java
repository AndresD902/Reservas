package com.institucion.reservas.e2e.serenity.tasks;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

/**
 * Prepara una reserva activa por API para consultarla y cancelarla desde
 * la interfaz de mis reservas.
 */
public class PrepareCancellableReservation implements Task {

    private static final DateTimeFormatter API_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    @Step("{0} prepara una reserva activa por API")
    public <T extends Actor> void performAs(T actor) {
        AdminApiClient api = new AdminApiClient();
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        String userName = "000 Usuario Serenity " + suffix;
        long userId = api.createUser(userName, "SERCAN" + suffix,
                "serenity.cancelar." + suffix + "@example.com");

        String resourceName = "000 Serenity " + suffix;
        long resourceId = api.createResource(resourceName);

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0)
                .withSecond(0).withNano(0);
        long reservationId = api.createReservation(userId, resourceId,
                start.format(API_FORMAT), start.plusHours(1).format(API_FORMAT));

        actor.remember("usuario", userName);
        actor.remember("recurso", resourceName);
        actor.remember("reservaId", reservationId);
    }

    public static PrepareCancellableReservation active() {
        return new PrepareCancellableReservation();
    }
}
