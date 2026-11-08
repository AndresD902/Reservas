package com.institucion.reservas.e2e.serenity.questions;

import net.serenitybdd.screenplay.Question;
import net.serenitybdd.screenplay.playwright.abilities.BrowseTheWebWithPlaywright;

/**
 * URL actual del navegador. Se lee directo de la página porque en Serenity
 * 5.3.11 `Ensure.thatTheCurrentUrl()` no es público para componerlo.
 */
public final class TheCurrentUrl {

    public static Question<String> value() {
        return Question.about("la URL actual").answeredBy(
                actor -> BrowseTheWebWithPlaywright.as(actor).getCurrentPage().url());
    }

    private TheCurrentUrl() {
    }
}
