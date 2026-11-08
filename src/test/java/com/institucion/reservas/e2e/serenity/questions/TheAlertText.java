package com.institucion.reservas.e2e.serenity.questions;

import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.playwright.abilities.BrowseTheWebWithPlaywright;

/**
 * Texto del mensaje de error visible (`role="alert"`).
 */
public final class TheAlertText {

    public static Question<String> value() {
        return Question.about("el mensaje de error").answeredBy(actor ->
                BrowseTheWebWithPlaywright.as(actor).getCurrentPage()
                        .locator("[role='alert']")
                        .first()
                        .textContent());
    }

    private TheAlertText() {
    }
}
