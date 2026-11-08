package com.institucion.reservas.e2e.serenity.tasks;

import com.institucion.reservas.e2e.serenity.ui.MyReservationsTargets;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.playwright.interactions.Click;

/**
 * Cancela la reserva activa y confirma en el diálogo.
 */
public class CancelReservation implements Task {

    @Override
    @Step("{0} cancela la reserva activa")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                Click.on(MyReservationsTargets.CANCEL_BUTTON),
                Click.on(MyReservationsTargets.CONFIRM_CANCEL_BUTTON));
    }

    public static CancelReservation active() {
        return new CancelReservation();
    }
}
