package com.institucion.reservas.e2e.serenity.ui;

import net.serenitybdd.screenplay.playwright.Target;

/**
 * Elementos de la pantalla de mis reservas: artículo por estado, botón de
 * cancelación y diálogo de confirmación.
 */
public final class MyReservationsTargets {

    public static final Target USER_SELECT =
            Target.the("selector de usuario")
                    .locatedBy("select");

    public static final Target CANCEL_BUTTON =
            Target.the("botón de cancelar reserva")
                    .locatedBy("button:has-text('Cancelar reserva')");

    public static final Target CONFIRM_CANCEL_BUTTON =
            Target.the("botón de confirmación de cancelación")
                    .locatedBy("[role='dialog'] button:has-text('Sí, cancelar')");

    public static Target reservationArticle(String resourceName, String state) {
        return Target.the("reserva de " + resourceName + " en estado " + state)
                .locatedBy("article[aria-label*='" + resourceName + " en estado " + state + "']");
    }

    private MyReservationsTargets() {
    }
}
