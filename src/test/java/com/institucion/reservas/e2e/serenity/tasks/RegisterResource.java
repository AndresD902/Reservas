package com.institucion.reservas.e2e.serenity.tasks;

import com.institucion.reservas.e2e.serenity.ui.AdminFormTargets;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.playwright.interactions.Click;
import net.serenitybdd.screenplay.playwright.interactions.Enter;
import net.serenitybdd.screenplay.playwright.interactions.Open;

/**
 * Registra un recurso desde el formulario de administración de recursos.
 */
public class RegisterResource implements Task {

    private final String name;

    private RegisterResource(String name) {
        this.name = name;
    }

    @Override
    @Step("{0} registra el recurso #name")
    public <T extends Actor> void performAs(T actor) {
        String baseUrl = System.getProperty("serenity.base.url", "http://localhost:5173")
                .replaceAll("/+$", "");
        actor.attemptsTo(
                Open.url(baseUrl + "/admin/recursos"),
                Enter.theValue(name).into(AdminFormTargets.RESOURCE_NAME),
                Enter.theValue("Sala para pruebas de administración")
                        .into(AdminFormTargets.RESOURCE_DESCRIPTION),
                Enter.theValue("SALA").into(AdminFormTargets.RESOURCE_TYPE),
                Enter.theValue("10").into(AdminFormTargets.RESOURCE_CAPACITY),
                Click.on(AdminFormTargets.REGISTER_RESOURCE_BUTTON));
    }

    public static RegisterResource with(String name) {
        return new RegisterResource(name);
    }
}
