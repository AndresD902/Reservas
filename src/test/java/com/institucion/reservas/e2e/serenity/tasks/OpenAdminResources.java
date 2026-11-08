package com.institucion.reservas.e2e.serenity.tasks;

import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.playwright.interactions.Open;

/**
 * Abre el panel de administración de recursos (con sesión activa).
 */
public class OpenAdminResources implements Task {

    @Override
    @Step("{0} abre la administración de recursos")
    public <T extends Actor> void performAs(T actor) {
        String baseUrl = System.getProperty("serenity.base.url", "http://localhost:5173")
                .replaceAll("/+$", "");
        actor.attemptsTo(Open.url(baseUrl + "/admin/recursos"));
    }

    public static OpenAdminResources page() {
        return new OpenAdminResources();
    }
}
