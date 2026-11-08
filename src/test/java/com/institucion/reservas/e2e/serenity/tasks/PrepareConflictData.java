package com.institucion.reservas.e2e.serenity.tasks;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;

/**
 * Prepara el conflicto de horario: un propietario con reserva mañana de
 * 10:00 a 11:00 y un segundo usuario (visitante) que intentará la misma
 * reserva por la interfaz.
 */
public class PrepareConflictData implements Task {

    private static final DateTimeFormatter API_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    @Override
    @Step("{0} prepara una reserva existente para el conflicto")
    public <T extends Actor> void performAs(T actor) {
        AdminApiClient api = new AdminApiClient();
        String suffix = UUID.randomUUID().toString().substring(0, 8);

        long ownerId = api.createUser("000 Propietario Serenity " + suffix,
                "SEROWN" + suffix, "serenity.propietario." + suffix + "@example.com");
        String visitorName = "000 Visitante Serenity " + suffix;
        api.createUser(visitorName, "SERVIS" + suffix, "serenity.visitante." + suffix + "@example.com");

        String resourceName = "000 Serenity " + suffix;
        long resourceId = api.createResource(resourceName);

        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0)
                .withSecond(0).withNano(0);
        api.createReservation(ownerId, resourceId,
                start.format(API_FORMAT), start.plusHours(1).format(API_FORMAT));

        actor.remember("usuario", visitorName);
        actor.remember("recurso", resourceName);
    }

    public static PrepareConflictData withExistingReservation() {
        return new PrepareConflictData();
    }
}
