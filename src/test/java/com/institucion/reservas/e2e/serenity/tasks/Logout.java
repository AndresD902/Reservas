package com.institucion.reservas.e2e.serenity.tasks;

import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.playwright.Target;
import net.serenitybdd.screenplay.playwright.interactions.Click;

/**
 * Cierra la sesión desde el encabezado. Espera a que el router vuelva al
 * login porque el cierre ocurre en segundo plano y la URL tarda en cambiar.
 */
public class Logout implements Task {

    private static final Target LOGOUT_BUTTON =
            Target.the("botón de cerrar sesión")
                    .locatedBy("button:has-text('Cerrar sesión')");

    @Override
    @Step("{0} cierra la sesión")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                Click.on(LOGOUT_BUTTON),
                WaitForUrl.containing("/login"));
    }

    public static Logout closeSession() {
        return new Logout();
    }
}
