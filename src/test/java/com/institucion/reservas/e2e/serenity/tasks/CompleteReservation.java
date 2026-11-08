package com.institucion.reservas.e2e.serenity.tasks;

import com.institucion.reservas.e2e.serenity.ui.ReservationPageTargets;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.playwright.interactions.Click;

/**
 * Envía la reserva con las fechas ya completadas.
 */
public class CompleteReservation implements Task {

    @Override
    @Step("{0} envía la reserva")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(Click.on(ReservationPageTargets.SUBMIT_BUTTON));
    }

    public static CompleteReservation submit() {
        return new CompleteReservation();
    }
}
