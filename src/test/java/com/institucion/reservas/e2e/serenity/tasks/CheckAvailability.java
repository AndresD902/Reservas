package com.institucion.reservas.e2e.serenity.tasks;

import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.playwright.Target;
import net.serenitybdd.screenplay.playwright.interactions.Click;

/**
 * Completa las fechas y consulta la disponibilidad sin reservar.
 */
public class CheckAvailability implements Task {

    private static final Target CHECK_BUTTON =
            Target.the("botón de comprobar disponibilidad")
                    .locatedBy("button:has-text('Comprobar disponibilidad')");

    @Override
    @Step("{0} comprueba la disponibilidad")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                FillReservationRange.tomorrowMorning(),
                Click.on(CHECK_BUTTON));
    }

    public static CheckAvailability forTomorrowMorning() {
        return new CheckAvailability();
    }
}
