package com.institucion.reservas.e2e.serenity.tasks;

import com.institucion.reservas.e2e.serenity.ui.MyReservationsTargets;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.playwright.interactions.Open;
import net.serenitybdd.screenplay.playwright.interactions.SelectFromOptions;

/**
 * Abre mis reservas y selecciona el usuario preparado.
 */
public class OpenMyReservations implements Task {

    @Override
    @Step("{0} abre mis reservas del usuario preparado")
    public <T extends Actor> void performAs(T actor) {
        String baseUrl = System.getProperty("serenity.base.url", "http://localhost:5173")
                .replaceAll("/+$", "");
        String userName = actor.recall("usuario");
        actor.attemptsTo(
                Open.url(baseUrl + "/mis-reservas"),
                SelectFromOptions.byVisibleText(userName).from(MyReservationsTargets.USER_SELECT));
    }

    public static OpenMyReservations forPreparedUser() {
        return new OpenMyReservations();
    }
}
