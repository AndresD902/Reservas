package com.institucion.reservas.e2e.serenity.questions;

import com.microsoft.playwright.Locator;
import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.playwright.abilities.BrowseTheWebWithPlaywright;

/**
 * Texto del panel de confirmación (`Reserva confirmada` con el recurso y
 * el usuario). Se lee directo de la página porque el panel comparte
 * `role="status"` con otros avisos.
 */
public final class TheConfirmationMessage {

    public static Question<String> text() {
        return Question.about("el mensaje de confirmación").answeredBy(actor ->
                BrowseTheWebWithPlaywright.as(actor).getCurrentPage()
                        .locator("[role='status']")
                        .filter(new Locator.FilterOptions()
                                .setHasText("Reserva confirmada"))
                        .first()
                        .textContent());
    }

    private TheConfirmationMessage() {
    }
}
