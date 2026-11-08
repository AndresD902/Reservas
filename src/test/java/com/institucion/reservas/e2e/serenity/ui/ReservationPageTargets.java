package com.institucion.reservas.e2e.serenity.ui;

import net.serenitybdd.screenplay.playwright.Target;

/**
 * Elementos del flujo de reserva: selector de usuario, enlace al detalle
 * del recurso y formulario de fechas.
 */
public final class ReservationPageTargets {

    public static final Target USER_SELECT =
            Target.the("selector de usuario")
                    .locatedBy("select");

    public static final Target START_INPUT =
            Target.the("campo de fecha y hora de inicio")
                    .locatedBy("#reservation-start");

    public static final Target END_INPUT =
            Target.the("campo de fecha y hora de fin")
                    .locatedBy("#reservation-end");

    public static final Target SUBMIT_BUTTON =
            Target.the("botón de reserva")
                    .locatedBy("button[type='submit']");

    public static Target resourceLink(String resourceName) {
        return Target.the("enlace de reserva de " + resourceName)
                .locatedBy("a[aria-label='Reservar " + resourceName + "']");
    }

    private ReservationPageTargets() {
    }
}
