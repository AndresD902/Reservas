package com.institucion.reservas.e2e.serenity.tasks;

import com.institucion.reservas.e2e.serenity.ui.ReservationPageTargets;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.playwright.interactions.Click;
import net.serenitybdd.screenplay.playwright.interactions.Open;
import net.serenitybdd.screenplay.playwright.interactions.SelectFromOptions;

/**
 * Abre el listado de reservas, elige el usuario preparado y entra al
 * detalle del recurso preparado.
 */
public class OpenReservationPage implements Task {

    @Override
    @Step("{0} abre la reserva del recurso preparado")
    public <T extends Actor> void performAs(T actor) {
        String baseUrl = System.getProperty("serenity.base.url", "http://localhost:5173")
                .replaceAll("/+$", "");
        String userName = actor.recall("usuario");
        String resourceName = actor.recall("recurso");
        actor.attemptsTo(
                Open.url(baseUrl + "/reservar"),
                SelectFromOptions.byVisibleText(userName).from(ReservationPageTargets.USER_SELECT),
                Click.on(ReservationPageTargets.resourceLink(resourceName)));
    }

    public static OpenReservationPage forPreparedResource() {
        return new OpenReservationPage();
    }
}
