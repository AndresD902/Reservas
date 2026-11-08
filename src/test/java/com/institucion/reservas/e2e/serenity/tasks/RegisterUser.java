package com.institucion.reservas.e2e.serenity.tasks;

import com.institucion.reservas.e2e.serenity.ui.AdminFormTargets;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.playwright.interactions.Click;
import net.serenitybdd.screenplay.playwright.interactions.Enter;
import net.serenitybdd.screenplay.playwright.interactions.Open;

/**
 * Registra un usuario desde el formulario de administración de usuarios.
 */
public class RegisterUser implements Task {

    private final String name;
    private final String document;
    private final String email;

    private RegisterUser(String name, String document, String email) {
        this.name = name;
        this.document = document;
        this.email = email;
    }

    @Override
    @Step("{0} registra el usuario #name")
    public <T extends Actor> void performAs(T actor) {
        String baseUrl = System.getProperty("serenity.base.url", "http://localhost:5173")
                .replaceAll("/+$", "");
        actor.attemptsTo(
                Open.url(baseUrl + "/admin/usuarios"),
                Enter.theValue(name).into(AdminFormTargets.USER_NAME),
                Enter.theValue(document).into(AdminFormTargets.USER_DOCUMENT),
                Enter.theValue(email).into(AdminFormTargets.USER_EMAIL),
                Click.on(AdminFormTargets.REGISTER_USER_BUTTON));
    }

    public static RegisterUser with(String name, String document, String email) {
        return new RegisterUser(name, document, email);
    }
}
