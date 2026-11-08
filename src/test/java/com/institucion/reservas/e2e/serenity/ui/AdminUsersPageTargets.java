package com.institucion.reservas.e2e.serenity.ui;

import net.serenitybdd.screenplay.playwright.Target;

/**
 * Elementos del panel de administración de usuarios.
 */
public final class AdminUsersPageTargets {

    public static final Target HEADING =
            Target.the("encabezado de administración de usuarios")
                    .locatedBy("h1");

    private AdminUsersPageTargets() {
    }
}
