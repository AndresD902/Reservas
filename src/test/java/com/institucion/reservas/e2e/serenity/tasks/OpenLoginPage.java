package com.institucion.reservas.e2e.serenity.tasks;

import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.playwright.interactions.Open;

/**
 * Abre la página de login de la interfaz servida en `serenity.base.url`.
 */
public class OpenLoginPage implements Task {

    private static String loginUrl() {
        String baseUrl = System.getProperty("serenity.base.url", "http://localhost:5173");
        return baseUrl.replaceAll("/+$", "") + "/login";
    }

    @Override
    @Step("{0} abre la página de login")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(Open.url(loginUrl()));
    }

    public static OpenLoginPage onTheLoginPage() {
        return new OpenLoginPage();
    }
}
