package com.institucion.reservas.e2e.serenity.tasks;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.institucion.reservas.e2e.serenity.ui.ReservationPageTargets;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.playwright.interactions.Enter;

/**
 * Completa las fechas del formulario con un intervalo configurable. Por
 * defecto es mañana de 10:00 a 11:00.
 */
public class FillReservationRange implements Task {

    private static final DateTimeFormatter INPUT_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

    private final LocalDateTime start;
    private final LocalDateTime end;

    private FillReservationRange(LocalDateTime start, LocalDateTime end) {
        this.start = start;
        this.end = end;
    }

    @Override
    @Step("{0} completa las fechas de la reserva")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                Enter.theValue(start.format(INPUT_FORMAT)).into(ReservationPageTargets.START_INPUT),
                Enter.theValue(end.format(INPUT_FORMAT)).into(ReservationPageTargets.END_INPUT));
    }

    public static FillReservationRange tomorrowMorning() {
        LocalDateTime start = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0)
                .withSecond(0).withNano(0);
        return new FillReservationRange(start, start.plusHours(1));
    }

    public static FillReservationRange fromTo(LocalDateTime start, LocalDateTime end) {
        return new FillReservationRange(start, end);
    }
}
