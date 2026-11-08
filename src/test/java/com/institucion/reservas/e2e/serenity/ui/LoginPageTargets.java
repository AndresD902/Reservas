package com.institucion.reservas.e2e.serenity.ui;

import net.serenitybdd.screenplay.playwright.Target;

/**
 * Elementos de la página de login con nombres descriptivos para el reporte.
 */
public final class LoginPageTargets {

    public static final Target HEADING =
            Target.the("encabezado de inicio de sesión")
                    .locatedBy("h1");

    public static final Target EMAIL_INPUT =
            Target.the("campo de correo electrónico")
                    .locatedBy("#login-correo");

    public static final Target PASSWORD_INPUT =
            Target.the("campo de contraseña")
                    .locatedBy("#login-password");

    public static final Target SUBMIT_BUTTON =
            Target.the("botón de inicio de sesión")
                    .locatedBy("button[type='submit']");

    private LoginPageTargets() {
    }
}
