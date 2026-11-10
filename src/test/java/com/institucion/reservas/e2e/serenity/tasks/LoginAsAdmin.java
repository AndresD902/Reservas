package com.institucion.reservas.e2e.serenity.tasks;

import com.institucion.reservas.e2e.serenity.ui.AdminUsersPageTargets;
import com.institucion.reservas.e2e.serenity.ui.LoginPageTargets;
import net.serenitybdd.annotations.Step;
import net.serenitybdd.screenplay.Actor;
import net.serenitybdd.screenplay.Task;
import net.serenitybdd.screenplay.playwright.interactions.Click;
import net.serenitybdd.screenplay.playwright.interactions.Enter;
import net.serenitybdd.screenplay.playwright.interactions.Open;
import net.serenitybdd.screenplay.playwright.interactions.WaitFor;

/**
 * Inicia sesión como administrador por la interfaz: entra al panel (que
 * redirige al login sin sesión), completa el formulario y envía.
 */
public class LoginAsAdmin implements Task {

    private static String adminUsersUrl() {
        String baseUrl = System.getProperty("serenity.base.url", "http://localhost:5173");
        return baseUrl.replaceAll("/+$", "") + "/admin/usuarios";
    }

    private static String adminEmail() {
        return System.getProperty("serenity.admin.email",
                System.getenv().getOrDefault("E2E_ADMIN_EMAIL", "admin@example.com"));
    }

    private static String adminPassword() {
        return System.getProperty("serenity.admin.password",
                System.getenv().getOrDefault("E2E_ADMIN_PASSWORD", "admin123"));
    }

    @Override
    @Step("{0} inicia sesión como administrador")
    public <T extends Actor> void performAs(T actor) {
        actor.attemptsTo(
                Open.url(adminUsersUrl()),
                Enter.theValue(adminEmail()).into(LoginPageTargets.EMAIL_INPUT),
                Enter.theValue(adminPassword()).into(LoginPageTargets.PASSWORD_INPUT),
                Click.on(LoginPageTargets.SUBMIT_BUTTON),
                WaitFor.selector(AdminUsersPageTargets.HEADING));
    }

    public static LoginAsAdmin withDefaultCredentials() {
        return new LoginAsAdmin();
    }
}
